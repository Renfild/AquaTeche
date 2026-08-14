package com.casesmod.client.gui;

import com.casesmod.client.ClientBalanceState;
import com.casesmod.client.gui.widget.CustomButton;
import com.casesmod.client.gui.widget.GlassUI;
import com.casesmod.client.gui.widget.MenuFonts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * F4 / {@code /menu} — Razor-sharp High-Tech Liquid Glass interface.
 */
public class MainMenuScreen extends Screen {

    private static final int PAD = 14;
    private static final int GAP = 6;
    private static final int BTN_H = 32;
    private static final int CARD_W = 300;
    private static final int CARD_H = 210;

    private static final int COLOR_CYAN = 0xFF00E5FF;
    private static final int COLOR_MUTED = 0xFF64748B;
    private static final int COLOR_BORDER = 0xFF1C3A5A;
    private static final int COLOR_BG_CARD = 0xF207111D;
    private static final int COLOR_STRIP_BG = 0xEE0B192A;

    private int cardX;
    private int cardY;
    private int cardW;
    private int cardH;
    private int stripX1;
    private int stripX2;
    private int stripY;
    private int stripH;

    public MainMenuScreen() {
        super(Component.literal("AquaTech"));
    }

    @Override
    protected void init() {
        cardW = CARD_W;
        cardH = CARD_H;
        cardX = (width - cardW) / 2;
        cardY = (height - cardH) / 2;

        stripY = cardY + 42;
        stripX1 = cardX + PAD;
        stripX2 = cardX + cardW - PAD;
        stripH = 24;

        int innerW = cardW - PAD * 2;
        int colW = (innerW - GAP) / 2;
        int gridTop = cardY + 74;
        int x0 = cardX + PAD;
        int x1 = x0 + colW + GAP;

        // Row 1
        addButton(x0, gridTop, colW, BTN_H, "Киты", 0, () -> minecraft.setScreen(new KitsScreen()));
        addButton(x1, gridTop, colW, BTN_H, "Варпы", 30, () -> minecraft.setScreen(new WarpsScreen()));

        // Row 2
        int row2 = gridTop + BTN_H + GAP;
        addButton(x0, row2, colW, BTN_H, "Спавн", 60, this::teleportSpawn);
        addButton(x1, row2, colW, BTN_H, "Донат", 90, () -> WebOverlay.openDonate(minecraft));

        // Row 3
        int row3 = row2 + BTN_H + GAP;
        addButton(x0, row3, colW, BTN_H, "Квесты", 120, this::openFtbQuests);
        addButton(x1, row3, colW, BTN_H, "Кейсы", 150, () -> minecraft.setScreen(new CasesScreen()));
    }

    private void addButton(int x, int y, int w, int h, String label, int appearDelay, Runnable action) {
        addRenderableWidget(new CustomButton(
                x, y, w, h,
                MenuFonts.text(label),
                COLOR_CYAN,
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

    private void openFtbQuests() {
        if (minecraft.player != null) {
            minecraft.player.connection.sendUnsignedCommand("ftbquests open");
        }
        onClose();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        // Dark atmosphere wash over blurred world
        gfx.fill(0, 0, width, height, 0x6003070E);

        // Main Sleek Glass Card
        GlassUI.drawGlassPanel(gfx, cardX, cardY, cardX + cardW, cardY + cardH, 2,
                COLOR_BG_CARD, COLOR_BORDER, false);

        int cx = cardX + cardW / 2;

        // Header Title
        Component titleComp = MenuFonts.text("AQUATECH");
        int tw = font.width(titleComp);
        gfx.drawString(font, titleComp, cx - tw / 2, cardY + 12, COLOR_CYAN, false);

        drawCentered(gfx, MenuFonts.text("МЕНЮ СЕРВЕРА · F4"), cx, cardY + 24, COLOR_MUTED);

        // Player Profile Strip
        renderProfileStrip(gfx, mouseX, mouseY);

        super.render(gfx, mouseX, mouseY, partialTicks);

        // Footer hint
        drawCentered(gfx, MenuFonts.text("ESC — Закрыть  ·  Клик по профилю — Кабинет"), cx, cardY + cardH - 12, 0xFF475569);
    }

    private void renderProfileStrip(GuiGraphics gfx, int mouseX, int mouseY) {
        boolean hovered = mouseX >= stripX1 && mouseX <= stripX2 && mouseY >= stripY && mouseY <= stripY + stripH;

        int stripBg = hovered ? 0xF010263E : COLOR_STRIP_BG;
        int stripBorder = hovered ? COLOR_CYAN : COLOR_BORDER;

        GlassUI.drawGlassPanel(gfx, stripX1, stripY, stripX2, stripY + stripH, 2, stripBg, stripBorder, hovered);

        String name = (minecraft.player != null) ? minecraft.player.getGameProfile().getName() : "Игрок";

        // Player Head Skin
        int avX = stripX1 + 4;
        int avY = stripY + 4;
        ResourceLocation skin = null;
        if (minecraft.getConnection() != null && minecraft.player != null) {
            PlayerInfo info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
            if (info != null) {
                skin = info.getSkinLocation();
            }
        }
        if (skin != null) {
            PlayerFaceRenderer.draw(gfx, skin, avX, avY, 16);
            GlassUI.drawRectOutline(gfx, avX - 1, avY - 1, avX + 17, avY + 17, COLOR_CYAN);
        } else {
            gfx.fill(avX, avY, avX + 16, avY + 16, 0xFF142E47);
            GlassUI.drawRectOutline(gfx, avX, avY, avX + 16, avY + 16, COLOR_CYAN);
        }

        // Nickname
        gfx.drawString(font, MenuFonts.text(name), avX + 22, stripY + 8, 0xFFFFFFFF, false);

        // Balance Badge
        Component balComp = MenuFonts.text(ClientBalanceState.balance + " монет");
        int balW = font.width(balComp);
        int pillX1 = stripX2 - balW - 14;
        int pillY1 = stripY + 4;
        gfx.fill(pillX1, pillY1, stripX2 - 4, stripY + stripH - 4, 0xFF0D2235);
        GlassUI.drawRectOutline(gfx, pillX1, pillY1, stripX2 - 4, stripY + stripH - 4, 0xFF1E3F61);
        gfx.drawString(font, balComp, pillX1 + 5, stripY + 8, COLOR_CYAN, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= stripX1 && mouseX <= stripX2 && mouseY >= stripY && mouseY <= stripY + stripH) {
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
