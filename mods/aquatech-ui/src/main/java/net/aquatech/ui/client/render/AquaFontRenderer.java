package net.aquatech.ui.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * TTF labels via {@code assets/aquatech_ui/font/main.json} and {@code header.json}.
 * Vanilla {@code Font} still rasterizes; the ResourceLocation on the Component picks the provider.
 */
public final class AquaFontRenderer {

    public static final ResourceLocation FONT_MAIN = new ResourceLocation("aquatech_ui", "main");
    public static final ResourceLocation FONT_HEADER = new ResourceLocation("aquatech_ui", "header");

    private AquaFontRenderer() {
    }

    public static MutableComponent text(String plainText) {
        return Component.literal(plainText == null ? "" : plainText).withStyle(Style.EMPTY.withFont(FONT_MAIN));
    }

    public static MutableComponent header(String plainText) {
        return Component.literal(plainText == null ? "" : plainText).withStyle(Style.EMPTY.withFont(FONT_HEADER));
    }

    public static Component withMain(Component src) {
        return src.copy().withStyle(style -> style.withFont(FONT_MAIN));
    }

    public static int width(Font font, String plain) {
        return font.width(text(plain));
    }

    public static int headerWidth(Font font, String plain) {
        return font.width(header(plain));
    }

    public static void draw(GuiGraphics g, Font font, String plain, int x, int y, int color) {
        g.drawString(font, text(plain), x, y, color, false);
    }

    public static void drawHeader(GuiGraphics g, Font font, String plain, int x, int y, int color) {
        g.drawString(font, header(plain), x, y, color, false);
    }

    public static void drawCentered(GuiGraphics g, Font font, String plain, int cx, int y, int color) {
        Component c = text(plain);
        g.drawString(font, c, cx - font.width(c) / 2, y, color, false);
    }

    public static void drawCenteredHeader(GuiGraphics g, Font font, String plain, int cx, int y, int color) {
        Component c = header(plain);
        g.drawString(font, c, cx - font.width(c) / 2, y, color, false);
    }

    public static void drawGlowText(GuiGraphics g, Font font, Component text, int x, int y, int textColor, int glowColor) {
        int alphaGlow = (glowColor >> 24) & 0xFF;
        if (alphaGlow > 0) {
            int subGlow = (alphaGlow / 3 << 24) | (glowColor & 0xFFFFFF);
            g.drawString(font, text, x - 1, y, subGlow, false);
            g.drawString(font, text, x + 1, y, subGlow, false);
            g.drawString(font, text, x, y - 1, subGlow, false);
            g.drawString(font, text, x, y + 1, subGlow, false);
        }
        g.drawString(font, text, x, y, textColor, false);
    }

    public static void drawCenteredGlowText(GuiGraphics g, Font font, Component text, int cx, int y, int textColor, int glowColor) {
        int w = font.width(text);
        drawGlowText(g, font, text, cx - w / 2, y, textColor, glowColor);
    }

    public static String fit(Font font, String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (width(font, text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellW = width(font, ellipsis);
        if (maxWidth <= ellW) {
            return ellipsis;
        }
        int lo = 0;
        int hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (width(font, text.substring(0, mid)) + ellW <= maxWidth) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return text.substring(0, lo) + ellipsis;
    }
}
