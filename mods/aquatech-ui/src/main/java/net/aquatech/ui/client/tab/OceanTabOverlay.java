package net.aquatech.ui.client.tab;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.gui.widget.AquaBadge;
import net.aquatech.ui.client.gui.widget.AquaGlassPanel;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.common.ServerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class OceanTabOverlay {
    private static final int OUTER_PADDING = 14;
    private static final int HEADER_HEIGHT = 52;
    private static final int FOOTER_HEIGHT = 20;
    private static final int CARD_HEIGHT = 40;
    private static final int CARD_GAP = 7;
    private static final int IDEAL_CARD_WIDTH = 218;
    private static double scroll;

    private OceanTabOverlay() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        ServerStats stats = ClientUiState.stats();
        List<PlayerProfile> profiles = ClientUiState.profiles();
        var font = mc.font;

        graphics.fill(0, 0, screenW, screenH, 0x52050D12);

        int columns = calculateColumns(screenW, profiles.size());
        int totalRows = Math.max(1, (profiles.size() + columns - 1) / columns);
        int maxVisibleRows = Math.max(1,
                (screenH - 40 - HEADER_HEIGHT - FOOTER_HEIGHT - OUTER_PADDING * 2) / (CARD_HEIGHT + CARD_GAP));
        int visibleRows = Math.min(totalRows, maxVisibleRows);

        int panelW = Math.min(
                screenW - 24,
                OUTER_PADDING * 2 + columns * IDEAL_CARD_WIDTH + (columns - 1) * CARD_GAP
        );
        int panelH = HEADER_HEIGHT + FOOTER_HEIGHT + OUTER_PADDING * 2
                + visibleRows * CARD_HEIGHT + Math.max(0, visibleRows - 1) * CARD_GAP;
        int panelX = (screenW - panelW) / 2;
        int panelY = (screenH - panelH) / 2;
        AquaGlassPanel.draw(graphics, panelX, panelY, panelW, panelH, AquaGlassPanel.FILL, AquaGlassPanel.BORDER, 5, true);

        int contentLeft = panelX + OUTER_PADDING;
        int contentRight = panelX + panelW - OUTER_PADDING;
        AquaFontRenderer.drawHeader(graphics, font, "AquaTech", contentLeft, panelY + 11, UiDraw.COLOR_ACCENT);
        AquaFontRenderer.draw(graphics, font, "Ocean Network · Сборка v2.9.47", contentLeft, panelY + 24, UiDraw.COLOR_MUTED);

        String onlineText = stats.online() + "/" + Math.max(stats.online(), stats.maxPlayers());
        int onlineColor = stats.online() > 0 ? 0xFF63E6A5 : UiDraw.COLOR_MUTED;
        int onlineW = AquaFontRenderer.width(font, onlineText);
        int dotX = contentRight - onlineW - 8;
        int dotY = panelY + 14;
        graphics.fill(dotX, dotY, dotX + 4, dotY + 4, onlineColor);
        AquaFontRenderer.draw(graphics, font, onlineText, contentRight - onlineW, panelY + 11, onlineColor);

        String staff = stats.staffOnline() + " команда";
        AquaFontRenderer.draw(graphics, font, staff, contentRight - AquaFontRenderer.width(font, staff), panelY + 24, UiDraw.COLOR_MUTED);
        graphics.fill(contentLeft, panelY + HEADER_HEIGHT - 5, contentRight, panelY + HEADER_HEIGHT - 4, 0x443A7892);

        int listTop = panelY + HEADER_HEIGHT;
        int listBottom = panelY + panelH - FOOTER_HEIGHT - OUTER_PADDING;
        int listHeight = listBottom - listTop;
        int colWidth = (panelW - OUTER_PADDING * 2 - (columns - 1) * CARD_GAP) / columns;
        int contentHeight = totalRows * CARD_HEIGHT + Math.max(0, totalRows - 1) * CARD_GAP;
        int maxScroll = Math.max(0, contentHeight - listHeight);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        graphics.enableScissor(contentLeft, listTop, contentRight, listBottom);
        for (int i = 0; i < profiles.size(); i++) {
            PlayerProfile profile = profiles.get(i);
            int col = i % columns;
            int row = i / columns;
            int x = contentLeft + col * (colWidth + CARD_GAP);
            int y = listTop + row * (CARD_HEIGHT + CARD_GAP) - (int) scroll;
            if (y + CARD_HEIGHT < listTop || y > listBottom) {
                continue;
            }
            renderEntry(graphics, font, profile, x, y, colWidth);
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            int barX = panelX + panelW - 6;
            graphics.fill(barX, listTop, barX + 2, listBottom, 0x44000000);
            int thumbH = Math.max(14, listHeight * listHeight / contentHeight);
            int thumbY = listTop + (int) ((scroll / (double) maxScroll) * (listHeight - thumbH));
            graphics.fill(barX, thumbY, barX + 2, thumbY + thumbH, UiDraw.COLOR_ACCENT);
        }

        // Footer: only hint, no website link
        String hint = maxScroll > 0 ? "колесо мыши — прокрутка" : "удерживайте TAB";
        AquaFontRenderer.draw(graphics, font, hint, contentRight - AquaFontRenderer.width(font, hint),
                panelY + panelH - FOOTER_HEIGHT + 3, UiDraw.COLOR_MUTED);
    }

    public static void scroll(double delta) {
        scroll -= delta * 18;
    }

    public static void resetScroll() {
        scroll = 0;
    }

    private static int calculateColumns(int screenWidth, int playerCount) {
        int byPlayers = playerCount <= 5 ? 1 : playerCount <= 12 ? 2 : 3;
        int byWidth = screenWidth < 500 ? 1 : screenWidth < 750 ? 2 : 3;
        return Math.max(1, Math.min(byPlayers, byWidth));
    }

    private static void renderEntry(GuiGraphics graphics, net.minecraft.client.gui.Font font, PlayerProfile profile, int x, int y, int width) {
        int rankColor = UiDraw.rankColor(profile.rankId());
        AquaGlassPanel.drawCard(graphics, x, y, width, CARD_HEIGHT, rankColor);
        UiDraw.drawPlayerHead(graphics, profile.uuid(), profile.name(), x + 8, y + 7, 26);

        int textX = x + 42;
        int right = x + width - 8;

        int realPing = profile.ping();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            var playerInfo = mc.getConnection().getPlayerInfo(profile.uuid());
            if (playerInfo != null) {
                realPing = playerInfo.getLatency();
            }
        }
        String ping = Math.max(0, realPing) + "ms";
        int pingColor = pingColor(realPing);

        String name = AquaFontRenderer.fit(font, profile.name(), Math.max(24, right - textX - AquaFontRenderer.width(font, ping) - 14));
        AquaFontRenderer.draw(graphics, font, name, textX, y + 8, UiDraw.COLOR_TEXT);

        int badgeX = textX;
        String rankRaw = profile.rankDisplay();
        if (rankRaw == null || rankRaw.isBlank()) {
            rankRaw = profile.rankId();
        }
        rankRaw = rankRaw.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim();
        if (rankRaw.isBlank()) {
            rankRaw = "ИГРОК";
        }
        String rank = AquaFontRenderer.fit(font, rankRaw.toUpperCase(), Math.max(24, right - textX - 50));
        AquaBadge.draw(graphics, font, badgeX, y + 22, rank, rankColor);
        graphics.fill(right - AquaFontRenderer.width(font, ping) - 6, y + 10, right - AquaFontRenderer.width(font, ping) - 3, y + 13, pingColor);
        AquaFontRenderer.draw(graphics, font, ping, right - AquaFontRenderer.width(font, ping), y + 7, pingColor);
    }

    private static int pingColor(int ping) {
        if (ping < 0) {
            return UiDraw.COLOR_MUTED;
        }
        if (ping <= 80) {
            return 0xFF63E6A5;
        }
        if (ping <= 160) {
            return 0xFFFFD166;
        }
        return 0xFFFF6B6B;
    }

}
