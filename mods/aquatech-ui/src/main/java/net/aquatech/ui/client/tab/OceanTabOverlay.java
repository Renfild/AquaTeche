package net.aquatech.ui.client.tab;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.LumenGfx;
import net.aquatech.ui.client.render.LumenIcons;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.client.theme.LumenTheme;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.common.ServerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * Modern TAB Overlay matching the AquaLumen design system:
 * 1. Clean dark frosted panel with subtle luminous accent borders.
 * 2. Header with brand title, live player count pill, and server status.
 * 3. Responsive player grid with stylized rank pills and ping indicators.
 * 4. Concise footer with navigation hints.
 */
public final class OceanTabOverlay {
    private static final int OUTER_PADDING = 14;
    private static final int HEADER_HEIGHT = 52;
    private static final int FOOTER_HEIGHT = 24;
    private static final int CARD_HEIGHT = 40;
    private static final int CARD_GAP = 6;
    private static final int IDEAL_CARD_WIDTH = 220;
    private static double scroll;

    private OceanTabOverlay() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        LumenTheme theme = LumenTheme.get();
        Font font = mc.font;

        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        ServerStats stats = ClientUiState.stats();
        List<PlayerProfile> profiles = ClientUiState.profiles();

        // Dark ambient background backdrop
        graphics.fill(0, 0, screenW, screenH, 0x85070C12);

        int columns = calculateColumns(screenW, profiles.size());
        int totalRows = Math.max(1, (profiles.size() + columns - 1) / columns);
        int maxVisibleRows = Math.max(1,
                (screenH - 40 - HEADER_HEIGHT - FOOTER_HEIGHT - OUTER_PADDING * 2) / (CARD_HEIGHT + CARD_GAP));
        int visibleRows = Math.min(totalRows, maxVisibleRows);

        int targetW = columns * IDEAL_CARD_WIDTH + (columns - 1) * CARD_GAP + OUTER_PADDING * 2;
        int panelW = Math.min(screenW - 24, Math.max(300, targetW));
        int panelH = HEADER_HEIGHT + FOOTER_HEIGHT + OUTER_PADDING * 2
                + visibleRows * CARD_HEIGHT + Math.max(0, visibleRows - 1) * CARD_GAP;
        int panelX = (screenW - panelW) / 2;
        int panelY = (screenH - panelH) / 2;

        // Modal Panel Surface & Glass Border
        int cardBg = theme.panelAlpha(0.94f);
        LumenGfx.roundedRect(graphics, panelX, panelY, panelW, panelH, 8, cardBg);
        LumenGfx.outline(graphics, panelX, panelY, panelW, panelH, 8, theme.border());
        LumenGfx.glow(graphics, panelX, panelY, panelW, panelH, 8, theme.accentAlpha(0.14f), 3);

        int contentLeft = panelX + OUTER_PADDING;
        int contentRight = panelX + panelW - OUTER_PADDING;

        // ═════════════════════════════════════════════════════════════════════════
        // HEADER
        // ═════════════════════════════════════════════════════════════════════════
        AquaFontRenderer.drawHeader(graphics, font, "AQUATECH", contentLeft, panelY + 11, theme.accent());
        AquaFontRenderer.draw(graphics, font, "OCEAN NETWORK", contentLeft, panelY + 24, theme.textDim());

        // Online Pill Badge
        int onlineCount = Math.max(1, stats.online());
        int maxCount = Math.max(stats.online(), stats.maxPlayers() > 0 ? stats.maxPlayers() : 100);
        String onlineText = onlineCount + " / " + maxCount + " ОНЛАЙН";
        int onlineW = AquaFontRenderer.width(font, onlineText);
        int pillW = onlineW + 16;
        int pillH = 15;
        int pillX = contentRight - pillW;
        int pillY = panelY + 11;

        LumenGfx.roundedRect(graphics, pillX, pillY, pillW, pillH, 7, 0x244CD08A);
        LumenGfx.outline(graphics, pillX, pillY, pillW, pillH, 7, 0x554CD08A);
        graphics.fill(pillX + 6, pillY + 5, pillX + 10, pillY + 9, theme.success());
        AquaFontRenderer.draw(graphics, font, onlineText, pillX + 13, pillY + 3, theme.success());

        // Subtitle Info (Staff or Network status)
        String subInfo = stats.staffOnline() > 0 ? (stats.staffOnline() + " персонал в сети") : "Сервер работает стабильно";
        AquaFontRenderer.draw(graphics, font, subInfo, contentRight - AquaFontRenderer.width(font, subInfo), panelY + 28, theme.textDim());

        // Header Gradient Divider
        LumenGfx.gradientRoundedH(graphics, contentLeft, panelY + HEADER_HEIGHT - 6, contentRight - contentLeft, 1, 0,
                theme.accentAlpha(0.40f), 0x083B9DFF);

        // ═════════════════════════════════════════════════════════════════════════
        // PLAYER CARDS GRID
        // ═════════════════════════════════════════════════════════════════════════
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
            renderEntry(graphics, font, profile, x, y, colWidth, theme);
        }
        graphics.disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            int barX = panelX + panelW - 6;
            LumenGfx.roundedRect(graphics, barX, listTop, 3, listHeight, 1.5F, 0x22FFFFFF);
            int thumbH = Math.max(14, listHeight * listHeight / contentHeight);
            int thumbY = listTop + (int) ((scroll / (double) maxScroll) * (listHeight - thumbH));
            LumenGfx.roundedRect(graphics, barX, thumbY, 3, thumbH, 1.5F, theme.accent());
        }

        // ═════════════════════════════════════════════════════════════════════════
        // FOOTER
        // ═════════════════════════════════════════════════════════════════════════
        int footerY = panelY + panelH - FOOTER_HEIGHT + 7;
        String domain = "aquateche.store";
        int domainW = AquaFontRenderer.width(font, domain);
        AquaFontRenderer.draw(graphics, font, domain, contentLeft, footerY, theme.textDim());

        String hint = maxScroll > 0 ? "Колесо — прокрутка · F4 — Меню" : "F4 — Меню сервера";
        int hintW = AquaFontRenderer.width(font, hint);

        if (contentRight - hintW > contentLeft + domainW + 16) {
            AquaFontRenderer.draw(graphics, font, hint, contentRight - hintW, footerY, theme.textDim());
        } else {
            String shortHint = "F4 — Меню";
            int shortW = AquaFontRenderer.width(font, shortHint);
            if (contentRight - shortW > contentLeft + domainW + 8) {
                AquaFontRenderer.draw(graphics, font, shortHint, contentRight - shortW, footerY, theme.textDim());
            }
        }
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

    private static void renderEntry(GuiGraphics graphics, Font font, PlayerProfile profile, int x, int y, int width, LumenTheme theme) {
        int rankColor = LumenTheme.getRankColor(profile.rankId());

        // Card background & rank border
        LumenGfx.roundedRect(graphics, x, y, width, CARD_HEIGHT, 5, theme.raised() & 0x88FFFFFF);
        LumenGfx.outline(graphics, x, y, width, CARD_HEIGHT, 5, rankColor & 0x38FFFFFF);

        // Left vertical rank indicator bar
        LumenGfx.roundedRect(graphics, x, y, 3, CARD_HEIGHT, 1.5F, rankColor);

        // Avatar 26x26
        int avX = x + 8;
        int avY = y + 7;
        int avSize = 26;
        UiDraw.drawPlayerHead(graphics, profile.uuid(), profile.name(), avX, avY, avSize);
        LumenGfx.outline(graphics, avX - 1, avY - 1, avSize + 2, avSize + 2, 2, rankColor & 0x55FFFFFF);
        // Mint online dot
        graphics.fill(avX + avSize - 3, avY + avSize - 3, avX + avSize + 1, avY + avSize + 1, theme.success());

        int textX = x + 40;
        int right = x + width - 8;

        // Ping calculation
        int realPing = profile.ping();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            var playerInfo = mc.getConnection().getPlayerInfo(profile.uuid());
            if (playerInfo != null) {
                realPing = playerInfo.getLatency();
            }
        }
        String ping = Math.max(0, realPing) + "ms";
        int pingCol = pingColor(realPing, theme);
        int pingW = AquaFontRenderer.width(font, ping);

        // Player Name
        String name = AquaFontRenderer.fit(font, profile.name(), Math.max(24, right - textX - pingW - 14));
        AquaFontRenderer.draw(graphics, font, name, textX, y + 7, theme.text());

        // Rank Pill
        String rankRaw = profile.rankDisplay();
        if (rankRaw == null || rankRaw.isBlank()) {
            rankRaw = profile.rankId();
        }
        rankRaw = rankRaw.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim().toUpperCase();
        if (rankRaw.isBlank()) {
            rankRaw = "ИГРОК";
        }
        String rank = AquaFontRenderer.fit(font, rankRaw, Math.max(24, right - textX - pingW - 12));
        int rankBadgeW = AquaFontRenderer.width(font, rank) + 8;
        int rankBadgeH = 12;
        int rankBadgeY = y + 21;

        LumenGfx.roundedRect(graphics, textX, rankBadgeY, rankBadgeW, rankBadgeH, 3, rankColor & 0x22FFFFFF);
        LumenGfx.outline(graphics, textX, rankBadgeY, rankBadgeW, rankBadgeH, 3, rankColor & 0x66FFFFFF);
        AquaFontRenderer.draw(graphics, font, rank, textX + 4, rankBadgeY + 2, rankColor);

        // Ping text & indicator
        int pingX = right - pingW;
        graphics.fill(pingX - 6, y + 10, pingX - 2, y + 14, pingCol);
        AquaFontRenderer.draw(graphics, font, ping, pingX, y + 8, pingCol);
    }

    private static int pingColor(int ping, LumenTheme theme) {
        if (ping < 0) {
            return theme.textDim();
        }
        if (ping <= 80) {
            return theme.success();
        }
        if (ping <= 160) {
            return theme.gold();
        }
        return theme.danger();
    }
}
