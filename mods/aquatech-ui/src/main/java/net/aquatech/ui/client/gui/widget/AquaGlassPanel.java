package net.aquatech.ui.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Translucent navy glass: fill, 1px border, top highlight. Rounded corners via row insets.
 */
public final class AquaGlassPanel {

    public static final int FILL = 0xCC0B1829;
    public static final int FILL_LIGHT = 0xCC123247;
    public static final int BORDER = 0x665CE1FF;
    public static final int BORDER_HOT = 0xFF5CE1FF;
    public static final int DEFAULT_RADIUS = 4;

    private AquaGlassPanel() {
    }

    public static void draw(GuiGraphics g, int x, int y, int w, int h) {
        draw(g, x, y, w, h, FILL, BORDER, DEFAULT_RADIUS, false);
    }

    public static void draw(GuiGraphics g, int x, int y, int w, int h, int fillARGB, int borderARGB) {
        draw(g, x, y, w, h, fillARGB, borderARGB, DEFAULT_RADIUS, false);
    }

    public static void draw(GuiGraphics g, int x, int y, int w, int h, int fillARGB, int borderARGB, int radius, boolean glow) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int x2 = x + w;
        int y2 = y + h;
        if (glow) {
            fillRounded(g, x - 1, y - 1, x2 + 1, y2 + 1, radius + 1, (0x33 << 24) | (borderARGB & 0xFFFFFF));
        }
        fillRounded(g, x, y, x2, y2, radius, borderARGB);
        fillRounded(g, x + 1, y + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fillARGB);
        int highlightBottom = y + Math.max(3, h / 4);
        fillRounded(g, x + 2, y + 2, x2 - 2, highlightBottom, Math.max(0, radius - 2), 0x18FFFFFF);
        if (w > radius * 2) {
            g.fill(x + radius, y + 1, x2 - radius, y + 2, 0x44FFFFFF);
        }
    }

    public static void drawCard(GuiGraphics g, int x, int y, int w, int h, int accentARGB) {
        draw(g, x, y, w, h, FILL_LIGHT, BORDER, DEFAULT_RADIUS, false);
        g.fill(x, y, x + 2, y + h, accentARGB);
    }

    private static void fillRounded(GuiGraphics g, int x1, int y1, int x2, int y2, int radius, int argb) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        int r = radius;
        if (x2 - x1 < r * 2) {
            r = Math.max(0, (x2 - x1) / 2);
        }
        if (y2 - y1 < r * 2) {
            r = Math.max(0, (y2 - y1) / 2);
        }
        if (r <= 0) {
            g.fill(x1, y1, x2, y2, argb);
            return;
        }
        g.fill(x1, y1 + r, x2, y2 - r, argb);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            g.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, argb);
            g.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, argb);
        }
    }
}
