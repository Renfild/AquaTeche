package net.aquatech.ui.client.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.LumenGfx;
import net.aquatech.ui.client.theme.LumenTheme;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.common.ServerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Modern Apple Bento Minimal TAB Overlay:
 * Grounded in Apple Human Interface Guidelines (HIG):
 * 1. Restraint & Clarity: Zero visual noise, whisper-quiet borders, no harsh neon.
 * 2. Materials & Depth: Dark frosted glass (obsidian vibrancy) with 1px hairline specular rim.
 * 3. Top Bento Widgets: Brand card with official logo, Server Pulse card, Connection card.
 * 4. Responsive Bento Player Grid: Squircle cards, rank accent dots, avatars, and latency.
 * 5. Minimalist Hairline Footer: Quick links and hotkey navigation hints.
 */
public final class OceanTabOverlay {
    private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("aquatech_ui", "textures/gui/logo.png");

    private static final int OUTER_PADDING = 12;
    private static final int BENTO_HEIGHT = 44;
    private static final int BENTO_GAP = 8;
    private static final int CARD_HEIGHT = 34;
    private static final int CARD_GAP = 6;
    private static final int FOOTER_HEIGHT = 22;

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

        // 1. Full-screen atmospheric backdrop tint (Apple frosted glass effect)
        graphics.fill(0, 0, screenW, screenH, 0x85040810);

        // 2. Responsive column calculation (up to 4 columns)
        int columns = calculateColumns(screenW, profiles.size());
        int idealCardWidth = columns >= 4 ? 135 : columns == 3 ? 150 : 180;
        int totalRows = Math.max(1, (profiles.size() + columns - 1) / columns);

        int maxVisibleRows = Math.max(1,
                (screenH - 40 - BENTO_HEIGHT - FOOTER_HEIGHT - OUTER_PADDING * 2 - 14) / (CARD_HEIGHT + CARD_GAP));
        int visibleRows = Math.min(totalRows, maxVisibleRows);

        int targetW = columns * idealCardWidth + (columns - 1) * CARD_GAP + OUTER_PADDING * 2;
        int panelW = Math.min(screenW - 20, Math.max(340, targetW));
        int panelH = BENTO_HEIGHT + FOOTER_HEIGHT + OUTER_PADDING * 2 + 12
                + visibleRows * CARD_HEIGHT + Math.max(0, visibleRows - 1) * CARD_GAP;
        int panelX = (screenW - panelW) / 2;
        int panelY = (screenH - panelH) / 2;

        // 3. Apple Frosted Glass Container (Obsidian Vibrancy)
        // 225 Alpha (~88%) dark obsidian body
        LumenGfx.roundedRect(graphics, panelX, panelY, panelW, panelH, 14, 0xEE0A101A);
        // 1px Hairline Specular Edge
        LumenGfx.outline(graphics, panelX, panelY, panelW, panelH, 14, 0x22FFFFFF);
        // Specular top light rim
        LumenGfx.gradientRoundedH(graphics, panelX + 16, panelY + 1, panelW - 32, 1, 0, 0x33FFFFFF, 0x06FFFFFF);

        int contentLeft = panelX + OUTER_PADDING;
        int contentRight = panelX + panelW - OUTER_PADDING;
        int bentoW = contentRight - contentLeft;

        // ═════════════════════════════════════════════════════════════════════════
        // TOP BENTO WIDGETS ROW
        // ═════════════════════════════════════════════════════════════════════════
        int bentoY = panelY + OUTER_PADDING;
        renderBentoWidgets(graphics, font, contentLeft, bentoY, bentoW, stats, theme);

        // ═════════════════════════════════════════════════════════════════════════
        // RESPONSIVE PLAYER CARDS GRID
        // ═════════════════════════════════════════════════════════════════════════
        int listTop = bentoY + BENTO_HEIGHT + 10;
        int listBottom = panelY + panelH - FOOTER_HEIGHT - 6;
        int listHeight = listBottom - listTop;
        int colWidth = (bentoW - (columns - 1) * CARD_GAP) / columns;
        int contentHeight = totalRows * CARD_HEIGHT + Math.max(0, totalRows - 1) * CARD_GAP;
        int maxScroll = Math.max(0, contentHeight - listHeight);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        graphics.enableScissor(contentLeft - 2, listTop - 2, contentRight + 2, listBottom + 2);
        for (int i = 0; i < profiles.size(); i++) {
            PlayerProfile profile = profiles.get(i);
            int col = i % columns;
            int row = i / columns;
            int x = contentLeft + col * (colWidth + CARD_GAP);
            int y = listTop + row * (CARD_HEIGHT + CARD_GAP) - (int) scroll;

            if (y + CARD_HEIGHT < listTop || y > listBottom) {
                continue;
            }
            renderBentoCard(graphics, font, profile, x, y, colWidth, theme);
        }
        graphics.disableScissor();

        // Elegant Minimal Scrollbar
        if (maxScroll > 0) {
            int barX = panelX + panelW - 5;
            LumenGfx.roundedRect(graphics, barX, listTop, 2, listHeight, 1.0F, 0x1AFFFFFF);
            int thumbH = Math.max(12, listHeight * listHeight / contentHeight);
            int thumbY = listTop + (int) ((scroll / (double) maxScroll) * (listHeight - thumbH));
            LumenGfx.roundedRect(graphics, barX, thumbY, 2, thumbH, 1.0F, 0x88FFFFFF);
        }

        // ═════════════════════════════════════════════════════════════════════════
        // MINIMALIST HAIRLINE FOOTER
        // ═════════════════════════════════════════════════════════════════════════
        int footerY = panelY + panelH - FOOTER_HEIGHT + 6;
        // 1px Hairline divider
        graphics.fill(contentLeft, panelY + panelH - FOOTER_HEIGHT - 2, contentRight, panelY + panelH - FOOTER_HEIGHT - 1, 0x14FFFFFF);

        String domain = "aquateche.store";
        AquaFontRenderer.draw(graphics, font, domain, contentLeft, footerY, 0xFF64748B);

        String hint = maxScroll > 0 ? "Колесо — прокрутка · [F4] Меню · [TAB] Закрыть" : "[F4] Меню сервера · [TAB] Закрыть";
        int hintW = AquaFontRenderer.width(font, hint);
        if (contentRight - hintW > contentLeft + AquaFontRenderer.width(font, domain) + 16) {
            AquaFontRenderer.draw(graphics, font, hint, contentRight - hintW, footerY, 0xFF64748B);
        }
    }

    private static void renderBentoWidgets(GuiGraphics graphics, Font font, int x, int y, int totalW,
                                           ServerStats stats, LumenTheme theme) {
        // Adapt widgets based on total available width
        if (totalW < 420) {
            // Compact single header bar
            renderGlassCard(graphics, x, y, totalW, BENTO_HEIGHT);
            renderLogo(graphics, x + 8, y + 8, 28);
            AquaFontRenderer.draw(graphics, font, "AquaTech", x + 42, y + 10, 0xFFFFFFFF);
            AquaFontRenderer.draw(graphics, font, stats.online() + " игроков", x + 42, y + 24, 0xFF94A3B8);
            return;
        }

        int gap = BENTO_GAP;
        int w1 = (int) (totalW * 0.38f);
        int w2 = (int) (totalW * 0.33f);
        int w3 = totalW - w1 - w2 - gap * 2;

        int x1 = x;
        int x2 = x1 + w1 + gap;
        int x3 = x2 + w2 + gap;

        // ── Widget 1: Brand Anchor ──
        renderGlassCard(graphics, x1, y, w1, BENTO_HEIGHT);
        renderLogo(graphics, x1 + 10, y + 8, 28);
        AquaFontRenderer.draw(graphics, font, "AquaTech", x1 + 44, y + 9, 0xFFFFFFFF);
        AquaFontRenderer.draw(graphics, font, "Ocean Skyblock · 1.20.1", x1 + 44, y + 24, 0xFF94A3B8);

        // ── Widget 2: Server Pulse ──
        renderGlassCard(graphics, x2, y, w2, BENTO_HEIGHT);
        AquaFontRenderer.draw(graphics, font, "СОСТОЯНИЕ СЕРВЕРА", x2 + 12, y + 8, 0xFF64748B);

        int onlineCount = Math.max(1, stats.online());
        int maxCount = stats.maxPlayers() > 0 ? stats.maxPlayers() : 100;
        String onlineStr = onlineCount + " игроков";
        // Mint indicator dot
        graphics.fill(x2 + 12, y + 26, x2 + 16, y + 30, theme.success());
        AquaFontRenderer.draw(graphics, font, onlineStr, x2 + 20, y + 23, 0xFFF1F5F9);

        float tpsVal = stats.tps() > 0 ? stats.tps() : 20.0F;
        int tpsCol = tpsVal >= 19.0F ? theme.success() : tpsVal >= 16.0F ? theme.gold() : theme.danger();
        String tpsStr = String.format("%.1f TPS", tpsVal);
        int tpsW = AquaFontRenderer.width(font, tpsStr);
        AquaFontRenderer.draw(graphics, font, tpsStr, x2 + w2 - 12 - tpsW, y + 23, tpsCol);

        // ── Widget 3: Connection Latency ──
        renderGlassCard(graphics, x3, y, w3, BENTO_HEIGHT);
        AquaFontRenderer.draw(graphics, font, "ТВОЯ СЕТЬ", x3 + 12, y + 8, 0xFF64748B);

        int ping = 15;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null && mc.player != null) {
            var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) {
                ping = Math.max(0, info.getLatency());
            }
        }
        int pingCol = pingColor(ping, theme);
        String quality = ping <= 60 ? "Отличное" : ping <= 120 ? "Хорошее" : "Высокий пинг";
        String pingStr = ping + " ms · " + quality;
        AquaFontRenderer.draw(graphics, font, pingStr, x3 + 12, y + 23, pingCol);
    }

    private static void renderGlassCard(GuiGraphics graphics, int x, int y, int w, int h) {
        LumenGfx.roundedRect(graphics, x, y, w, h, 8, 0x14FFFFFF);
        LumenGfx.outline(graphics, x, y, w, h, 8, 0x1AFFFFFF);
    }

    private static void renderLogo(GuiGraphics graphics, int x, int y, int size) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(LOGO_TEXTURE, x, y, size, size, 0, 0, 512, 512, 512, 512);
    }

    private static void renderBentoCard(GuiGraphics graphics, Font font, PlayerProfile profile,
                                        int x, int y, int width, LumenTheme theme) {
        int rankColor = LumenTheme.getRankColor(profile.rankId());

        // Card surface (Clean translucent squircle with 1px subtle edge)
        LumenGfx.roundedRect(graphics, x, y, width, CARD_HEIGHT, 7, 0x12FFFFFF);
        LumenGfx.outline(graphics, x, y, width, CARD_HEIGHT, 7, 0x14FFFFFF);

        // Understated Rank Accent Dot (top-left)
        graphics.fill(x + 5, y + 5, x + 8, y + 8, rankColor);

        // Player Head Avatar (20x20 squircle)
        int avX = x + 12;
        int avY = y + 7;
        int avSize = 20;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(profile.uuid());
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            var info = mc.getConnection().getPlayerInfo(profile.uuid());
            if (info != null) {
                skin = info.getSkinLocation();
            }
        }
        // Base head + Hat overlay
        graphics.blit(skin, avX, avY, avSize, avSize, 8, 8, 8, 8, 64, 64);
        graphics.blit(skin, avX, avY, avSize, avSize, 40, 8, 8, 8, 64, 64);
        LumenGfx.outline(graphics, avX - 1, avY - 1, avSize + 2, avSize + 2, 3, 0x22FFFFFF);

        // Content Area
        int textX = avX + avSize + 7;
        int right = x + width - 8;

        // Ping calculation
        int realPing = profile.ping();
        if (mc.getConnection() != null) {
            var playerInfo = mc.getConnection().getPlayerInfo(profile.uuid());
            if (playerInfo != null) {
                realPing = playerInfo.getLatency();
            }
        }
        String pingStr = Math.max(0, realPing) + "ms";
        int pingCol = pingColor(realPing, theme);
        int pingW = AquaFontRenderer.width(font, pingStr);

        // Player Name
        int maxNameW = Math.max(20, right - textX - pingW - 4);
        String name = AquaFontRenderer.fit(font, profile.name(), maxNameW);
        AquaFontRenderer.draw(graphics, font, name, textX, y + 6, 0xFFF1F5F9);

        // Rank Display — unified pill (same style as chat and HUD)
        String rankClean = LumenTheme.getRankTitle(profile.rankId());
        if (profile.rankDisplay() != null && !profile.rankDisplay().isBlank()) {
            String custom = profile.rankDisplay().replaceAll("[\uE000-\uF8FF\uD800-\uDFFF]", "").trim();
            if (!custom.isBlank() && !custom.equalsIgnoreCase(profile.rankId())) {
                rankClean = custom;
            }
        }
        String rank = AquaFontRenderer.fit(font, rankClean, maxNameW);
        int pillW = AquaFontRenderer.width(font, rank) + 8;
        LumenGfx.roundedRect(graphics, textX, y + 15, pillW, 12, 3, rankColor & 0x22FFFFFF);
        LumenGfx.outline(graphics, textX, y + 15, pillW, 12, 3, rankColor & 0x55FFFFFF);
        AquaFontRenderer.draw(graphics, font, rank, textX + 4, y + 17, rankColor);

        // Latency text
        AquaFontRenderer.draw(graphics, font, pingStr, right - pingW, y + 12, pingCol);
    }

    private static int calculateColumns(int screenWidth, int playerCount) {
        if (screenWidth >= 640 && playerCount > 12) {
            return 4;
        }
        if (screenWidth >= 480 && playerCount > 6) {
            return 3;
        }
        if (screenWidth >= 320 && playerCount > 3) {
            return 2;
        }
        return 1;
    }

    public static void scroll(double delta) {
        scroll -= delta * 18;
    }

    public static void resetScroll() {
        scroll = 0;
    }

    private static int pingColor(int ping, LumenTheme theme) {
        if (ping < 0) {
            return 0xFF64748B;
        }
        if (ping <= 70) {
            return theme.success();
        }
        if (ping <= 140) {
            return theme.gold();
        }
        return theme.danger();
    }
}
