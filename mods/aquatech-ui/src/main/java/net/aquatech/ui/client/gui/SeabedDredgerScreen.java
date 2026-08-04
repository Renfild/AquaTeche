package net.aquatech.ui.client.gui;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.inventory.SeabedDredgerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SeabedDredgerScreen extends AbstractAquaMachineScreen<SeabedDredgerMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/seabed_dredger.png");

    public SeabedDredgerScreen(SeabedDredgerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, true, 8, 18, 10, 52);
    }

    @Override
    protected boolean isMachineActive() {
        return menu.isCrafting();
    }

    @Override
    protected void renderMachineOverlays(GuiGraphics guiGraphics, int x, int y, float t, boolean active) {
        blitEnergy(guiGraphics, x, y, menu.getScaledEnergy(), t);
        if (active) {
            blitProgressArrow(guiGraphics, x, y, 70, 35, menu.getScaledProgress(), t);
        }
    }

    @Override
    protected Component energyTooltip() {
        return Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE");
    }
}
