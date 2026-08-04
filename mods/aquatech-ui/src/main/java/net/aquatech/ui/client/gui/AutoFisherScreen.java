package net.aquatech.ui.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.inventory.AutoFisherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AutoFisherScreen extends AbstractAquaMachineScreen<AutoFisherMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AquaTechUI.MOD_ID, "textures/gui/auto_fisher.png");

    /** Radar dial on the baked GUI (concentric circles). */
    private static final int RADAR_X = 61;
    private static final int RADAR_Y = 29;
    /** Frames in the atlas column to the right of the GUI (x ≥ 176). */
    private static final int RADAR_FRAME_U = 180;
    private static final int RADAR_FRAME_V0 = 89;
    private static final int RADAR_FRAME_SIZE = 16;
    private static final int RADAR_FRAME_STRIDE = 17;
    private static final int RADAR_FRAMES = 7;
    /** Scale 16px frames up to cover the ~32px radar well. */
    private static final float RADAR_SCALE = 2.0F;
    /** Game ticks per frame (~6.7 fps). */
    private static final int RADAR_TICKS_PER_FRAME = 3;

    /**
     * Progress arrow sprite in atlas: top-left (180,58) → bottom-right (198,76) = 18×18.
     * Drawn on the GUI between sonar and outputs at (100, 36).
     */
    private static final int PROG_GUI_X = 100;
    private static final int PROG_GUI_Y = 36;
    private static final int PROG_U = 180;
    private static final int PROG_V = 58;
    private static final int PROG_H = 18; // 76 - 58

    /**
     * Energy fill strip in atlas (NOT the default 176 used by other machines).
     * Gradient is at u=182..193, first solid row v=2, height 49.
     */
    private static final int ENERGY_FILL_U = 182;
    private static final int ENERGY_FILL_V = 2;

    public AutoFisherScreen(AutoFisherMenu menu, Inventory playerInventory, Component title) {
        // Energy well on texture: x=37 y=21 w=12 h=49
        super(menu, playerInventory, title, TEXTURE, true, 37, 21, 12, 49);
        // Full GUI including player inventory (your art size)
        this.imageWidth = 177;
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
        if (active) {
            blitRadarAnimation(guiGraphics, x, y);
        }
    }

    /** Pixel-perfect energy fill from your drawn strip; no FX overlays. */
    private void blitEnergyClean(GuiGraphics g, int x, int y, int scaled) {
        if (scaled <= 0) return;
        int h = Math.min(scaled, energyBarH);
        g.blit(TEXTURE,
                x + energyBarX, y + energyBarY + (energyBarH - h),
                ENERGY_FILL_U, ENERGY_FILL_V + (energyBarH - h),
                energyBarW, h);
    }

    /** Horizontal fill of the custom 18×18 arrow as fishing progress advances. */
    private void blitProgressArrow(GuiGraphics g, int x, int y) {
        int scaled = menu.getScaledProgress();
        if (scaled <= 0) return;
        g.blit(TEXTURE,
                x + PROG_GUI_X, y + PROG_GUI_Y,
                PROG_U, PROG_V,
                Math.min(scaled, 18), PROG_H);
    }

    /** Cycles the drawn radar frames from the right side of auto_fisher.png. */
    private void blitRadarAnimation(GuiGraphics g, int x, int y) {
        long ticks = (minecraft != null && minecraft.level != null)
                ? minecraft.level.getGameTime()
                : System.currentTimeMillis() / 50L;
        int frame = (int) ((ticks / RADAR_TICKS_PER_FRAME) % RADAR_FRAMES);
        int v = RADAR_FRAME_V0 + frame * RADAR_FRAME_STRIDE;

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x + RADAR_X, y + RADAR_Y, 0);
        pose.scale(RADAR_SCALE, RADAR_SCALE, 1.0F);
        g.blit(TEXTURE, 0, 0, RADAR_FRAME_U, v, RADAR_FRAME_SIZE, RADAR_FRAME_SIZE);
        pose.popPose();
    }

    @Override
    protected Component energyTooltip() {
        return Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE");
    }
}
