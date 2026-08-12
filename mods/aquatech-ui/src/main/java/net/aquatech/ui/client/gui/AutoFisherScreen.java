package net.aquatech.ui.client.gui;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.inventory.AutoFisherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AutoFisherScreen extends AbstractAquaMachineScreen<AutoFisherMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/auto_fisher.png");

    /** Arrow outline on GUI; filled strip in atlas at (180,61). */
    private static final int PROG_GUI_X = 79;
    private static final int PROG_GUI_Y = 40;
    private static final int PROG_U = 180;
    private static final int PROG_V = 61;
    private static final int PROG_H = 13;

    private static final int ENERGY_FILL_U = 182;
    private static final int ENERGY_FILL_V = 2;

    public AutoFisherScreen(AutoFisherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, true, 37, 21, 12, 49);
        this.imageWidth = 176;
        this.imageHeight = 166;
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
        blitEnergyClean(guiGraphics, x, y, menu.getScaledEnergy());
        blitProgressArrow(guiGraphics, x, y);
    }

    private void blitEnergyClean(GuiGraphics g, int x, int y, int scaled) {
        if (scaled <= 0) return;
        int h = Math.min(scaled, energyBarH);
        g.blit(TEXTURE,
                x + energyBarX, y + energyBarY + (energyBarH - h),
                ENERGY_FILL_U, ENERGY_FILL_V + (energyBarH - h),
                energyBarW, h);
    }

    private void blitProgressArrow(GuiGraphics g, int x, int y) {
        int scaled = menu.getScaledProgress();
        if (scaled <= 0) return;
        g.blit(TEXTURE,
                x + PROG_GUI_X, y + PROG_GUI_Y,
                PROG_U, PROG_V,
                Math.min(scaled, AutoFisherMenu.PROGRESS_WIDTH), PROG_H);
    }

    @Override
    protected Component energyTooltip() {
        return Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE");
    }
}
