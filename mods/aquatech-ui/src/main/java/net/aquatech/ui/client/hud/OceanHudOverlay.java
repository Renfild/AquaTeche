package net.aquatech.ui.client.hud;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.common.ModClientConfig;
import net.aquatech.ui.server.PressureBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;

/** Правый боковой HUD: глубина, давление и кислород — давление считается локально каждый кадр. */
public final class OceanHudOverlay {
    private OceanHudOverlay() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        if (!ModClientConfig.HUD_VISIBLE.get()) {
            return;
        }
        if (mc.screen != null || ClientUiState.tabOpen()) {
            return;
        }

        int w = Math.max(110, ModClientConfig.HUD_WIDTH.get());
        float scale = (float) ModClientConfig.HUD_SCALE.get().doubleValue();
        int marginRight = ModClientConfig.HUD_MARGIN_RIGHT.get();
        int marginTop = ModClientConfig.HUD_MARGIN_TOP.get();
        boolean showPressure = ModClientConfig.SHOW_PRESSURE.get();
        int h = showPressure ? 84 : 58;

        int screenX = graphics.guiWidth() - Math.round(w * scale) - marginRight;
        int screenY = marginTop;

        // Live local calc — не ждём сетевой sync профиля (там давление отставало от урона).
        PressureBridge.PressureInfo live = PressureBridge.fromPlayer(player);
        boolean inWater = live.inWater()
                || player.isInWater()
                || player.isEyeInFluid(FluidTags.WATER);
        int depth = inWater ? Math.max(0, PressureBridge.SEA_LEVEL_Y - player.blockPosition().getY()) : 0;
        int pressure = inWater ? live.effective() : 0;
        int tolerance = live.tolerance();

        // Fallback if capability not loaded yet on client
        if (inWater && live.depth() == 0 && depth > 0) {
            pressure = Math.max(0, depth - 10);
            tolerance = 10;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(screenX, screenY, 0);
        graphics.pose().scale(scale, scale, 1f);

        graphics.fill(3, 3, w + 3, h + 3, 0x44000000);
        graphics.fill(0, 0, w, h, 0xD10B1F2A);
        graphics.fill(0, 0, w, 1, UiDraw.COLOR_ACCENT);
        graphics.fill(0, 5, 2, 22, UiDraw.COLOR_ACCENT);

        graphics.drawString(mc.font, AquaFontRenderer.header("Погружение"), 8, 6, UiDraw.COLOR_ACCENT, false);
        graphics.fill(8, 18, w - 8, 19, 0x443A7892);

        String depthStr = inWater ? depth + " м" : "поверхность";
        drawRightValue(graphics, 0, 24, w, "Глубина", depthStr, UiDraw.COLOR_TEXT);

        int y = 35;
        if (showPressure) {
            String pressureValue;
            int color;
            if (!inWater) {
                pressureValue = "норма";
                color = UiDraw.COLOR_ACCENT;
            } else {
                pressureValue = pressure + " (" + pressureLabel(pressure) + ")";
                color = pressureColor(pressure);
            }
            drawRightValue(graphics, 0, y, w, "Давление", pressureValue, color);
            y += 11;
            String reserve = inWater ? ("запас " + tolerance + " м") : "—";
            drawRightValue(graphics, 0, y, w, "Защита", reserve, UiDraw.COLOR_MUTED);
            y += 11;
        }

        int maxAir = Math.max(1, player.getMaxAirSupply());
        int airPercent = Math.max(0, Math.min(100, player.getAirSupply() * 100 / maxAir));
        drawRightValue(graphics, 0, y, w, "Кислород", airPercent + "%", airColor(airPercent));

        int barLeft = 8;
        int barRight = w - 8;
        int barY = y + 11;
        graphics.fill(barLeft, barY, barRight, barY + 3, 0x55234350);
        int airRight = barLeft + (barRight - barLeft) * airPercent / 100;
        graphics.fill(barLeft, barY, airRight, barY + 3, airColor(airPercent));

        graphics.pose().popPose();
    }

    private static void drawRightValue(
            GuiGraphics graphics, int panelX, int y, int panelWidth,
            String label, String value, int valueColor
    ) {
        Minecraft mc = Minecraft.getInstance();
        AquaFontRenderer.draw(graphics, mc.font, label, panelX + 8, y, UiDraw.COLOR_MUTED);
        int maxVal = panelWidth - 16 - AquaFontRenderer.width(mc.font, label) - 6;
        String fitted = AquaFontRenderer.fit(mc.font, value, Math.max(20, maxVal));
        AquaFontRenderer.draw(graphics, mc.font, fitted, panelX + panelWidth - 8 - AquaFontRenderer.width(mc.font, fitted), y, valueColor);
    }

    private static String pressureLabel(int pressure) {
        if (pressure <= 0) return "норма";
        if (pressure <= 5) return "лёгкое";
        if (pressure <= 10) return "среднее";
        if (pressure <= 15) return "высокое";
        if (pressure <= 25) return "критич.";
        return "экстрим";
    }

    private static int pressureColor(int pressure) {
        if (pressure <= 0) return UiDraw.COLOR_ACCENT;
        if (pressure <= 5) return 0xFFAAFFAA;
        if (pressure <= 10) return 0xFFFFFF88;
        if (pressure <= 15) return 0xFFFFAA55;
        return 0xFFFF5555;
    }

    private static int airColor(int percent) {
        if (percent > 55) return UiDraw.COLOR_ACCENT;
        if (percent > 25) return 0xFFFFD166;
        return 0xFFFF6B6B;
    }
}
