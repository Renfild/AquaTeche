package com.casesmod.client.gui;

import com.casesmod.client.gui.widget.CustomButton;
import com.casesmod.data.CaseDefinition;
import com.casesmod.data.CaseItem;
import com.casesmod.data.CaseManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Анимация открытия кейса в стиле рулетки (как в CS:GO), с дополнительными эффектами:
 *  - подсветка редкости по всей ленте (не только у победителя)
 *  - "смаз" (motion blur) призрачными копиями предметов на высокой скорости
 *  - азартная кривая замедления (резкий разгон, долгое напряжённое торможение)
 *  - пульсация центрального указателя, нарастающая к концу
 *  - тряска экрана и 2D-конфетти при EPIC/LEGENDARY призе
 *  - кнопка пропуска анимации
 */
public class CaseOpeningScreen extends Screen {
    private static final int SLOT_SIZE = 70;
    private static final int REEL_LENGTH = 60;
    private static final int TARGET_INDEX = 48;
    private static final long SPIN_DURATION_MS = 5200;
    private static final long SKIP_MIN_MS = 900;

    private final String caseId;
    private final String wonItemId;
    private final int wonCount;
    private final CaseItem.Rarity rarity;
    private final String wonDisplayName;

    private final List<Slot> reel = new ArrayList<>();
    private long startTime = -1;
    private boolean finished = false;
    private boolean skipping = false;
    private long lastTickSoundAt = 0;
    private boolean anticipationPlayed = false;
    private long revealAt = -1;
    private final List<Confetto> confetti = new ArrayList<>();
    private final Random rnd = new Random();
    private CustomButton skipButton;
    private CustomButton backButton;
    private long lastConfettiUpdate = -1;

    private record Slot(ItemStack stack, CaseItem.Rarity rarity) {}

    private record Confetto(float x, float y, float vx, float vy, float rot, float vrot, int color) {}

    public CaseOpeningScreen(String caseId, String itemId, int count, String rarity, String displayName) {
        super(Component.literal("Открытие кейса"));
        this.caseId = caseId;
        this.wonItemId = itemId;
        this.wonCount = count;
        CaseItem.Rarity r;
        try { r = CaseItem.Rarity.valueOf(rarity.toUpperCase()); } catch (Exception e) { r = CaseItem.Rarity.COMMON; }
        this.rarity = r;
        this.wonDisplayName = displayName;
    }

    @Override
    protected void init() {
        buildReel();
        startTime = System.currentTimeMillis();

        skipButton = new CustomButton(width / 2 - 60, height - 46, 120, 20, Component.literal("Пропустить »"),
                0, 0xFF888888, b -> requestSkip());
        skipButton.visible = false;
        addRenderableWidget(skipButton);

        backButton = new CustomButton(width / 2 - 70, height - 46, 140, 22, Component.literal("К спискам кейсов"),
                0, 0xFF5CE1FF, b -> minecraft.setScreen(new CasesScreen()));
        backButton.visible = false;
        addRenderableWidget(backButton);
    }

    private void buildReel() {
        reel.clear();
        List<CaseItem> pool = poolFromNetworkOrFallback();

        for (int i = 0; i < REEL_LENGTH; i++) {
            if (i == TARGET_INDEX) {
                reel.add(new Slot(makeStack(wonItemId, wonCount), rarity));
            } else if (!pool.isEmpty()) {
                CaseItem random = pool.get(rnd.nextInt(pool.size()));
                reel.add(new Slot(makeStack(random.itemId, random.count), random.rarityEnum()));
            } else {
                reel.add(new Slot(makeStack(wonItemId, 1), rarity));
            }
        }
    }

    /**
     * Пул предметов для визуальной ленты (не влияет на результат — тот уже определён сервером).
     * Берётся из синхронизированного по сети списка кейсов (работает и на выделенном сервере);
     * CaseManager.INSTANCE — запасной вариант на случай одиночной игры/интегрированного сервера.
     */
    private List<CaseItem> poolFromNetworkOrFallback() {
        for (com.casesmod.network.packets.CaseListSyncS2CPacket.CaseSnapshot snap : com.casesmod.client.ClientCaseState.cases) {
            if (!snap.id().equals(caseId)) continue;
            List<CaseItem> pool = new ArrayList<>();
            for (var it : snap.items()) {
                CaseItem ci = new CaseItem();
                ci.itemId = it.itemId(); ci.count = it.count(); ci.weight = it.weight();
                ci.rarity = it.rarity(); ci.displayName = it.displayName();
                pool.add(ci);
            }
            return pool;
        }
        CaseDefinition def = CaseManager.INSTANCE.get(caseId);
        return (def != null && !def.items.isEmpty()) ? def.items : List.of();
    }

    private ItemStack makeStack(String id, int count) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
            return new ItemStack(item, Math.max(1, count));
        } catch (Exception e) {
            return new ItemStack(net.minecraft.world.item.Items.BARRIER);
        }
    }

    private static double easeOutQuint(double t) {
        double p = 1 - t;
        return 1 - p * p * p * p * p;
    }

    private void requestSkip() {
        if (finished || skipping) return;
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed < SKIP_MIN_MS) return;
        skipping = true;
        long remainingVirtual = 350;
        startTime = System.currentTimeMillis() - (SPIN_DURATION_MS - remainingVirtual);
        skipButton.active = false;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        long elapsed = System.currentTimeMillis() - startTime;
        double t = Math.min(1.0, elapsed / (double) SPIN_DURATION_MS);
        double eased = easeOutQuint(t);

        double eased2 = easeOutQuint(Math.min(1.0, (elapsed + 16) / (double) SPIN_DURATION_MS));
        double instSpeed = Math.max(0, eased2 - eased);

        if (t >= 1.0 && !finished) {
            finished = true;
            revealAt = System.currentTimeMillis();
            skipButton.visible = false;
            backButton.visible = true;
            playRevealEffects();
        }
        if (!finished) {
            skipButton.visible = elapsed >= SKIP_MIN_MS;
        }

        int shakeX = 0, shakeY = 0;
        if (finished && (rarity == CaseItem.Rarity.EPIC || rarity == CaseItem.Rarity.LEGENDARY)) {
            long since = System.currentTimeMillis() - revealAt;
            double shakeDuration = rarity == CaseItem.Rarity.LEGENDARY ? 500 : 300;
            if (since < shakeDuration) {
                double power = (1.0 - since / shakeDuration);
                double magnitude = (rarity == CaseItem.Rarity.LEGENDARY ? 6 : 3) * power;
                shakeX = (int) ((rnd.nextDouble() - 0.5) * 2 * magnitude);
                shakeY = (int) ((rnd.nextDouble() - 0.5) * 2 * magnitude);
            }
        }

        renderBackground(gfx, t);

        gfx.pose().pushPose();
        gfx.pose().translate(shakeX, shakeY, 0);

        int centerX = width / 2;
        int reelY = height / 2 - SLOT_SIZE / 2 - 20;

        double finalOffset = TARGET_INDEX * SLOT_SIZE;
        double offset = finalOffset * eased;

        renderTickSound(t, elapsed, instSpeed);

        int viewportW = Math.min(width - 80, SLOT_SIZE * 7);
        int viewLeft = centerX - viewportW / 2;
        int viewRight = centerX + viewportW / 2;

        // ровная тёмная подложка ленты (без кривых градиентных краёв)
        gfx.fill(viewLeft, reelY - 6, viewRight, reelY + SLOT_SIZE + 6, 0xCC0A0E18);
        gfx.fill(viewLeft, reelY - 6, viewRight, reelY - 4, 0xFF5CE1FF);
        gfx.fill(viewLeft, reelY + SLOT_SIZE + 4, viewRight, reelY + SLOT_SIZE + 6, 0xFF5CE1FF);

        gfx.enableScissor(viewLeft, reelY - 4, viewRight, reelY + SLOT_SIZE + 4);

        int ghostCount = instSpeed > 0.012 ? 3 : instSpeed > 0.006 ? 2 : instSpeed > 0.002 ? 1 : 0;

        for (int i = 0; i < reel.size(); i++) {
            double slotX = centerX - offset + i * SLOT_SIZE - SLOT_SIZE / 2.0;
            if (slotX < viewLeft - SLOT_SIZE || slotX > viewRight + SLOT_SIZE) continue;

            boolean isWinner = i == TARGET_INDEX && finished;

            for (int g = ghostCount; g >= 1; g--) {
                float ghostAlpha = (0.22f / g);
                int gx = (int) (slotX + g * 9);
                renderSlot(gfx, gx, reelY, reel.get(i), false, ghostAlpha, 1f);
            }

            float pulse = 1f;
            if (isWinner) {
                long since = System.currentTimeMillis() - revealAt;
                pulse = 1f + 0.06f * (float) Math.sin(since / 90.0) * (float) Math.max(0, 1 - since / 1500.0);
                // мягкое текстурное свечение позади выигрышного предмета, тон — цвет редкости
                double burstPulse = 0.75 + 0.25 * Math.sin(since / 140.0);
                int glowAlpha = (int) (170 * Math.max(0, 1 - since / 2000.0) * burstPulse);
                int glowColor = (Math.max(0, glowAlpha) << 24) | (rarity.color & 0xFFFFFF);
                com.casesmod.client.gui.widget.ModTextures.blitGlowBurst(gfx,
                        (int) slotX + SLOT_SIZE / 2, reelY + SLOT_SIZE / 2, SLOT_SIZE, glowColor);
            }
            renderSlot(gfx, (int) slotX, reelY, reel.get(i), isWinner, 1f, pulse);
        }
        gfx.disableScissor();

        double suspense = Math.max(0, (t - 0.75) / 0.25);
        float markerPulse = 1f + 0.5f * (float) Math.sin(elapsed / 60.0) * (float) suspense;
        int markerColor = finished ? (rarity.color | 0xFF000000) : 0xFFFFFFFF;
        int markerW = finished ? 3 : (int) (2 + markerPulse);
        gfx.fill(centerX - markerW, reelY - 12, centerX + markerW, reelY + SLOT_SIZE + 12, markerColor);
        if (!finished && suspense > 0) {
            int glowAlpha = (int) (60 * suspense);
            gfx.fill(centerX - markerW - 3, reelY - 12, centerX - markerW, reelY + SLOT_SIZE + 12, (glowAlpha << 24) | 0xFFFFFF);
            gfx.fill(centerX + markerW, reelY - 12, centerX + markerW + 3, reelY + SLOT_SIZE + 12, (glowAlpha << 24) | 0xFFFFFF);
        }

        String title = finished ? "§l§fГотово!" : (suspense > 0.6 ? "§l§eЕщё чуть-чуть..." : "§l§fОткрытие кейса...");
        gfx.drawCenteredString(font, Component.literal(title), centerX, reelY - 40, 0xFFFFFFFF);

        if (finished) {
            renderReward(gfx, centerX, reelY + SLOT_SIZE + 40);
            updateAndRenderConfetti(gfx);
        }

        gfx.pose().popPose();

        super.render(gfx, mouseX, mouseY, partialTicks);
    }

    private void renderBackground(GuiGraphics gfx, double t) {
        int topBase = 0xF0090912, botBase = 0xF01A0F28;
        if (t > 0.75) {
            double blend = (t - 0.75) / 0.25;
            int tint = mixRGB(0x1A0F28, rarity.color, blend * 0.35);
            botBase = 0xF0000000 | tint;
        }
        if (finished) {
            long since = System.currentTimeMillis() - revealAt;
            double pulse = Math.max(0, 1 - since / 900.0) * (0.5 + 0.5 * Math.sin(since / 70.0));
            int tint = mixRGB(0x1A0F28, rarity.color, pulse * 0.3);
            botBase = 0xF0000000 | tint;
        }
        gfx.fillGradient(0, 0, width, height, topBase, botBase);
    }

    private static int mixRGB(int base, int accent, double t) {
        t = Math.max(0, Math.min(1, t));
        int br = (base >> 16) & 0xFF, bg = (base >> 8) & 0xFF, bb = base & 0xFF;
        int ar = (accent >> 16) & 0xFF, ag = (accent >> 8) & 0xFF, ab = accent & 0xFF;
        int r = (int) (br + (ar - br) * t);
        int g = (int) (bg + (ag - bg) * t);
        int b = (int) (bb + (ab - bb) * t);
        return (r << 16) | (g << 8) | b;
    }

    private void renderSlot(GuiGraphics gfx, int x, int y, Slot slot, boolean isWinner, float alpha, float scale) {
        CaseItem.Rarity r = slot.rarity();
        int rgb = r.color & 0xFFFFFF;
        int bgAlpha = isWinner ? 0x66 : 0x2A;
        int a = (int) (bgAlpha * alpha);
        int bg = (a << 24) | rgb;
        gfx.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, bg);

        int borderA = (int) ((isWinner ? 255 : 90) * alpha);
        int borderColor = (borderA << 24) | rgb;
        gfx.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + 2, borderColor);
        gfx.fill(x + 1, y + SLOT_SIZE - 2, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, borderColor);

        ItemStack stack = slot.stack();
        if (stack.isEmpty()) return;

        int cx = x + SLOT_SIZE / 2;
        int cy = y + SLOT_SIZE / 2;
        if (scale != 1f) {
            gfx.pose().pushPose();
            gfx.pose().translate(cx, cy, 0);
            gfx.pose().scale(scale, scale, 1f);
            gfx.pose().translate(-cx, -cy, 0);
        }
        if (alpha < 1f) {
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        }
        gfx.renderItem(stack, cx - 8, cy - 8);
        if (alpha < 1f) {
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        if (alpha >= 1f) {
            gfx.renderItemDecorations(font, stack, cx - 8, cy - 8);
        }
        if (scale != 1f) {
            gfx.pose().popPose();
        }
    }

    private void renderReward(GuiGraphics gfx, int centerX, int y) {
        String rarityName = switch (rarity) {
            case COMMON -> "§7Обычный";
            case UNCOMMON -> "§aНеобычный";
            case RARE -> "§9Редкий";
            case EPIC -> "§5Эпический";
            case LEGENDARY -> "§6§lЛЕГЕНДАРНЫЙ";
        };

        long since = System.currentTimeMillis() - revealAt;
        float appear = (float) Math.min(1.0, since / 260.0);
        float scale = 0.7f + 0.3f * easeOutBack(appear);
        int alpha = (int) (255 * appear);

        gfx.drawCenteredString(font, Component.literal(rarityName + " §fприз!"), centerX, y, (alpha << 24) | 0xFFFFFFFF);

        gfx.pose().pushPose();
        gfx.pose().translate(centerX, y + 16, 0);
        gfx.pose().scale(scale, scale, 1f);
        gfx.pose().translate(-centerX, -(y + 16), 0);
        String displayText;
        if (wonDisplayName != null && !wonDisplayName.isEmpty()) {
            if (wonDisplayName.contains("×") || wonDisplayName.contains("x") || wonDisplayName.contains("X")) {
                displayText = wonDisplayName;
            } else if (wonCount > 1) {
                displayText = wonDisplayName + " §7×" + wonCount;
            } else {
                displayText = wonDisplayName;
            }
        } else {
            String fallbackName = wonItemId != null ? wonItemId : "Предмет";
            displayText = "§f" + fallbackName + (wonCount > 1 ? " §7×" + wonCount : "");
        }
        gfx.drawCenteredString(font, Component.literal(displayText), centerX, y + 16, (alpha << 24) | 0xFFFFFFFF);
        gfx.pose().popPose();

        if (since > 400) {
            // Кнопка «← К кейсам» уже внизу — ESC-подсказку не рисуем (накладывалась на кнопку)
        }
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f, c3 = c1 + 1;
        float p = t - 1;
        return 1 + c3 * p * p * p + c1 * p * p;
    }

    private void renderTickSound(double t, long elapsed, double instSpeed) {
        if (t >= 1.0 || minecraft == null || minecraft.level == null) return;
        double speedFactor = Math.max(0.02, instSpeed * 40);
        long interval = (long) (35 + (1 - Math.min(1, speedFactor)) * 300);
        if (elapsed - lastTickSoundAt > interval) {
            lastTickSoundAt = elapsed;
            float pitch = 0.9f + (float) Math.min(1, speedFactor) * 0.9f;
            float volume = 0.35f + (float) Math.min(1, speedFactor) * 0.35f;
            minecraft.level.playLocalSound(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(),
                    com.casesmod.item.ModSounds.CASE_TICK.get(), SoundSource.MASTER, volume, pitch, false);
        }
        if (t > 0.72 && !anticipationPlayed) {
            anticipationPlayed = true;
            minecraft.level.playLocalSound(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(),
                    com.casesmod.item.ModSounds.ANTICIPATION_RISER.get(), SoundSource.MASTER, 0.55f, 1.0f, false);
        }
    }

    private void playRevealEffects() {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) return;
        var sound = switch (rarity) {
            case LEGENDARY -> com.casesmod.item.ModSounds.REVEAL_LEGENDARY.get();
            case EPIC -> com.casesmod.item.ModSounds.REVEAL_EPIC.get();
            case RARE -> com.casesmod.item.ModSounds.REVEAL_RARE.get();
            case UNCOMMON -> com.casesmod.item.ModSounds.REVEAL_UNCOMMON.get();
            case COMMON -> com.casesmod.item.ModSounds.REVEAL_COMMON.get();
        };
        minecraft.level.playLocalSound(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(),
                sound, SoundSource.MASTER, 1.0f, 1.0f, false);

        if (rarity == CaseItem.Rarity.EPIC || rarity == CaseItem.Rarity.LEGENDARY) {
            int count = rarity == CaseItem.Rarity.LEGENDARY ? 40 : 22;
            for (int i = 0; i < count; i++) {
                double ox = (rnd.nextDouble() - 0.5) * 1.5;
                double oy = rnd.nextDouble() * 1.5;
                double oz = (rnd.nextDouble() - 0.5) * 1.5;
                minecraft.level.addParticle(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
                        minecraft.player.getX() + ox, minecraft.player.getY() + 1 + oy, minecraft.player.getZ() + oz,
                        0, 0.1, 0);
            }
            spawnConfetti(rarity == CaseItem.Rarity.LEGENDARY ? 90 : 45);
        }
    }

    private void spawnConfetti(int count) {
        confetti.clear();
        int[] palette = rarity == CaseItem.Rarity.LEGENDARY
                ? new int[]{0xFFD700, 0xFFA500, 0xFFFFE066, 0xFFFFFFFF}
                : new int[]{rarity.color, 0xFFFFFFFF};
        for (int i = 0; i < count; i++) {
            float x = width / 2f + (rnd.nextFloat() - 0.5f) * 120;
            float y = height / 2f - 60 - rnd.nextFloat() * 40;
            float vx = (rnd.nextFloat() - 0.5f) * 2.2f;
            float vy = -2.5f - rnd.nextFloat() * 2.5f;
            float rot = rnd.nextFloat() * 360;
            float vrot = (rnd.nextFloat() - 0.5f) * 12f;
            int color = palette[rnd.nextInt(palette.length)] | 0xFF000000;
            confetti.add(new Confetto(x, y, vx, vy, rot, vrot, color));
        }
    }

    private void updateAndRenderConfetti(GuiGraphics gfx) {
        if (confetti.isEmpty()) return;
        long now = System.currentTimeMillis();
        if (lastConfettiUpdate < 0) lastConfettiUpdate = now;
        float dt = Math.min(0.05f, (now - lastConfettiUpdate) / 1000f);
        lastConfettiUpdate = now;

        List<Confetto> updated = new ArrayList<>(confetti.size());
        for (Confetto c : confetti) {
            float nvy = c.vy() + 9.0f * dt;
            float nx = c.x() + c.vx() * dt * 60;
            float ny = c.y() + nvy * dt * 60;
            float nrot = c.rot() + c.vrot();
            if (ny > height + 20) continue;
            Confetto nc = new Confetto(nx, ny, c.vx(), nvy, nrot, c.vrot(), c.color());
            updated.add(nc);
            drawConfetto(gfx, nc);
        }
        confetti.clear();
        confetti.addAll(updated);
    }

    private void drawConfetto(GuiGraphics gfx, Confetto c) {
        gfx.pose().pushPose();
        gfx.pose().translate(c.x(), c.y(), 0);
        gfx.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(c.rot()));
        com.casesmod.client.gui.widget.ModTextures.blitSparkle(gfx, 0, 0, 7, c.color());
        gfx.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return finished; }
}
