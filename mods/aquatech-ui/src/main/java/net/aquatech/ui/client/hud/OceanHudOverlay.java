package net.aquatech.ui.client.hud;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.gui.widget.AquaBadge;
import net.aquatech.ui.client.gui.widget.AquaGlassPanel;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.common.ModClientConfig;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.server.PressureBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;

/**
 * In-game right-side HUD overlay:
 * 1) Top Profile card (avatar, nickname, rank, coins, playtime).
 * 2) Bottom Immersion card (depth, pressure, tolerance, oxygen bar).
 */
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

        int w = Math.max(140, ModClientConfig.HUD_WIDTH.get());
        float scale = (float) ModClientConfig.HUD_SCALE.get().doubleValue();
        int marginRight = ModClientConfig.HUD_MARGIN_RIGHT.get();
        int marginTop = ModClientConfig.HUD_MARGIN_TOP.get();
        boolean showPressure = ModClientConfig.SHOW_PRESSURE.get();

        int screenX = graphics.guiWidth() - Math.round(w * scale) - marginRight;
        int screenY = marginTop;

        PlayerProfile profile = ClientUiState.profile(player.getUUID());
        String rankId = profile != null ? profile.rankId() : "default";
        String rankRaw = profile != null ? profile.rankDisplay() : "ИГРОК";
        if (rankRaw == null || rankRaw.isBlank()) rankRaw = "ИГРОК";
        rankRaw = rankRaw.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim().toUpperCase();
        if (rankRaw.isBlank()) rankRaw = "ИГРОК";
        int rankColor = UiDraw.rankColor(rankId);

        graphics.pose().pushPose();
        graphics.pose().translate(screenX, screenY, 0);
        graphics.pose().scale(scale, scale, 1f);

        // ═════════════════════════════════════════════════════════════════════════
        // 1. TOP PROFILE CARD
        // ═════════════════════════════════════════════════════════════════════════
        int profileCardH = 68;
        AquaGlassPanel.draw(graphics, 0, 0, w, profileCardH, 0xD10B1824, rankColor, 3, false);

        // Avatar
        UiDraw.drawPlayerHead(graphics, player.getUUID(), player.getGameProfile().getName(), 6, 6, 26);
        // Online green indicator dot
        graphics.fill(28, 28, 33, 33, 0xFF10B981);

        // Nickname & Rank
        String name = AquaFontRenderer.fit(mc.font, player.getGameProfile().getName(), w - 42);
        AquaFontRenderer.draw(graphics, mc.font, name, 36, 6, 0xFFFFFFFF);
        AquaBadge.draw(graphics, mc.font, 36, 18, rankRaw, rankColor);

        graphics.fill(6, 36, w - 6, 37, 0x443A7892);

        // Coins & Hours played
        int bal = ClientUiState.sessionBalance();
        drawStatRow(graphics, 0, 40, w, "💰 Монеты", String.valueOf(bal), 0xFFFFD166);
        drawStatRow(graphics, 0, 52, w, "⏱ В игре", "1 ч", UiDraw.COLOR_MUTED);

        // ═════════════════════════════════════════════════════════════════════════
        // 2. BOTTOM IMMERSION CARD
        // ═════════════════════════════════════════════════════════════════════════
        int immersionY = profileCardH + 6;
        int immersionH = showPressure ? 80 : 56;

        PressureBridge.PressureInfo live = PressureBridge.fromPlayer(player);
        boolean inWater = live.inWater()
                || player.isInWater()
                || player.isEyeInFluid(FluidTags.WATER);
        int depth = inWater ? Math.max(0, PressureBridge.SEA_LEVEL_Y - player.blockPosition().getY()) : 0;
        int pressure = inWater ? live.effective() : 0;
        int tolerance = live.tolerance();

        if (inWater && live.depth() == 0 && depth > 0) {
            pressure = Math.max(0, depth - 10);
            tolerance = 10;
        }

        AquaGlassPanel.draw(graphics, 0, immersionY, w, immersionH, 0xD10B1F2A, UiDraw.COLOR_ACCENT, 3, false);
        graphics.fill(0, immersionY + 5, 2, immersionY + 20, UiDraw.COLOR_ACCENT);

        graphics.drawString(mc.font, AquaFontRenderer.header("Погружение"), 8, immersionY + 6, UiDraw.COLOR_ACCENT, false);
        graphics.fill(8, immersionY + 18, w - 8, immersionY + 19, 0x443A7892);

        String depthStr = inWater ? depth + " м" : "поверхность";
        drawRightValue(graphics, 0, immersionY + 23, w, "Глубина", depthStr, UiDraw.COLOR_TEXT);

        int curY = immersionY + 34;
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
            drawRightValue(graphics, 0, curY, w, "Давление", pressureValue, color);
            curY += 11;
            String reserve = inWater ? ("запас " + tolerance + " м") : "—";
            drawRightValue(graphics, 0, curY, w, "Защита", reserve, UiDraw.COLOR_MUTED);
            curY += 11;
        }

        int maxAir = Math.max(1, player.getMaxAirSupply());
        int airPercent = Math.max(0, Math.min(100, player.getAirSupply() * 100 / maxAir));
        drawRightValue(graphics, 0, curY, w, "Кислород", airPercent + "%", airColor(airPercent));

        int barLeft = 8;
        int barRight = w - 8;
        int barY = curY + 10;
        graphics.fill(barLeft, barY, barRight, barY + 3, 0x55234350);
        int airRight = barLeft + (barRight - barLeft) * airPercent / 100;
        graphics.fill(barLeft, barY, airRight, barY + 3, airColor(airPercent));

        graphics.pose().popPose();
    }

    private static void drawStatRow(GuiGraphics graphics, int panelX, int y, int panelWidth, String label, String value, int valueColor) {
        Minecraft mc = Minecraft.getInstance();
        AquaFontRenderer.draw(graphics, mc.font, label, panelX + 8, y, UiDraw.COLOR_MUTED);
        String fitted = AquaFontRenderer.fit(mc.font, value, 60);
        AquaFontRenderer.draw(graphics, mc.font, fitted, panelX + panelWidth - 8 - AquaFontRenderer.width(mc.font, fitted), y, valueColor);
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
