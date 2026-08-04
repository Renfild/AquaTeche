package net.aquatech.ui.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

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
        int width = font.width(text) + 10;
        graphics.fill(x, y, x + width, y + 12, 0xAA000000 | (color & 0x00FFFFFF));
        graphics.drawString(font, text, x + 5, y + 2, COLOR_TEXT, false);
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceLocation skin = DefaultPlayerSkin.getDefaultSkin(uuid);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            var playerInfo = minecraft.getConnection().getPlayerInfo(uuid);
            if (playerInfo != null) {
                skin = playerInfo.getSkinLocation();
            }
        }
        graphics.blit(skin, x, y, size, size, 8, 8, 8, 8, 64, 64);
        graphics.blit(skin, x, y, size, size, 40, 8, 8, 8, 64, 64);
        UiDraw.border(graphics, x - 1, y - 1, size + 2, size + 2, COLOR_ACCENT);
    }

    public static int rankColor(String rankId) {
        return switch (rankId) {
            case "owner" -> 0xFFFBBF24;
            case "admin", "dev", "developer" -> 0xFFEF4444;
            case "mod", "moderator" -> 0xFFF97316;
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
