package com.casesmod.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Набор примитивов для UI в стиле "liquid glass" (аналог Apple Liquid Glass / visionOS):
 * полупрозрачные скруглённые панели с бликом сверху, мягкой тенью и светящейся рамкой.
 * Скругление углов рисуется настоящими пиксель-точными дугами через fillRoundedRect —
 * без текстур, чистыми примитивами GuiGraphics.
 */
public class GlassUI {

    /** Палитра — холодные аква/бирюзовые тона поверх тёмно-синего фона. */
    public static final int AQUA = 0xFF5CE1FF;
    public static final int AQUA_SOFT = 0xFF8FF0FF;
    public static final int DEEP_NAVY = 0xFF0B1220;
    public static final int DEEP_NAVY_2 = 0xFF101A2E;

    /** Liquid-glass tokens: softer fill, aqua border — less solid MC teal. */
    public static final int GLASS_FILL = 0x22E8F6FF;
    public static final int GLASS_FILL_HOVER = 0x3AE8F6FF;
    public static final int GLASS_BORDER = 0x555CE1FF;
    public static final int GLASS_BORDER_HOVER = 0x8C5CE1FF;
    public static final int GLASS_CARD_FILL = 0x2A101828;
    public static final int GLASS_CARD_BORDER = 0x665CE1FF;

    /** Заливка прямоугольника со скруглёнными углами (аппроксимация окружности по строкам). */
    public static void fillRoundedRect(GuiGraphics gfx, int x1, int y1, int x2, int y2, int radius, int argb) {
        if (x2 - x1 < radius * 2) radius = Math.max(0, (x2 - x1) / 2);
        if (y2 - y1 < radius * 2) radius = Math.max(0, (y2 - y1) / 2);
        if (radius <= 0) { gfx.fill(x1, y1, x2, y2, argb); return; }

        // средняя часть без скругления (полная ширина)
        gfx.fill(x1, y1 + radius, x2, y2 - radius, argb);

        for (int i = 0; i < radius; i++) {
            double dy = radius - i - 0.5;
            int inset = (int) Math.round(radius - Math.sqrt(Math.max(0, radius * radius - dy * dy)));
            gfx.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, argb);
            gfx.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, argb);
        }
    }

    /**
     * Стеклянная карточка: тень + скруглённая рамка-заливка (двухпроходная техника: сначала
     * контур цветом рамки, затем заливка внутри на 1px меньше — получается ровная 1px рамка)
     * + верхний блик, имитирующий отражение света на стекле.
     */
    public static void drawGlassPanel(GuiGraphics gfx, int x1, int y1, int x2, int y2, int radius,
                                       int fillARGB, int borderARGB, boolean glow) {
        // мягкая тень под панелью
        fillRoundedRect(gfx, x1 + 2, y1 + 3, x2 + 2, y2 + 3, radius, 0x40000000);

        // внешнее свечение (только при hover/активном состоянии)
        if (glow) {
            fillRoundedRect(gfx, x1 - 2, y1 - 2, x2 + 2, y2 + 2, radius + 2, 0x33 << 24 | (borderARGB & 0xFFFFFF));
        }

        // рамка + заливка (двухпроходная техника даёт чёткую 1px обводку)
        fillRoundedRect(gfx, x1, y1, x2, y2, radius, borderARGB);
        fillRoundedRect(gfx, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), fillARGB);

        // блик стекла — светлая полупрозрачная полоса в верхней трети панели
        int highlightBottom = y1 + Math.max(3, (y2 - y1) / 3);
        fillRoundedRect(gfx, x1 + 2, y1 + 2, x2 - 2, highlightBottom, Math.max(0, radius - 2), 0x22FFFFFF);
        // тонкая яркая линия по самому верху — "кромка" стекла
        gfx.fill(x1 + radius, y1 + 1, x2 - radius, y1 + 2, 0x55FFFFFF);
    }

    /** Смешивает два ARGB-цвета. */
    public static int mix(int a, int b, float t) {
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int ra = (int) (aa + (ba - aa) * t), rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t), rb = (int) (ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    /** Filled disk via per-row circle equation (cheap, no textures). */
    public static void fillDisk(GuiGraphics gfx, int cx, int cy, int radius, int argb) {
        if (radius <= 0) return;
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.floor(Math.sqrt((double) radius * radius - (double) dy * dy));
            gfx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, argb);
        }
    }

    /**
     * Porthole: metal ring + inner glass tint + soft highlight arc.
     * Drawn as outer disk (rim) then inner disk (glass), no stencil needed.
     */
    /** Soft corner vignette — no hard horizontal bands. */
    public static void drawSoftVignette(GuiGraphics gfx, int width, int height, int cornerAlpha) {
        int r = (int) (Math.max(width, height) * 0.72f);
        int argb = (cornerAlpha << 24);
        fillDisk(gfx, 0, 0, r, argb);
        fillDisk(gfx, width, 0, r, argb);
        fillDisk(gfx, 0, height, r, argb);
        fillDisk(gfx, width, height, r, argb);
    }

    public static void drawPorthole(GuiGraphics gfx, int cx, int cy, int outerR, int rimW,
                                    int rimARGB, int glassARGB, int highlightARGB) {
        fillDisk(gfx, cx, cy, outerR, rimARGB);
        fillDisk(gfx, cx, cy, Math.max(1, outerR - rimW), glassARGB);
        // thin inner rim edge
        int edge = Math.max(1, outerR - rimW);
        // highlight crescent (top-left quarter band)
        int hr = edge - 4;
        if (hr > 8) {
            for (int dy = -hr; dy <= -hr / 3; dy++) {
                int dx = (int) Math.floor(Math.sqrt((double) hr * hr - (double) dy * dy));
                int inset = Math.max(2, dx / 6);
                gfx.fill(cx - dx, cy + dy, cx - dx + inset, cy + dy + 1, highlightARGB);
            }
        }
    }
}
