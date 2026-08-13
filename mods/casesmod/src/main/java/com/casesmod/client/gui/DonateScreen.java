package com.casesmod.client.gui;

import com.casesmod.client.gui.widget.CustomButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Витрина донат-пакетов. По умолчанию просто показывает описание —
 * подключите сюда команду покупки вашего донат-плагина (например /buy <id>) в onPress кнопки.
 */
public class DonateScreen extends Screen {
    private static final int CARD_W = 280, CARD_H = 72, GAP = 14, LIST_TOP = 70;
    private static final int FOOTER_H = 52;

    private static final String[][] PACKAGES = {
            {"§6VIP", "Доступ к /kit vip, донат-кейс раз в день", "199₽"},
            {"§bPREMIUM", "VIP + доступ к цветным нику и /fly", "399₽"},
            {"§dLEGEND", "PREMIUM + эксклюзивный кейс LEGEND", "799₽"}
    };

    private final List<CustomButton> cards = new ArrayList<>();
    private int scrollY;
    private int maxScroll;
    private int listBottom;
    private CustomButton backBtn;

    public DonateScreen() {
        super(Component.literal("Донат"));
    }

    @Override
    protected void init() {
        cards.clear();
        listBottom = height - FOOTER_H;
        int totalH = PACKAGES.length * (CARD_H + GAP) - GAP;
        maxScroll = Math.max(0, totalH - Math.max(40, listBottom - LIST_TOP));
        scrollY = Mth.clamp(scrollY, 0, maxScroll);

        int x = (width - CARD_W) / 2;
        for (int i = 0; i < PACKAGES.length; i++) {
            // Empty label — title/desc/price drawn in render() so nothing overlaps.
            CustomButton btn = new CustomButton(x, LIST_TOP, CARD_W, CARD_H,
                    Component.empty(),
                    0xFFD4AF37, 0xFFFFD75E, b -> {
                        // Подключите свой донат-плагин/веб-магазин здесь.
                    });
            cards.add(btn);
            addRenderableWidget(btn);
        }
        applyScroll();

        backBtn = new CustomButton(20, height - 36, 90, 22, Component.literal("← Назад"),
                0xFF444444, 0xFF666666, b -> minecraft.setScreen(new MainMenuScreen()));
        addRenderableWidget(backBtn);
    }

    private void applyScroll() {
        int x = (width - CARD_W) / 2;
        for (int i = 0; i < cards.size(); i++) {
            CustomButton btn = cards.get(i);
            int y = LIST_TOP + i * (CARD_H + GAP) - scrollY;
            btn.setX(x);
            btn.setY(y);
            btn.visible = y + CARD_H > LIST_TOP - 2 && y < listBottom + 2;
        }
        if (backBtn != null) {
            backBtn.setY(height - 36);
            backBtn.visible = true;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll <= 0) return super.mouseScrolled(mouseX, mouseY, delta);
        int before = scrollY;
        scrollY = Mth.clamp(scrollY - (int) (delta * 18), 0, maxScroll);
        if (scrollY != before) {
            applyScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        gfx.fillGradient(0, 0, width, height, 0xF0080604, 0xF0140C06);
        gfx.fill(0, 0, width, height, 0x77000000);
        gfx.drawCenteredString(font, Component.literal("§l§6Донат"), width / 2, 30, 0xFFFFFFFF);
        gfx.drawCenteredString(font, Component.literal("§7Поддержите сервер и получите бонусы"),
                width / 2, 48, 0xFFAAAAAA);

        gfx.enableScissor(0, LIST_TOP - 4, width, listBottom);
        super.render(gfx, mouseX, mouseY, partialTicks);

        int x = (width - CARD_W) / 2;
        for (int i = 0; i < PACKAGES.length; i++) {
            CustomButton btn = cards.get(i);
            if (!btn.visible) continue;
            String[] pack = PACKAGES[i];
            int y = btn.getY();

            String price = "§e" + pack[2];
            int priceW = font.width(price);
            gfx.drawString(font, price, x + CARD_W - 12 - priceW, y + 12, 0xFFFFEE88, false);

            String title = pack[0];
            int maxTitleW = CARD_W - 24 - priceW - 12;
            if (font.width(title) > maxTitleW) {
                while (title.length() > 1 && font.width(title + "…") > maxTitleW) {
                    title = title.substring(0, title.length() - 1);
                }
                title = title + "…";
            }
            gfx.drawString(font, title, x + 14, y + 12, 0xFFFFFFFF, false);

            gfx.drawWordWrap(font, Component.literal("§7" + pack[1]),
                    x + 14, y + 32, CARD_W - 28, 0xFFCCCCCC);
        }
        gfx.disableScissor();

        if (backBtn != null) {
            backBtn.render(gfx, mouseX, mouseY, partialTicks);
        }

        if (maxScroll > 0) {
            int trackX = width - 12;
            int trackTop = LIST_TOP;
            int trackH = listBottom - LIST_TOP;
            gfx.fill(trackX, trackTop, trackX + 3, trackTop + trackH, 0x44AA8844);
            int thumbH = Math.max(16, trackH * trackH / (trackH + maxScroll));
            int thumbY = trackTop + (int) ((trackH - thumbH) * (scrollY / (float) maxScroll));
            gfx.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xAAD4AF37);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
