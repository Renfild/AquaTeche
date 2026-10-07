package net.aquatech.ui.client.gui;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.inventory.KeepnetMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Экран садка: текстура 256 x высота GUI тира (рисует tools/build_keepnet_gui.py), тиры с большим числом рядов выше. */
public class KeepnetScreen extends AbstractContainerScreen<KeepnetMenu> {

    /** Табличка с названием занимает (60, 2) .. (195, 17) в координатах GUI. */
    private static final int PLAQUE_CENTER_X = 128;
    private static final int PLAQUE_TEXT_Y = 6;
    private static final int PLAQUE_TEXT_COLOR = 0x22262C;
    private static final int TEXTURE_WIDTH = 256;

    private final ResourceLocation texture;

    public KeepnetScreen(KeepnetMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = menu.getTier().imageHeight();
        this.texture = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/" + menu.getTier().id() + ".png");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component label = Component.empty().append(title).append(" " + menu.filledSlots() + "/" + menu.getTier().slots());
        guiGraphics.drawString(font, label, PLAQUE_CENTER_X - font.width(label) / 2, PLAQUE_TEXT_Y, PLAQUE_TEXT_COLOR, false);
    }
}
