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
        super(menu, playerInventory, title, TEXTURE, true, 3, 17, 8, 50);
        this.drawLabels = false;
        this.drawAmbientFx = false;
        this.inventoryLabelY = 74;
    }

    @Override
    protected boolean isMachineActive() {
        return menu.isCrafting();
    }

    @Override
    protected void renderMachineOverlays(GuiGraphics guiGraphics, int x, int y, float t, boolean active) {
        blitEnergy(guiGraphics, x, y, menu.getScaledEnergy(), t);
        if (active) {
            blitProgressArrow(guiGraphics, x, y, 50, 36, menu.getScaledProgress(), t);
        }
    }

    @Override
    protected void blitEnergy(GuiGraphics g, int x, int y, int scaled, float t) {
        if (scaled <= 0) return;
        g.blit(texture, x + energyBarX, y + energyBarY + (energyBarH - scaled),
                176, energyBarH - scaled, energyBarW, scaled);
    }

    @Override
    protected void blitProgressArrow(GuiGraphics g, int x, int y, int slotX, int slotY, int scaled, float t) {
        if (scaled <= 0) return;
        g.blit(texture, x + slotX, y + slotY, 176, 52, scaled, 17);
    }

    @Override
    protected Component energyTooltip() {
        return Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE");
    }
}
