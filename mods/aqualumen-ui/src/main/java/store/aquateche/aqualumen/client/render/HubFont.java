package store.aquateche.aqualumen.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/** Hub copy uses the baked TTF (`assets/aqualumen/font/hub.json`), not the vanilla 8px bitmap. */
public final class HubFont {

    public static final ResourceLocation ID = new ResourceLocation("aqualumen", "hub");

    private HubFont() {
    }

    public static MutableComponent of(String text) {
        return Component.literal(text).withStyle(style -> style.withFont(ID));
    }

    public static MutableComponent wrap(Component text) {
        return text.copy().withStyle(style -> style.withFont(ID));
    }

    public static int width(Font font, String text) {
        return font.width(of(text));
    }

    public static int width(Font font, Component text) {
        return font.width(wrap(text));
    }

    public static void draw(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, of(text), x, y, color, false);
    }

    public static void draw(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        graphics.drawString(font, wrap(text), x, y, color, false);
    }

    public static void centered(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        Component line = of(text);
        graphics.drawString(font, line, x - font.width(line) / 2, y, color, false);
    }

    public static void centered(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        Component line = wrap(text);
        graphics.drawString(font, line, x - font.width(line) / 2, y, color, false);
    }
}
