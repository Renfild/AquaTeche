package net.aquatech.ui.client.gui;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.client.render.MachineGuiFx;
import net.aquatech.ui.inventory.TackleBoxMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TackleBoxScreen extends AbstractAquaMachineScreen<TackleBoxMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/tackle_box.png");
    private static final int[] SLOT_XS = {26, 62, 98, 134};

    public TackleBoxScreen(TackleBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, false);
    }

    @Override
    protected void renderMachineOverlays(GuiGraphics guiGraphics, int x, int y, float t, boolean active) {
        for (int i = 0; i < SLOT_XS.length; i++) {
            MachineGuiFx.slotTwinkle(guiGraphics, x + SLOT_XS[i], y + 32, t, i);
        }
    }
}
