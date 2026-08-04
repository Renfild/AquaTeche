package net.aquatech.ui.client.gui;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.client.render.MachineGuiFx;
import net.aquatech.ui.inventory.OceanAltarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OceanAltarScreen extends AbstractAquaMachineScreen<OceanAltarMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/ocean_altar.png");

    public OceanAltarScreen(OceanAltarMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, false);
    }

    @Override
    protected boolean isMachineActive() {
        return menu.isCrafting();
    }

    @Override
    protected void renderMachineOverlays(GuiGraphics guiGraphics, int x, int y, float t, boolean active) {
        MachineGuiFx.altarPulse(guiGraphics, x + 80, y + 35, t, active);
        if (active) {
            blitProgressArrow(guiGraphics, x, y, 62, 35, menu.getScaledProgress(), t);
        }
    }
}
