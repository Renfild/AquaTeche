package net.aquatech.ui.client.gui;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.inventory.FishSmokerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FishSmokerScreen extends AbstractAquaMachineScreen<FishSmokerMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/fish_smoker.png");

    public FishSmokerScreen(FishSmokerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, false);
        this.inventoryLabelY = 74;
    }

    @Override
    protected boolean isMachineActive() {
        return menu.isCrafting();
    }

    @Override
    protected void renderMachineOverlays(GuiGraphics guiGraphics, int x, int y, float t, boolean active) {
        int burn = menu.getScaledBurn();
        if (burn > 0) {
            blitBurnFlame(guiGraphics, x, y, 57, 37, burn, t);
        }
        if (active) {
            blitProgressArrow(guiGraphics, x, y, 79, 34, menu.getScaledProgress(), t);
        }
    }
}
