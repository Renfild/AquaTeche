package net.aquatech.ui.client.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Плашка «Лот продан» — угол сверху-справа, выезжает, висит 6 сек, растворяется.
 * Фон: textures/gui/plaque_sold.png (256x64, рисует юзер). Текст рисуется поверх
 * шрифтом aquatech_ui:main (ванильный шрифт в паке подменён и ломает кириллицу).
 * Цена — нашей иконкой монеты textures/gui/coin.png вместо глифа «¤».
 */
public final class SoldPlaqueToast {
    private static final ResourceLocation BG =
            new ResourceLocation("aquatech_ui", "textures/gui/plaque_sold.png");
    private static final ResourceLocation COIN =
            new ResourceLocation("aquatech_ui", "textures/gui/coin.png");
    private static final int W = 256;
    private static final int H = 64;

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

    /** Разбирает строку доставки вида: Ваш лот "X" продан. Купил: Y за N ¤ */
    public static void pushFromChat(String stripped) {
        if (stripped == null) {
            return;
        }
        String clean = stripped.replaceAll("[§&][0-9a-fk-orA-FK-OR]", "");
        String low = clean.toLowerCase(java.util.Locale.ROOT);
        if (!low.contains("ваш лот") || !low.contains("продан") || !low.contains("купил")) {
            return;
        }
        try {
            var m = java.util.regex.Pattern
                    .compile("Ваш лот \"(.+?)\" продан[.!]\\s*Купил:\\s*(.+?)\\s*за\\s*([\\d\\s]+)",
                            java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE)
                    .matcher(clean);
            if (m.find()) {
                push(m.group(1), m.group(2).trim(), m.group(3).trim());
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
            g.blit(BG, 0, 0, 0, 0, W, H, 256, 64);

            AquaFontRenderer.draw(g, font, clip(font, t.item, 150), 26, 16, fade(0xFFFFC25B, a));
            // Цена: число + наша иконка монеты (11x11) у правого края
            String price = t.price == null ? "" : t.price.trim();
            int priceW = AquaFontRenderer.width(font, price);
            AquaFontRenderer.draw(g, font, price, W - 26 - priceW - 14, 15, fade(0xFFFFC25B, a));
            RenderSystem.setShaderColor(1F, 1F, 1F, fade);
            g.blit(COIN, W - 26 - 11, 14, 11, 11, 0, 0, 32, 32, 32, 32);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            String sold = "Продано игроку ";
            AquaFontRenderer.draw(g, font, sold, 26, 44, fade(0xFF9DB2C4, a));
            AquaFontRenderer.draw(g, font, clip(font, t.buyer, 120),
                    26 + AquaFontRenderer.width(font, sold), 44, fade(0xFF55FFFF, a));
            g.pose().popPose();
            y += H + 4;
        }
    }

    private static int fade(int color, int alpha) {
        int base = (color >>> 24) & 0xFF;
        return ((base * alpha / 255) << 24) | (color & 0xFFFFFF);
    }

    private static String clip(Font font, String s, int maxW) {
        String clean = s == null ? "" : s.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim();
        if (AquaFontRenderer.width(font, clean) <= maxW) return clean;
        while (!clean.isEmpty() && AquaFontRenderer.width(font, clean + "...") > maxW) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean + "...";
    }
}
