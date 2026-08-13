package net.aquatech.ui.client.gui.widget;

import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class AquaBadge {

    public static final int HEIGHT = 12;
    public static final int PAD_X = 5;

    private AquaBadge() {
    }

    public static int width(Font font, String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return AquaFontRenderer.width(font, text) + PAD_X * 2;
    }

    public static int draw(GuiGraphics g, Font font, int x, int y, String text, int color) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int w = width(font, text);
        int fill = 0xAA000000 | (color & 0x00FFFFFF);
        g.fill(x, y, x + w, y + HEIGHT, fill);
        AquaFontRenderer.draw(g, font, text, x + PAD_X, y + 2, UiDraw.COLOR_TEXT);
        return w;
    }
}
