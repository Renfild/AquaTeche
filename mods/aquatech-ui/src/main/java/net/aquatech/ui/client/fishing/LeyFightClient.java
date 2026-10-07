package net.aquatech.ui.client.fishing;

import com.mojang.blaze3d.platform.InputConstants;
import net.aquatech.ui.fishing.LeyFight;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.C2SLeyFightInputPacket;
import net.aquatech.ui.network.packet.S2CLeyFightPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

/**
 * Клиентская сторона схватки со Старым Леем: читает ПКМ и клавиши влево/вправо, шлёт их серверу, рисует шкалы и
 * на время схватки глушит обычное управление. Решения принимает сервер, здесь только ввод и картинка.
 */
public final class LeyFightClient {

    private static final int BAR_W = 220;

    private static byte mode = S2CLeyFightPacket.MODE_END;
    private static float tension;
    private static float stamina = 100F;
    private static int phase;
    private static int surgeDir;
    private static float shownTension;
    private static float shownStamina = 100F;
    private static int ticks;

    private LeyFightClient() {
    }

    public static void apply(byte newMode, float newTension, float newStamina, byte newPhase, byte newDir) {
        if (mode == S2CLeyFightPacket.MODE_END && newMode != S2CLeyFightPacket.MODE_END) {
            shownTension = newTension;
            shownStamina = newStamina;
            ticks = 0;
        }
        mode = newMode;
        tension = newTension;
        stamina = newStamina;
        phase = newPhase;
        surgeDir = newDir;
    }

    public static boolean isActive() {
        return mode != S2CLeyFightPacket.MODE_END;
    }

    /** Борьба идёт: управление игрока занято удочкой. */
    public static boolean isFighting() {
        return mode == S2CLeyFightPacket.MODE_FIGHT;
    }

    /** ПКМ удочкой Лея вне схватки: сообщаем серверу сами, не полагаясь на клик (его может отменить защита региона). */
    public static void onRightClick() {
        Minecraft mc = Minecraft.getInstance();
        if (isActive() || mc.player == null || mc.screen != null
                || !mc.player.getMainHandItem().is(net.aquatech.ui.registry.ModItems.LEY_ROD.get())) {
            return;
        }
        NetworkHandler.CHANNEL.sendToServer(new net.aquatech.ui.network.packet.C2SLeyCastPacket());
    }

    public static void tick() {
        if (!isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            mode = S2CLeyFightPacket.MODE_END;
            return;
        }
        ticks++;
        shownTension += (tension - shownTension) * 0.4F;
        shownStamina += (stamina - shownStamina) * 0.2F;
        if (!isFighting()) {
            return;
        }
        suppressVanillaInput(mc);
        freezePlayerMotion(mc.player);
        NetworkHandler.CHANNEL.sendToServer(new C2SLeyFightInputPacket(isReeling(mc), steer(mc)));
    }

    private static boolean isReeling(Minecraft mc) {
        return mc.screen == null && mc.getWindow() != null
                && GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
    }

    private static int steer(Minecraft mc) {
        if (mc.screen != null || mc.getWindow() == null) {
            return 0;
        }
        long win = mc.getWindow().getWindow();
        boolean left = InputConstants.isKeyDown(win, mc.options.keyLeft.getKey().getValue());
        boolean right = InputConstants.isKeyDown(win, mc.options.keyRight.getKey().getValue());
        return left == right ? 0 : left ? -1 : 1;
    }

    public static void suppressVanillaInput(Minecraft mc) {
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
        if (!isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int cx = g.guiWidth() / 2;
        int x0 = cx - BAR_W / 2;
        int y = 24;
        if (!isFighting()) {
            g.drawCenteredString(mc.font, "Ждите поклёвку" + ".".repeat(1 + (ticks / 8) % 3), cx, y, 0xFFE8D9A8);
            return;
        }
        g.drawCenteredString(mc.font, "СТАРЫЙ ЛЕЙ", cx, y - 4, 0xFFF2C14E);

        // выносливость Лея: сколько ему осталось
        int sy = y + 8;
        bar(g, x0, sy, BAR_W, 7, 0xFF1A1208);
        int sw = Math.round(BAR_W * Math.max(0F, Math.min(100F, shownStamina)) / 100F);
        g.fill(x0 + 1, sy + 1, x0 + 1 + Math.max(0, sw - 2), sy + 6, 0xFFE0A030);
        g.drawString(mc.font, "Лей", x0 - 24, sy - 1, 0xFFE8D9A8);

        // натяжение: слабина, зелёная зона, красная черта
        int ty = sy + 14;
        bar(g, x0, ty, BAR_W, 11, 0xFF101820);
        zone(g, x0, ty, 0, (float) LeyFight.SAFE_LOW, 0xFF2A4258);
        zone(g, x0, ty, (float) LeyFight.SAFE_LOW, (float) LeyFight.SAFE_HIGH, 0xFF2E8B57);
        zone(g, x0, ty, (float) LeyFight.SAFE_HIGH, 100F, 0xFFA83232);
        int mx = x0 + 1 + Math.round((BAR_W - 2) * Math.max(0F, Math.min(100F, shownTension)) / 100F);
        g.fill(mx - 1, ty - 3, mx + 2, ty + 14, 0xFFFFF4D6);
        g.drawString(mc.font, "Леска", x0 - 32, ty + 1, 0xFFE8D9A8);

        String hint;
        int color;
        if (phase == LeyFight.Phase.CALM.ordinal()) {
            hint = shownTension > LeyFight.SAFE_HIGH - 8 ? "Отпустите ПКМ, леска горячая" : "Держите ПКМ: мотайте";
            color = shownTension > LeyFight.SAFE_HIGH - 8 ? 0xFFFFB347 : 0xFF9FE3B0;
        } else {
            String counter = surgeDir < 0 ? "D" : "A";
            boolean warn = phase == LeyFight.Phase.WARN.ordinal();
            String head = surgeDir < 0 ? "« РЫВОК ВЛЕВО" : "РЫВОК ВПРАВО »";
            hint = (warn ? "Внимание! " : "") + head + " · отпустите ПКМ и жмите " + counter;
            boolean blink = (ticks / 4) % 2 == 0;
            color = blink ? 0xFFFF5A4D : 0xFFFFD0C8;
        }
        g.drawCenteredString(mc.font, hint, cx, ty + 20, color);
    }

    private static void bar(GuiGraphics g, int x, int y, int w, int h, int fill) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.fill(x, y, x + w, y + h, fill);
    }

    private static void zone(GuiGraphics g, int x0, int y, float from, float to, int color) {
        int a = x0 + 1 + Math.round((BAR_W - 2) * from / 100F);
        int b = x0 + 1 + Math.round((BAR_W - 2) * to / 100F);
        g.fill(a, y + 1, b, y + 10, color);
    }
}
