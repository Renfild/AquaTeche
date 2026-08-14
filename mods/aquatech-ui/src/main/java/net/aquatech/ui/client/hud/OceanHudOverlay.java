package net.aquatech.ui.client.hud;

import net.aquatech.ui.client.ClientUiState;
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
 * In-game right-side HUD overlay matching https://aquateche.store/embed/hud:
 * 1) Top Profile card (avatar with green dot, nickname, cyan rank, coin balance, playtime).
 * 2) Bottom Immersion card (depth, pressure, tolerance, oxygen bar).
 */
public final class OceanHudOverlay {

    private static final int COLOR_CARD_BG = 0xDE081420;
    private static final int COLOR_CARD_BORDER = 0x5500E5FF;
    private static final int COLOR_METRIC_BG = 0x88040C16;
    private static final int COLOR_METRIC_BORDER = 0x15FFFFFF;
    private static final int COLOR_CYAN = 0xFF00E5FF;
    private static final int COLOR_LABEL = 0xFFCBD5E1;
    private static final int COLOR_MUTED_LABEL = 0xFF94A3B8;
    private static final int COLOR_WHITE = 0xFFFFFFFF;

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
        String rankRaw = profile != null ? profile.rankDisplay() : "ИГРОК";
        if (rankRaw == null || rankRaw.isBlank()) rankRaw = "ИГРОК";
        rankRaw = rankRaw.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim().toUpperCase();
        if (rankRaw.isBlank()) rankRaw = "ИГРОК";

        graphics.pose().pushPose();
        graphics.pose().translate(screenX, screenY, 0);
        graphics.pose().scale(scale, scale, 1f);

        // ═════════════════════════════════════════════════════════════════════════
        // 1. TOP PROFILE CARD (Matching hud.html)
        // ═════════════════════════════════════════════════════════════════════════
        int profileCardH = 72;
        AquaGlassPanel.draw(graphics, 0, 0, w, profileCardH, COLOR_CARD_BG, COLOR_CARD_BORDER, 4, false);

        // Avatar 24x24
        int avX = 8;
        int avY = 8;
        int avSize = 24;
        UiDraw.drawPlayerHead(graphics, player.getUUID(), player.getGameProfile().getName(), avX, avY, avSize);
        // Cyan avatar border
        UiDraw.border(graphics, avX - 1, avY - 1, avSize + 2, avSize + 2, COLOR_CYAN);
        // Online green indicator dot
        graphics.fill(avX + avSize - 4, avY + avSize - 4, avX + avSize + 1, avY + avSize + 1, 0xFF10B981);

        // Nickname & Rank
        int textLeft = avX + avSize + 8;
        String name = AquaFontRenderer.fit(mc.font, player.getGameProfile().getName(), w - textLeft - 8);
        AquaFontRenderer.draw(graphics, mc.font, name, textLeft, avY, COLOR_WHITE);
        AquaFontRenderer.draw(graphics, mc.font, rankRaw, textLeft, avY + 12, COLOR_CYAN);

        // Divider
        graphics.fill(6, 36, w - 6, 37, 0x2200E5FF);

        // Metric Rows
        int bal = ClientUiState.sessionBalance();
        drawMetricRow(graphics, 6, 40, w - 12, 14, "💰 Монеты", String.valueOf(bal), COLOR_WHITE);
        drawMetricRow(graphics, 6, 55, w - 12, 14, "⏱ В игре", "1 ч", COLOR_WHITE);

        // ═════════════════════════════════════════════════════════════════════════
        // 2. BOTTOM DIVING CARD (Matching hud.html)
        // ═════════════════════════════════════════════════════════════════════════
        int immersionY = profileCardH + 8;
        int immersionH = showPressure ? 82 : 58;

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

        AquaGlassPanel.draw(graphics, 0, immersionY, w, immersionH, COLOR_CARD_BG, COLOR_CARD_BORDER, 4, false);

        // Header: "ПОГРУЖЕНИЕ"
        AquaFontRenderer.draw(graphics, mc.font, "ПОГРУЖЕНИЕ", 8, immersionY + 6, COLOR_CYAN);
        graphics.fill(8, immersionY + 17, w - 8, immersionY + 18, 0x2200E5FF);

        String depthStr = inWater ? depth + " м" : "поверхность";
        drawStatRow(graphics, 8, immersionY + 22, w - 16, "Глубина", depthStr, COLOR_WHITE);

        int curY = immersionY + 33;
        if (showPressure) {
            String pressureValue;
            int color;
            if (!inWater) {
                pressureValue = "норма";
                color = COLOR_CYAN;
            } else {
                pressureValue = pressure + " (" + pressureLabel(pressure) + ")";
                color = pressureColor(pressure);
            }
            drawStatRow(graphics, 8, curY, w - 16, "Давление", pressureValue, color);
            curY += 11;
            String reserve = inWater ? ("запас " + tolerance + " м") : "—";
            drawStatRow(graphics, 8, curY, w - 16, "Защита", reserve, COLOR_MUTED_LABEL);
            curY += 11;
        }

        int maxAir = Math.max(1, player.getMaxAirSupply());
        int airPercent = Math.max(0, Math.min(100, player.getAirSupply() * 100 / maxAir));
        drawStatRow(graphics, 8, curY, w - 16, "Кислород", airPercent + "%", COLOR_CYAN);

        // Oxygen bar track
        int barLeft = 8;
        int barRight = w - 8;
        int barY = curY + 10;
        graphics.fill(barLeft, barY, barRight, barY + 3, 0x22FFFFFF);
        int airRight = barLeft + (barRight - barLeft) * airPercent / 100;
        if (airRight > barLeft) {
            graphics.fill(barLeft, barY, airRight, barY + 3, COLOR_CYAN);
        }

        graphics.pose().popPose();
    }

    private static void drawMetricRow(GuiGraphics graphics, int x, int y, int rowW, int rowH, String label, String value, int valColor) {
        Minecraft mc = Minecraft.getInstance();
        graphics.fill(x, y, x + rowW, y + rowH, COLOR_METRIC_BG);
        UiDraw.border(graphics, x, y, rowW, rowH, COLOR_METRIC_BORDER);

        AquaFontRenderer.draw(graphics, mc.font, label, x + 4, y + 3, COLOR_LABEL);
        String fitted = AquaFontRenderer.fit(mc.font, value, 50);
        int vW = AquaFontRenderer.width(mc.font, fitted);
        AquaFontRenderer.draw(graphics, mc.font, fitted, x + rowW - vW - 4, y + 3, valColor);
    }

    private static void drawStatRow(GuiGraphics graphics, int x, int y, int rowW, String label, String value, int valColor) {
        Minecraft mc = Minecraft.getInstance();
        AquaFontRenderer.draw(graphics, mc.font, label, x, y, COLOR_MUTED_LABEL);
        int maxVal = rowW - AquaFontRenderer.width(mc.font, label) - 6;
        String fitted = AquaFontRenderer.fit(mc.font, value, Math.max(20, maxVal));
        int vW = AquaFontRenderer.width(mc.font, fitted);
        AquaFontRenderer.draw(graphics, mc.font, fitted, x + rowW - vW, y, valColor);
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
        if (pressure <= 0) return COLOR_CYAN;
        if (pressure <= 5) return 0xFFAAFFAA;
        if (pressure <= 10) return 0xFFFFFF88;
        if (pressure <= 15) return 0xFFFFAA55;
        return 0xFFFF5555;
    }
}
