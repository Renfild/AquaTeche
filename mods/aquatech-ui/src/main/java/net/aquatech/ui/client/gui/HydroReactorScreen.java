package net.aquatech.ui.client.gui;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.inventory.HydroReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HydroReactorScreen extends AbstractAquaMachineScreen<HydroReactorMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/hydro_reactor.png");

    public HydroReactorScreen(HydroReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, true, 152, 18, 12, 52);
    }

    @Override
    protected boolean isMachineActive() {
        return menu.isBurning();
    }

    @Override
    protected void renderMachineOverlays(GuiGraphics guiGraphics, int x, int y, float t, boolean active) {
        blitEnergy(guiGraphics, x, y, menu.getScaledEnergy(), t);
        if (active) {
            int flame = Math.max(1, menu.getScaledBurn() * 14 / 24);
            blitBurnFlame(guiGraphics, x, y, 81, 55, flame, t);
        }
    }

    @Override
    protected Component energyTooltip() {
        return Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE");
    }
}
