package net.aquatech.ui.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Rarity-tinted item cell. Colors match casesmod {@code CaseItem.Rarity}.
 */
public final class AquaCaseSlot {

    public static final int DEFAULT_SIZE = 36;

    public enum Rarity {
        COMMON(0xAAAAAA),
        UNCOMMON(0x55FF55),
        RARE(0x55AAFF),
        EPIC(0xAA00FF),
        LEGENDARY(0xFFAA00);

        public final int rgb;

        Rarity(int rgb) {
            this.rgb = rgb;
        }

        public static Rarity fromName(String name) {
            if (name == null || name.isEmpty()) {
                return COMMON;
            }
            try {
                return valueOf(name.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return COMMON;
            }
        }
    }

    private AquaCaseSlot() {
    }

    public static void draw(GuiGraphics g, Font font, int x, int y, int size, Rarity rarity, ItemStack stack, boolean highlight) {
        if (size < 12) {
            return;
        }
        Rarity r = rarity == null ? Rarity.COMMON : rarity;
        int rgb = r.rgb & 0xFFFFFF;
        int bgAlpha = highlight ? 0x66 : 0x2A;
        g.fill(x + 2, y + 2, x + size - 2, y + size - 2, (bgAlpha << 24) | rgb);
        int borderA = highlight ? 0xFF : 0x90;
        int border = (borderA << 24) | rgb;
        g.fill(x + 1, y + 1, x + size - 1, y + 2, border);
        g.fill(x + 1, y + size - 2, x + size - 1, y + size - 1, border);
        g.fill(x + 1, y + 1, x + 2, y + size - 1, border);
        g.fill(x + size - 2, y + 1, x + size - 1, y + size - 1, border);

        if (stack == null || stack.isEmpty()) {
            return;
        }
        int cx = x + size / 2;
        int cy = y + size / 2;
        g.renderItem(stack, cx - 8, cy - 8);
        if (font != null) {
            g.renderItemDecorations(font, stack, cx - 8, cy - 8);
        }
    }

    public static void draw(GuiGraphics g, int x, int y, int size, Rarity rarity, ItemStack stack, boolean highlight) {
        draw(g, null, x, y, size, rarity, stack, highlight);
    }
}
