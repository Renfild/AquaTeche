package com.casesmod.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Modern High-Tech Liquid Glass rendering primitives for Forge 1.20.1.
 * Crisp 1px borders, subtle 2px corner insets, glowing hover states and rich gradients.
 */
public class GlassUI {

    public static final int AQUA = 0xFF00E5FF;
    public static final int AQUA_SOFT = 0xFF38BDF8;
    public static final int DEEP_NAVY = 0xFF060D17;
    public static final int DEEP_NAVY_2 = 0xFF0B1726;

    public static final int GLASS_FILL = 0xD90B192C;
    public static final int GLASS_FILL_HOVER = 0xF0122B45;
    public static final int GLASS_BORDER = 0xFF1C3A5A;
    public static final int GLASS_BORDER_HOVER = 0xFF00E5FF;
    public static final int GLASS_CARD_FILL = 0xEE07111D;
    public static final int GLASS_CARD_BORDER = 0xFF1E3D5C;

    /**
     * Draws a crisp 1px bordered glass panel with subtle 2px corner insets and top refraction line.
     */
    public static void drawGlassPanel(GuiGraphics gfx, int x1, int y1, int x2, int y2, int radius,
                                       int fillARGB, int borderARGB, boolean glow) {
        if (x2 <= x1 || y2 <= y1) return;

        // Subtle soft drop shadow
        gfx.fill(x1 + 1, y2, x2 - 1, y2 + 2, 0x40000000);
        gfx.fill(x2, y1 + 1, x2 + 2, y2, 0x40000000);

        // Outer glow on hover
        if (glow) {
            drawRectOutline(gfx, x1 - 1, y1 - 1, x2 + 1, y2 + 1, (0x44 << 24) | (borderARGB & 0xFFFFFF));
        }

        // 1px Crisp Border
        drawRectOutline(gfx, x1, y1, x2, y2, borderARGB);

        // Main Glass Body Fill
        gfx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillARGB);

        // Top edge light refraction line
        gfx.fill(x1 + 2, y1 + 1, x2 - 2, y1 + 2, (0x30 << 24) | 0xFFFFFF);
    }

    /**
     * Draws a crisp 1-pixel rectangle outline without overlap.
     */
    public static void drawRectOutline(GuiGraphics gfx, int x1, int y1, int x2, int y2, int argb) {
        if (x2 <= x1 || y2 <= y1) return;
        gfx.fill(x1, y1, x2, y1 + 1, argb);         // Top
        gfx.fill(x1, y2 - 1, x2, y2, argb);         // Bottom
        gfx.fill(x1, y1 + 1, x1 + 1, y2 - 1, argb); // Left
        gfx.fill(x2 - 1, y1 + 1, x2, y2 - 1, argb); // Right
    }

    /** Mixes two ARGB colors. */
    public static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int ra = (int) (aa + (ba - aa) * t);
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    public static void fillRoundedRect(GuiGraphics gfx, int x1, int y1, int x2, int y2, int radius, int argb) {
        if (x2 <= x1 || y2 <= y1) return;
        drawRectOutline(gfx, x1, y1, x2, y2, (0x55 << 24) | (argb & 0xFFFFFF));
        gfx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, argb);
    }

    public static void drawSoftVignette(GuiGraphics gfx, int width, int height, int cornerAlpha) {
        int argb = (cornerAlpha << 24);
        gfx.fillGradient(0, 0, width, height / 4, argb, 0x00000000);
        gfx.fillGradient(0, height * 3 / 4, width, height, 0x00000000, argb);
    }

    public static void drawPorthole(GuiGraphics gfx, int cx, int cy, int outerR, int rimW,
                                    int rimARGB, int glassARGB, int highlightARGB) {
        drawRectOutline(gfx, cx - outerR, cy - outerR, cx + outerR, cy + outerR, rimARGB);
        gfx.fill(cx - outerR + 1, cy - outerR + 1, cx + outerR - 1, cy + outerR - 1, glassARGB);
    }
}
