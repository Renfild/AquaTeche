package net.aquatech.ui.client.hud;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.LumenGfx;
import net.aquatech.ui.client.render.LumenIcons;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.client.theme.LumenTheme;
import net.aquatech.ui.common.ModClientConfig;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.server.PressureBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;

/**
 * Modern In-Game HUD / Sidebar Overlay designed to 100% match AquaLumen aesthetic:
 * 1. Profile & Status Card: Avatar, Nickname, Rank Pill, Balance with vector icon, Playtime with vector icon.
 * 2. Ocean & Diving Card: Status, Depth, Pressure, Protection & smooth Gradient Oxygen Bar.
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

        LumenTheme theme = LumenTheme.get();
        Font font = mc.font;

        int w = Math.max(148, ModClientConfig.HUD_WIDTH.get());
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

        int rankColor = LumenTheme.getRankColor(profile != null ? profile.rankId() : "player");

        graphics.pose().pushPose();
        graphics.pose().translate(screenX, screenY, 0);
        graphics.pose().scale(scale, scale, 1f);

        // ═════════════════════════════════════════════════════════════════════════
        // 1. TOP PROFILE & STATS CARD
        // ═════════════════════════════════════════════════════════════════════════
        int profileCardH = 76;
        int cardBg = theme.panelAlpha(0.90f);

        // Background & crisp border
        LumenGfx.roundedRect(graphics, 0, 0, w, profileCardH, 6, cardBg);
        LumenGfx.outline(graphics, 0, 0, w, profileCardH, 6, theme.border());
        LumenGfx.glow(graphics, 0, 0, w, 2, 6, theme.accentAlpha(0.12f), 2);

        // Avatar 24x24
        int avX = 8;
        int avY = 8;
        int avSize = 24;
        UiDraw.drawPlayerHead(graphics, player.getUUID(), player.getGameProfile().getName(), avX, avY, avSize);
        // Soft outline around avatar
        LumenGfx.outline(graphics, avX - 1, avY - 1, avSize + 2, avSize + 2, 2, theme.accentAlpha(0.4f));
        // Online status mint dot
        graphics.fill(avX + avSize - 3, avY + avSize - 3, avX + avSize + 1, avY + avSize + 1, theme.success());

        // Nickname
        int textLeft = avX + avSize + 8;
        String name = AquaFontRenderer.fit(font, player.getGameProfile().getName(), w - textLeft - 8);
        AquaFontRenderer.draw(graphics, font, name, textLeft, avY + 1, theme.text());

        // Rank Pill Badge
        int rankBadgeW = AquaFontRenderer.width(font, rankRaw) + 8;
        int rankBadgeH = 11;
        int rankBadgeY = avY + 12;
        LumenGfx.roundedRect(graphics, textLeft, rankBadgeY, rankBadgeW, rankBadgeH, 3, rankColor & 0x22FFFFFF);
        LumenGfx.outline(graphics, textLeft, rankBadgeY, rankBadgeW, rankBadgeH, 3, rankColor & 0x66FFFFFF);
        AquaFontRenderer.draw(graphics, font, rankRaw, textLeft + 4, rankBadgeY + 2, rankColor);

        // Gradient Divider
        LumenGfx.gradientRoundedH(graphics, 8, 36, w - 16, 1, 0, theme.accentAlpha(0.35f), 0x053B9DFF);

        // Metric Rows
        int bal = ClientUiState.sessionBalance();
        drawLumenMetricRow(graphics, font, 6, 40, w - 12, 15, LumenIcons.Icon.COIN, "Баланс", bal + " AQ", theme.gold(), theme);
        drawLumenMetricRow(graphics, font, 6, 57, w - 12, 15, LumenIcons.Icon.CLOCK, "В игре", ClientUiState.getPlaytimeFormatted(), theme.text(), theme);

        // ═════════════════════════════════════════════════════════════════════════
        // 2. BOTTOM DIVING & OCEAN CARD
        // ═════════════════════════════════════════════════════════════════════════
        int immersionY = profileCardH + 6;
        int immersionH = showPressure ? 82 : 60;

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

        LumenGfx.roundedRect(graphics, 0, immersionY, w, immersionH, 6, cardBg);
        LumenGfx.outline(graphics, 0, immersionY, w, immersionH, 6, theme.border());

        // Header with Wave vector icon
        LumenIcons.draw(graphics, LumenIcons.Icon.WAVE, 8, immersionY + 6, 10, theme.accent());
        AquaFontRenderer.draw(graphics, font, "ПОГРУЖЕНИЕ", 22, immersionY + 7, theme.accent());
        LumenGfx.gradientRoundedH(graphics, 8, immersionY + 18, w - 16, 1, 0, theme.accentAlpha(0.35f), 0x053B9DFF);

        String depthStr = inWater ? depth + " м" : "поверхность";
        drawStatRow(graphics, font, 8, immersionY + 23, w - 16, "Глубина", depthStr, theme.text(), theme);

        int curY = immersionY + 34;
        if (showPressure) {
            String pressureValue;
            int color;
            if (!inWater) {
                pressureValue = "норма";
                color = theme.accent();
            } else {
                pressureValue = pressure + " (" + pressureLabel(pressure) + ")";
                color = pressureColor(pressure, theme);
            }
            drawStatRow(graphics, font, 8, curY, w - 16, "Давление", pressureValue, color, theme);
            curY += 11;
            String reserve = inWater ? (tolerance + " м") : "—";
            drawStatRow(graphics, font, 8, curY, w - 16, "Защита", reserve, theme.textDim(), theme);
            curY += 11;
        }

        int maxAir = Math.max(1, player.getMaxAirSupply());
        int airPercent = Math.max(0, Math.min(100, player.getAirSupply() * 100 / maxAir));
        drawStatRow(graphics, font, 8, curY, w - 16, "Кислород", airPercent + "%", theme.accent(), theme);

        // Smooth Rounded Gradient Oxygen Bar
        int barLeft = 8;
        int barWidth = w - 16;
        int barY = curY + 10;
        LumenGfx.progressBar(graphics, barLeft, barY, barWidth, 3, airPercent / 100.0F,
                0x3316202C, theme.accent(), theme.accentAlt());

        graphics.pose().popPose();
    }

    private static void drawLumenMetricRow(GuiGraphics graphics, Font font, int x, int y, int rowW, int rowH,
                                           LumenIcons.Icon icon, String label, String value, int valColor, LumenTheme theme) {
        LumenGfx.roundedRect(graphics, x, y, rowW, rowH, 4, theme.raised() & 0x77FFFFFF);
        LumenGfx.outline(graphics, x, y, rowW, rowH, 4, theme.borderMuted());

        // Vector icon
        LumenIcons.draw(graphics, icon, x + 5, y + 2.5F, 10, valColor);

        // Label
        AquaFontRenderer.draw(graphics, font, label, x + 19, y + 3, theme.textDim());

        // Value
        String fitted = AquaFontRenderer.fit(font, value, 60);
        int vW = AquaFontRenderer.width(font, fitted);
        AquaFontRenderer.draw(graphics, font, fitted, x + rowW - vW - 5, y + 3, valColor);
    }

    private static void drawStatRow(GuiGraphics graphics, Font font, int x, int y, int rowW, String label, String value, int valColor, LumenTheme theme) {
        AquaFontRenderer.draw(graphics, font, label, x, y, theme.textDim());
        int maxVal = rowW - AquaFontRenderer.width(font, label) - 6;
        String fitted = AquaFontRenderer.fit(font, value, Math.max(20, maxVal));
        int vW = AquaFontRenderer.width(font, fitted);
        AquaFontRenderer.draw(graphics, font, fitted, x + rowW - vW, y, valColor);
    }

    private static String pressureLabel(int pressure) {
        if (pressure <= 0) return "норма";
        if (pressure <= 5) return "лёгкое";
        if (pressure <= 10) return "среднее";
        if (pressure <= 15) return "высокое";
        if (pressure <= 25) return "критич.";
        return "экстрим";
    }

    private static int pressureColor(int pressure, LumenTheme theme) {
        if (pressure <= 0) return theme.accent();
        if (pressure <= 5) return theme.success();
        if (pressure <= 10) return theme.gold();
        if (pressure <= 15) return 0xFFFFAA55;
        return theme.danger();
    }
}
