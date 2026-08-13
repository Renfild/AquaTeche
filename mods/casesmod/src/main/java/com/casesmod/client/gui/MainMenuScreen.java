package com.casesmod.client.gui;

import com.casesmod.client.ClientBalanceState;
import com.casesmod.client.gui.widget.BubbleField;
import com.casesmod.client.gui.widget.CustomButton;
import com.casesmod.client.gui.widget.GlassUI;
import com.casesmod.client.gui.widget.MenuFonts;
import com.casesmod.client.gui.widget.OceanParallax;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * F4 / {@code /menu} — compact liquid glass, TTF labels, no item sprites.
 */
public class MainMenuScreen extends Screen {

    private static final int CARD_RADIUS = 12;
    private static final int PAD = 16;
    private static final int GAP = 8;
    private static final int BTN_H = 32;
    private static final int CARD_MIN_W = 300;
    private static final int CARD_MAX_W = 340;
    private static final int MUTED = 0xB3AABBCC;

    private final BubbleField bubbles = new BubbleField();
    private final long openedAtMs = System.currentTimeMillis();

    private int cardX;
    private int cardY;
    private int cardW;
    private int cardH;
    private int stripX1;
    private int stripX2;
    private int stripY;

    public MainMenuScreen() {
        super(Component.literal("AquaTech"));
    }

    @Override
    protected void init() {
        cardW = Mth.clamp((int) (width * 0.32f), CARD_MIN_W, CARD_MAX_W);
        cardH = Mth.clamp((int) (height * 0.58f), 268, 300);
        cardX = (width - cardW) / 2;
        cardY = (height - cardH) / 2;
        stripY = cardY + 68;
        stripX1 = cardX + PAD;
        stripX2 = cardX + cardW - PAD;

        int innerW = cardW - PAD * 2;
        int colW = (innerW - GAP) / 2;
        int gridTop = cardY + 88;
        int x0 = cardX + PAD;
        int x1 = x0 + colW + GAP;

        addButton(x0, gridTop, colW, BTN_H, "Киты", 0, () -> minecraft.setScreen(new KitsScreen()));
        addButton(x1, gridTop, colW, BTN_H, "Варпы", 45, () -> minecraft.setScreen(new WarpsScreen()));

        int row2 = gridTop + BTN_H + GAP;
        addButton(x0, row2, colW, BTN_H, "Спавн", 90, this::teleportSpawn);
        addButton(x1, row2, colW, BTN_H, "Рынок рыбы", 135, () -> minecraft.setScreen(new FishMarketScreen()));

        int row3 = row2 + BTN_H + GAP;
        addButton(x0, row3, colW, BTN_H, "Донат", 180, () -> WebOverlay.openDonate(minecraft));
        addButton(x1, row3, colW, BTN_H, "Квесты", 225, () -> minecraft.setScreen(new QuestsScreen()));

        int row4 = row3 + BTN_H + GAP;
        addButton(x0, row4, innerW, BTN_H + 2, "Кейсы", 270, () -> minecraft.setScreen(new CasesScreen()));

        bubbles.resize(width, height);
    }

    private void addButton(int x, int y, int w, int h, String label, int appearDelay, Runnable action) {
        addRenderableWidget(new CustomButton(
                x, y, w, h,
                MenuFonts.text(label),
                GlassUI.AQUA,
                b -> action.run(),
                appearDelay
        ));
    }

    private void teleportSpawn() {
        if (minecraft.player != null) {
            minecraft.player.connection.sendUnsignedCommand("spawn");
        }
        onClose();
    }

    @Override
    public void tick() {
        bubbles.tick(1f / 20f);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        float timeSec = (System.currentTimeMillis() - openedAtMs) / 1000f;
        OceanParallax.render(gfx, width, height, timeSec, partialTicks);
        bubbles.render(gfx);
        gfx.fill(0, 0, width, height, 0x40000000);

        GlassUI.drawGlassPanel(gfx, cardX, cardY, cardX + cardW, cardY + cardH, CARD_RADIUS,
                GlassUI.GLASS_CARD_FILL, GlassUI.GLASS_CARD_BORDER, false);

        int cx = cardX + cardW / 2;
        int emblemY = cardY + 26;
        GlassUI.drawPorthole(gfx, cx, emblemY, 16, 3, 0xAA3A6080, 0x66102840, 0x55FFFFFF);
        GlassUI.fillDisk(gfx, cx, emblemY, 6, 0x885CE1FF);
        GlassUI.fillDisk(gfx, cx - 2, emblemY - 3, 2, 0x66FFFFFF);

        drawCentered(gfx, MenuFonts.text("AQUATECH"), cx, cardY + 42, 0xFFFFFFFF);
        drawCentered(gfx, MenuFonts.text("Меню сервера · F4"), cx, cardY + 54, MUTED);

        renderProfileStrip(gfx);
        super.render(gfx, mouseX, mouseY, partialTicks);
        drawCentered(gfx, MenuFonts.text("клик по нику — кабинет"), cx, cardY + cardH - 12, 0x66AAAAAA);
    }

    private void renderProfileStrip(GuiGraphics gfx) {
        int stripY = cardY + 68;
        int stripX1 = cardX + PAD;
        int stripX2 = cardX + cardW - PAD;
        this.stripY = stripY;
        this.stripX1 = stripX1;
        this.stripX2 = stripX2;
        int innerR = Math.max(4, CARD_RADIUS - 8);
        GlassUI.fillRoundedRect(gfx, stripX1, stripY, stripX2, stripY + 18, innerR, 0x28102035);

        String name = minecraft.player != null ? minecraft.player.getGameProfile().getName() : "Игрок";
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        int avCx = stripX1 + 10;
        int avCy = stripY + 9;
        GlassUI.fillDisk(gfx, avCx, avCy, 7, 0x665CE1FF);
        Component initialComp = MenuFonts.text(initial);
        gfx.drawString(font, initialComp, avCx - font.width(initialComp) / 2, stripY + 5, 0xFFFFFFFF, false);
        gfx.drawString(font, MenuFonts.text(name), stripX1 + 22, stripY + 5, 0xFFFFFFFF, false);

        Component balComp = MenuFonts.text("✦ " + ClientBalanceState.balance);
        int balW = font.width(balComp);
        int pillX1 = stripX2 - balW - 12;
        GlassUI.fillRoundedRect(gfx, pillX1, stripY + 2, stripX2 - 4, stripY + 16, Math.max(3, innerR - 2), 0x385CE1FF);
        gfx.drawString(font, balComp, pillX1 + 6, stripY + 5, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= stripX1 && mouseX <= stripX2 && mouseY >= stripY && mouseY <= stripY + 18) {
            WebOverlay.openCabinet(minecraft);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawCentered(GuiGraphics gfx, Component text, int cx, int y, int color) {
        gfx.drawString(font, text, cx - font.width(text) / 2, y, color, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
