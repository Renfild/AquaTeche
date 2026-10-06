package net.aquatech.ui.client.gui;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.block.entity.KeepnetBlockEntity;
import net.aquatech.ui.inventory.KeepnetMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Экран садка: текстура 256x256, видимая часть 256x212 (рисует tools/build_keepnet_gui.py). */
public class KeepnetScreen extends AbstractContainerScreen<KeepnetMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/fish_keepnet.png");
    /** Табличка с названием занимает (60, 2) .. (195, 17) в координатах GUI. */
    private static final int PLAQUE_CENTER_X = 128;
    private static final int PLAQUE_TEXT_Y = 6;
    private static final int PLAQUE_TEXT_COLOR = 0x22262C;

    public KeepnetScreen(KeepnetMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 212;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component label = Component.empty().append(title).append(" " + menu.filledSlots() + "/" + KeepnetBlockEntity.SLOTS);
        guiGraphics.drawString(font, label, PLAQUE_CENTER_X - font.width(label) / 2, PLAQUE_TEXT_Y, PLAQUE_TEXT_COLOR, false);
    }
}
