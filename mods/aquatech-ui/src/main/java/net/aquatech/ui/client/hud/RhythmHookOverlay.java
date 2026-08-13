package net.aquatech.ui.client.hud;

import net.aquatech.ui.client.render.FishingMinigameFx;
import net.aquatech.ui.client.render.HitParticle;
import net.aquatech.ui.network.C2SRhythmHookResultPacket;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.registry.ModSounds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * StarCatcher-style vertical bar fishing minigame (hold RMB / A / D in sweet spot).
 */
public final class RhythmHookOverlay {

    private static final int GRACE_TICKS = 80;
    private static final int HOLDING_DELAY = 6;
    private static final int RESULT_LINGER = 40;
    private static final int MAX_TICKS = 600;
    private static final float RENDER_SCALE = 1.0f;

    private static boolean active;
    private static int seed;
    private static int fishHp;
    private static float spotSize;
    private static float yellowPad;
    private static float pointerSpeed;
    private static float decay;
    private static boolean elite;
    private static boolean treasureActive;

    private static float progress;
    private static float progressSmooth;
    private static float pointerPos;
    private static float pointerPrev;
    private static int pointerDir;
    private static float spotCenter;
    private static float spotMovePhase;
    private static int treasurePct;

    private static int tickCount;
    private static int holdingTicks;
    private static boolean grace;
    private static boolean finished;
    private static boolean pendingSuccess;
    private static int pendingQuality;
    private static int resultLinger;
    private static int hitsGreen;
    private static int hitsYellow;
    private static boolean perfectCatch;

    private static float kimbeAlpha;
    private static float kimbePos;
    private static int hitCooldown;
    private static final List<HitParticle> particles = new ArrayList<>();
    private static Random rng;

    private RhythmHookOverlay() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void start(int seedIn, int fishHpIn, float spotSizeIn, float yellowPadIn,
                             float pointerSpeedIn, float decayIn, boolean eliteIn, boolean treasureIn) {
        active = true;
        finished = false;
        resultLinger = 0;
        seed = seedIn;
        fishHp = Math.max(12, fishHpIn);
        spotSize = Mth.clamp(spotSizeIn, 12f, 40f);
        yellowPad = Mth.clamp(yellowPadIn, 4f, 18f);
        pointerSpeed = Mth.clamp(pointerSpeedIn, 0.8f, 4.5f);
        decay = Mth.clamp(decayIn, 0.02f, 0.12f);
        elite = eliteIn;
        treasureActive = treasureIn;

        rng = new Random(seed);
        progress = fishHp;
        progressSmooth = fishHp;
        pointerPos = rng.nextFloat() * 100f;
        pointerPrev = pointerPos;
        pointerDir = rng.nextBoolean() ? 1 : -1;
        spotCenter = 35f + rng.nextFloat() * 30f;
        spotMovePhase = rng.nextFloat() * 10f;
        treasurePct = 0;
        tickCount = 0;
        holdingTicks = 0;
        grace = true;
        hitsGreen = 0;
        hitsYellow = 0;
        perfectCatch = true;
        kimbeAlpha = 0f;
        kimbePos = spotCenter;
        hitCooldown = 0;
        particles.clear();

        suppressVanillaInput();
        playUi(ModSounds.SAFE_CHIME.get(), 0.5f, elite ? 1.2f : 1.0f);
    }

    public static void stopLocal() {
        active = false;
        finished = true;
    }

    private static boolean isHoldingInput() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) return false;
        long win = mc.getWindow().getWindow();
        boolean rmb = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean a = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS;
        boolean d = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS;
        return rmb || a || d;
    }

    public static void tick() {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            stopLocal();
            return;
        }

        suppressVanillaInput();
        freezePlayerMotion(mc.player);

        if (hitCooldown > 0) hitCooldown--;
        tickParticles();

        if (finished) {
            resultLinger--;
            if (resultLinger <= 0) {
                active = false;
                NetworkHandler.CHANNEL.sendToServer(new C2SRhythmHookResultPacket(
                        pendingSuccess, pendingQuality, hitsGreen, hitsYellow));
            }
            return;
        }

        tickCount++;
        pointerPrev = pointerPos;

        if (elite) {
            spotCenter = 50f + Mth.sin((tickCount + spotMovePhase) * 0.04f) * 22f;
        }

        float speed = pointerSpeed * (grace ? 0.35f : 1f);
        pointerPos += speed * pointerDir;
        if (pointerPos >= 100f) {
            pointerPos = 100f;
            pointerDir = -1;
        } else if (pointerPos <= 0f) {
            pointerPos = 0f;
            pointerDir = 1;
        }

        if (grace) {
            if (tickCount >= GRACE_TICKS) {
                grace = false;
            }
            progressSmooth += (progress - progressSmooth) / 6f;
            updateKimbe(false);
            return;
        }

        boolean holding = isHoldingInput();
        if (holding) {
            holdingTicks++;
        } else {
            holdingTicks = 0;
        }

        int zone = zoneAt(pointerPos);
        boolean effectiveHold = holding && holdingTicks >= HOLDING_DELAY;

        if (effectiveHold) {
            if (zone == 2) {
                progress -= 1.15f;
                hitsGreen++;
                onHitSpark(true);
                if (treasureActive) {
                    treasurePct = Math.min(100, treasurePct + 3);
                }
            } else if (zone == 1) {
                progress -= 0.55f;
                hitsYellow++;
                perfectCatch = false;
                onHitSpark(false);
                if (treasureActive) {
                    treasurePct = Math.min(100, treasurePct + 1);
                }
            } else {
                progress += 0.35f;
                perfectCatch = false;
            }
        } else {
            progress += decay;
        }

        progress = Mth.clamp(progress, 0f, fishHp * 1.5f);
        progressSmooth += (progress - progressSmooth) / 6f;

        updateKimbe(zone >= 1);

        if (progress <= 0.05f) {
            int quality = perfectCatch ? 100 : Math.max(55, 100 - hitsYellow * 3);
            triggerFinish(true, quality);
            return;
        }

        if (progress >= fishHp * 1.35f || tickCount > MAX_TICKS) {
            triggerFinish(false, 0);
        }
    }

    private static int zoneAt(float pos) {
        float half = spotSize * 0.5f;
        float greenLo = spotCenter - half;
        float greenHi = spotCenter + half;
        float yLo = greenLo - yellowPad;
        float yHi = greenHi + yellowPad;
        if (pos >= greenLo && pos <= greenHi) return 2;
        if (pos >= yLo && pos <= yHi) return 1;
        return 0;
    }

    private static void updateKimbe(boolean inZone) {
        float dist = Math.abs(pointerPos - spotCenter);
        if (dist < spotSize + yellowPad + 8f) {
            kimbePos = spotCenter;
            kimbeAlpha = Mth.lerp(0.15f, kimbeAlpha, inZone ? 0.9f : 0.45f);
        } else {
            kimbeAlpha = Mth.lerp(0.1f, kimbeAlpha, 0f);
        }
    }

    private static void onHitSpark(boolean perfect) {
        if (hitCooldown > 0) return;
        hitCooldown = 4;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int ox = sw / 2;
        int oy = sh / 2;
        int barY = oy - FishingMinigameFx.gui(FishingMinigameFx.TANK_H) / 2 + FishingMinigameFx.gui(32);
        int barH = FishingMinigameFx.gui(FishingMinigameFx.BAR_H);
        float py = barY + (100f - pointerPos) / 100f * barH;
        int color = perfect ? FishingMinigameFx.SAFE : FishingMinigameFx.GOLD;
        particles.add(new HitParticle(ox + FishingMinigameFx.gui(8), py, color, mc.player.getRandom()));
        playUi(perfect ? ModSounds.CATCH_SUCCESS.get() : ModSounds.PULL_TICK.get(), 0.45f, 1.05f);
    }

    private static void tickParticles() {
        Iterator<HitParticle> it = particles.iterator();
        while (it.hasNext()) {
            if (!it.next().tick()) it.remove();
        }
    }

    private static void triggerFinish(boolean success, int quality) {
        if (finished) return;
        finished = true;
        pendingSuccess = success;
        pendingQuality = Mth.clamp(quality, 0, 100);
        resultLinger = RESULT_LINGER;
        if (success) {
            playUi(ModSounds.CATCH_SUCCESS.get(), 0.85f, pendingQuality >= 90 ? 1.25f : 1.05f);
        } else {
            playUi(ModSounds.LINE_SNAP.get(), 0.85f, 0.9f);
        }
    }

    private static void playUi(SoundEvent sound, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || sound == null) return;
        mc.player.playSound(sound, volume, pitch);
    }

    private static void suppressVanillaInput() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) return;
        KeyMapping.releaseAll();
        if (mc.player != null) {
            Input input = mc.player.input;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
            input.shiftKeyDown = false;
            input.forwardImpulse = 0f;
            input.leftImpulse = 0f;
        }
    }

    private static void freezePlayerMotion(LocalPlayer player) {
        player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
        player.xxa = 0f;
        player.zza = 0f;
        player.setSprinting(false);
    }

    public static void render(GuiGraphics g, float partialTick) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int sw = g.guiWidth();
        int sh = g.guiHeight();
        int ox = sw / 2;
        int oy = sh / 2 - 8;
        float animT = FishingMinigameFx.time(partialTick);

        FishingMinigameFx.dimScreen(g, sw, sh, finished ? 0.22f : 0.14f);

        boolean inSpot = !grace && zoneAt(pointerPos) >= 2;
        boolean holding = isHoldingInput();

        FishingMinigameFx.drawMinigame(g, ox, oy, RENDER_SCALE,
                progress, progressSmooth, fishHp,
                pointerPos, pointerPrev, partialTick,
                spotCenter, spotSize, yellowPad,
                treasureActive, treasurePct,
                inSpot, holding, kimbeAlpha, kimbePos,
                particles, animT, grace);

        int hintY = oy + FishingMinigameFx.gui(FishingMinigameFx.TANK_H) / 2 + FishingMinigameFx.gui(8);
        if (finished) {
            String key = pendingSuccess
                    ? "hud.aquatech_ui.rhythm_hook.result_success"
                    : "hud.aquatech_ui.rhythm_hook.result_fail";
            int col = pendingSuccess ? FishingMinigameFx.SAFE : FishingMinigameFx.DANGER;
            FishingMinigameFx.drawHint(g, mc.font, ox, hintY,
                    Component.translatable(key).getString(), col);
        } else if (grace) {
            FishingMinigameFx.drawHint(g, mc.font, ox, hintY,
                    Component.translatable("hud.aquatech_ui.rhythm_hook.grace").getString(), FishingMinigameFx.WATER);
        } else {
            String hint = inSpot && holding
                    ? "hud.aquatech_ui.rhythm_hook.holding"
                    : "hud.aquatech_ui.rhythm_hook.hint_hold";
            int col = inSpot ? FishingMinigameFx.SAFE : FishingMinigameFx.CREAM;
            FishingMinigameFx.drawHint(g, mc.font, ox, hintY,
                    Component.translatable(hint).getString(), col);
        }

        String titleKey = elite
                ? "hud.aquatech_ui.rhythm_hook.title_elite"
                : "hud.aquatech_ui.rhythm_hook.title";
        int titleCol = elite ? 0xFFB05030 : FishingMinigameFx.INK;
        FishingMinigameFx.drawHint(g, mc.font, ox, oy - FishingMinigameFx.gui(FishingMinigameFx.TANK_H) / 2 - FishingMinigameFx.gui(6),
                Component.translatable(titleKey).getString(), titleCol);
    }
}
