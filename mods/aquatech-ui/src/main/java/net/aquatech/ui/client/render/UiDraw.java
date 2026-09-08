package net.aquatech.ui.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.aquatech.ui.client.gui.widget.AquaBadge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class UiDraw {
    public static final int COLOR_PANEL = 0xCC0B1F2A;
    public static final int COLOR_PANEL_LIGHT = 0xCC123247;
    public static final int COLOR_ACCENT = 0xFF5AC8FA;
    public static final int COLOR_ACCENT_DARK = 0xFF1F6FEB;
    public static final int COLOR_TEXT = 0xFFFFFFFF;
    public static final int COLOR_MUTED = 0xFFB6C9D6;
    public static final int COLOR_PRIMARY = 0xFF38BDF8;

    private UiDraw() {
    }

    public static void panel(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, color);
        border(graphics, x, y, w, h, COLOR_ACCENT);
    }

    public static void border(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    public static void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1;
        int y = y1;
        while (true) {
            graphics.fill(x, y, x + 2, y + 2, color);
            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    public static void drawGlowCircle(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    graphics.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
                }
            }
        }
    }

    /** Cheap AquaTech skill-node: border + fill + accent strip (no per-pixel circles). */
    public static void drawSkillNode(GuiGraphics graphics, int cx, int cy, int halfSize, int borderColor, int fillColor) {
        int x0 = cx - halfSize;
        int y0 = cy - halfSize;
        int size = halfSize * 2;
        graphics.fill(x0 - 1, y0 - 1, x0 + size + 1, y0 + size + 1, borderColor);
        graphics.fill(x0, y0, x0 + size, y0 + size, fillColor);
        graphics.fill(x0 + 1, y0 + 1, x0 + size - 1, y0 + 2, (borderColor & 0x55FFFFFF));
    }

    /** Manhattan link between nodes — 1px lines, cheap for skill trees. */
    public static void drawSkillLink(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int midX = x2;
        int midY = y1;
        int left = Math.min(x1, midX);
        int right = Math.max(x1, midX);
        graphics.fill(left, y1, right + 1, y1 + 1, color);
        int top = Math.min(midY, y2);
        int bottom = Math.max(midY, y2);
        graphics.fill(x2, top, x2 + 1, bottom + 1, color);
    }

    public static void badge(GuiGraphics graphics, int x, int y, String text, int color) {
        Font font = Minecraft.getInstance().font;
        AquaBadge.draw(graphics, font, x, y, text, color);
    }

    /**
     * Blits a small square icon (native texW x texH) scaled to w x h, multiplied by tintColor RGB.
     * Use light/white PNGs so tinting works; pass 0xFFFFFFFF to draw unmodified.
     */
    public static void blitIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y, int w, int h,
                                 int texW, int texH, int tintColor) {
        float r = ((tintColor >> 16) & 0xFF) / 255f;
        float g = ((tintColor >> 8) & 0xFF) / 255f;
        float b = (tintColor & 0xFF) / 255f;
        int a8 = (tintColor >>> 24) & 0xFF;
        float a = a8 == 0 ? 1.0f : a8 / 255f;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, a);
        graphics.blit(texture, x, y, w, h, 0, 0, texW, texH, texW, texH);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawPlayerHead(GuiGraphics graphics, UUID uuid, String name, int x, int y, int size) {
        drawPlayerHead(graphics, uuid, name, x, y, size, false);
    }

    /**
     * Face+hat from the 8×8 skin UVs, snapped to a multiple of 8 so pixel heads stay crisp.
     * Chat should pass 24 (3×). Nearest filter; no accent box unless asked.
     */
    public static void drawPlayerHead(GuiGraphics graphics, UUID uuid, String name, int x, int y, int size, boolean accentBorder) {
        String nick = name == null ? "" : name;
        UUID fallback = uuid != null
                ? uuid
                : UUID.nameUUIDFromBytes(("OfflinePlayer:" + nick).getBytes(StandardCharsets.UTF_8));
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(fallback);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            var playerInfo = uuid != null ? minecraft.getConnection().getPlayerInfo(uuid) : null;
            if (playerInfo == null && !nick.isBlank()) {
                playerInfo = minecraft.getConnection().getPlayerInfo(nick);
            }
            if (playerInfo != null) {
                skin = playerInfo.getSkinLocation();
            }
        }
        int px = Math.max(8, ((size + 4) / 8) * 8);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().getTexture(skin).bind();
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        graphics.blit(skin, x, y, px, px, 8, 8, 8, 8, 64, 64);
        graphics.blit(skin, x, y, px, px, 40, 8, 8, 8, 64, 64);
        if (accentBorder) {
            border(graphics, x - 1, y - 1, px + 2, px + 2, COLOR_ACCENT);
        }
    }

    /**
     * Cyber-MMO Tactical Rectangular Avatar with smoothed corners and glowing frame.
     */
    public static void drawTacticalAvatar(GuiGraphics graphics, UUID uuid, String name, int x, int y, int size,
                                         int borderColor, float alpha) {
        int bgCol = ((int) (alpha * 200.0F) << 24) | 0x080E17;
        LumenGfx.roundedRect(graphics, x, y, size, size, 5, bgCol);

        // Snap head to internal bounds with 2px padding
        int headPad = 2;
        int headSize = size - headPad * 2;
        int hx = x + headPad;
        int hy = y + headPad;

        drawPlayerHead(graphics, uuid, name, hx, hy, headSize, false);

        int borderAlpha = (int) (((borderColor >>> 24) & 0xFF) * alpha);
        if (borderAlpha == 0 && alpha > 0.05F) borderAlpha = (int) (alpha * 255.0F);
        int finalBorder = (borderAlpha << 24) | (borderColor & 0x00FFFFFF);

        // Soft outer glow + crisp outline
        LumenGfx.outline(graphics, x, y, size, size, 5, finalBorder);
        LumenGfx.outline(graphics, x - 1, y - 1, size + 2, size + 2, 6, (borderAlpha / 3 << 24) | (borderColor & 0x00FFFFFF));
    }

    /**
     * Procedural glowing tactical crest / shield for system announcements (Concept 2 style).
     */
    public static void drawTacticalShield(GuiGraphics graphics, int x, int y, int size, int tintColor, float alpha) {
        int a8 = (int) (((tintColor >>> 24) & 0xFF) * alpha);
        if (a8 == 0 && alpha > 0.05F) a8 = (int) (alpha * 255.0F);
        int col = (a8 << 24) | (tintColor & 0x00FFFFFF);
        int glowCol = ((a8 / 4) << 24) | (tintColor & 0x00FFFFFF);

        int cx = x + size / 2;
        int cy = y + size / 2;
        int r = size / 2 - 2;

        // Outer soft glow backdrop
        LumenGfx.roundedRect(graphics, x + 1, y + 1, size - 2, size - 2, 6, ((a8 / 6) << 24) | 0x001B2B);

        // Tactical Shield Geometry
        // Crown peaks
        graphics.fill(cx - 1, y + 2, cx + 1, y + 5, col);
        graphics.fill(cx - 5, y + 3, cx - 3, y + 6, col);
        graphics.fill(cx + 3, y + 3, cx + 5, y + 6, col);

        // Shield border
        int topY = y + 6;
        int midY = y + size - 8;
        int botY = y + size - 3;
        int w2 = r - 2;

        // Top horizontal bar
        graphics.fill(cx - w2, topY, cx + w2, topY + 1, col);

        // Left & right verticals
        graphics.fill(cx - w2, topY, cx - w2 + 1, midY, col);
        graphics.fill(cx + w2 - 1, topY, cx + w2, midY, col);

        // Bottom chevron taper
        graphics.fill(cx - w2 + 1, midY, cx - w2 + 3, midY + 2, col);
        graphics.fill(cx + w2 - 3, midY, cx + w2 - 1, midY + 2, col);
        graphics.fill(cx - 3, midY + 2, cx - 1, botY - 1, col);
        graphics.fill(cx + 1, midY + 2, cx + 3, botY - 1, col);
        graphics.fill(cx - 1, botY - 1, cx + 1, botY, col);

        // Side tech wing brackets
        int wingL = cx - w2 - 3;
        int wingR = cx + w2 + 2;
        graphics.fill(wingL, topY + 2, wingL + 2, topY + 4, col);
        graphics.fill(wingL - 1, topY + 4, wingL + 1, midY - 2, col);
        graphics.fill(wingL, midY - 2, wingL + 2, midY, col);

        graphics.fill(wingR, topY + 2, wingR + 2, topY + 4, col);
        graphics.fill(wingR + 1, topY + 4, wingR + 3, midY - 2, col);
        graphics.fill(wingR, midY - 2, wingR + 2, midY, col);

        // Core cyan emblem / spark
        graphics.fill(cx - 2, cy - 2, cx + 2, cy + 2, col);
        graphics.fill(cx - 1, cy - 4, cx + 1, cy + 4, col);
        graphics.fill(cx - 4, cy - 1, cx + 4, cy + 1, col);
    }

    public static int rankColor(String rankId) {
        if (rankId == null) return 0xFF3B82F6;
        String id = rankId.toLowerCase(java.util.Locale.ROOT);
        return switch (id) {
            case "owner" -> 0xFFFBBF24;
            case "admin", "dev", "developer" -> 0xFFEF4444;
            case "mod", "moderator" -> 0xFFF97316;
            case "ultimate" -> 0xFF00E5FF;
            case "admiral" -> 0xFF8B5CF6;
            case "legend" -> 0xFFE879F9;
            case "captain" -> 0xFF38BDF8;
            case "skipper" -> 0xFF22D3EE;
            case "sailor" -> 0xFF60A5FA;
            case "vip", "premium" -> 0xFFF59E0B;
            default -> 0xFF3B82F6;
        };
    }
}
