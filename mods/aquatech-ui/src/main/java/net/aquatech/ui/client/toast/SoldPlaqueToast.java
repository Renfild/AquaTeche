package net.aquatech.ui.client.toast;

import net.aquatech.ui.client.render.AquaFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Плашка «Лот продан» — угол сверху-справа, выезжает, висит 6 сек, растворяется.
 * Фон: textures/gui/plaque_sold.png (256x64, рисует юзер). Текст рисуется поверх.
 */
public final class SoldPlaqueToast {
    private static final ResourceLocation BG =
            new ResourceLocation("aquatech_ui", "textures/gui/plaque_sold.png");
    private static final int W = 128;
    private static final int H = 32;

    private static final List<SoldPlaqueToast> ACTIVE = new ArrayList<>();

    public final String item;
    public final String buyer;
    public final String price;
    public int age;

    private SoldPlaqueToast(String item, String buyer, String price) {
        this.item = item;
        this.buyer = buyer;
        this.price = price;
    }

    /** Разбирает строку доставки вида: Ваш лот "X" купил Y за N ¤ */
    public static void pushFromChat(String stripped) {
        if (stripped == null || !stripped.contains("Ваш лот") || !stripped.contains("продан")
                || !stripped.contains("купил")) {
            return;
        }
        try {
            var m = java.util.regex.Pattern
                    .compile("Ваш лот \"(.+?)\" купил (.+?) за ([\\d ]+)")
                    .matcher(stripped);
            if (m.find()) {
                push(m.group(1), m.group(2), m.group(3).trim());
            }
        } catch (Exception ignored) {
        }
    }

    public static void push(String item, String buyer, String price) {
        synchronized (ACTIVE) {
            ACTIVE.add(0, new SoldPlaqueToast(item, buyer, price));
            while (ACTIVE.size() > 3) {
                ACTIVE.remove(ACTIVE.size() - 1);
            }
        }
    }

    public static void tick() {
        synchronized (ACTIVE) {
            ACTIVE.forEach(t -> t.age++);
            ACTIVE.removeIf(t -> t.age > 170);
        }
    }

    public static void render(GuiGraphics g, Font font) {
        List<SoldPlaqueToast> snapshot;
        synchronized (ACTIVE) {
            if (ACTIVE.isEmpty()) return;
            snapshot = new ArrayList<>(ACTIVE);
        }
        int mcW = g.guiWidth();
        int y = 6;
        for (SoldPlaqueToast t : snapshot) {
            float fadeIn = Math.min(1F, t.age / 6F);
            float fadeOut = Math.min(1F, (170 - t.age) / 12F);
            float fade = Math.max(0F, Math.min(fadeIn, fadeOut));
            float slide = (1F - fadeIn) * (W + 10);
            float x = mcW - W - 6 + slide;
            int a = (int) (fade * 255);

            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.blit(BG, 0, 0, 0, 0, W, H, W, H);
            Font f = Minecraft.getInstance().font;
            drawA(g, f, "§6§lЛОТ ПРОДАН", 8, 5, 0xFFFFC25B, a);
            drawA(g, f, "§f" + clip(f, t.item, 100), 8, 13, 0xFFFFFFFF, a);
            drawA(g, f, "§7купил: §b" + t.buyer, 8, 21, 0xFF9DB2C4, a);
            drawA(g, f, "§6§l" + t.price + " ¤", W - 8 - f.width(t.price + " ¤"), 21, 0xFFFFC25B, a);
            g.pose().popPose();
            y += H + 4;
        }
    }

    private static void drawA(GuiGraphics g, Font font, String text, int x, int y, int color, int alpha) {
        var comp = Component.literal(text);
        int base = (color >>> 24) & 0xFF;
        int c = ((base * alpha / 255) << 24) | (color & 0xFFFFFF);
        g.drawString(font, comp, x, y, c, false);
    }

    private static String clip(Font font, String s, int maxW) {
        String clean = s == null ? "" : s.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim();
        if (font.width(clean) <= maxW) return clean;
        while (!clean.isEmpty() && font.width(clean + "...") > maxW) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean + "...";
    }
}
