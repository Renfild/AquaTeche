package net.aquatech.ui.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aquatech.ui.client.render.MachineGuiFx;
import net.aquatech.ui.client.render.UiDraw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Shared chrome for AquaTech machine screens with soft ambient motion.
 */
public abstract class AbstractAquaMachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    protected final ResourceLocation texture;
    protected final boolean showEnergy;
    protected final int energyBarX;
    protected final int energyBarY;
    protected final int energyBarW;
    protected final int energyBarH;
    protected float animPartial;
    /** When false, skips title / inventory caption drawn by vanilla labels. */
    protected boolean drawLabels = true;
    /** Soft MachineGuiFx chrome (accent line, shimmer, bubbles, dots). */
    protected boolean drawAmbientFx = true;

    protected AbstractAquaMachineScreen(T menu, Inventory playerInventory, Component title,
                                        ResourceLocation texture, boolean showEnergy) {
        this(menu, playerInventory, title, texture, showEnergy, 8, 18, 12, 52);
    }

    protected AbstractAquaMachineScreen(T menu, Inventory playerInventory, Component title,
                                        ResourceLocation texture, boolean showEnergy,
                                        int energyBarX, int energyBarY, int energyBarW, int energyBarH) {
        super(menu, playerInventory, title);
        this.texture = texture;
        this.showEnergy = showEnergy;
        this.energyBarX = energyBarX;
        this.energyBarY = energyBarY;
        this.energyBarW = energyBarW;
        this.energyBarH = energyBarH;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    protected boolean isMachineActive() {
        return false;
    }

    protected int fxSeed() {
        return texture.getPath().hashCode();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.animPartial = partialTick;
        float t = MachineGuiFx.time(partialTick);
        boolean active = isMachineActive();

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = leftPos;
        int y = topPos;
        guiGraphics.blit(texture, x, y, 0, 0, imageWidth, imageHeight);

        if (drawAmbientFx) {
            MachineGuiFx.accentPulse(guiGraphics, x, y, imageWidth, t);
            MachineGuiFx.scanShimmer(guiGraphics, x, y, imageWidth, t, active);
            MachineGuiFx.bubbles(guiGraphics, x, y, t, active, fxSeed());
        }

        renderMachineOverlays(guiGraphics, x, y, t, active);
        if (drawAmbientFx) {
            MachineGuiFx.workingDots(guiGraphics, x + imageWidth - 22, y + 7, t, active);
        }
    }

    protected abstract void renderMachineOverlays(GuiGraphics guiGraphics, int x, int y, float t, boolean active);

    protected void blitEnergy(GuiGraphics g, int x, int y, int scaled, float t) {
        if (scaled <= 0) return;
        g.blit(texture, x + energyBarX, y + energyBarY + (energyBarH - scaled),
                176, energyBarH - scaled, energyBarW, scaled);
        MachineGuiFx.energyPulse(g, x + energyBarX, y + energyBarY, energyBarW, energyBarH, scaled, t);
    }

    protected void blitProgressArrow(GuiGraphics g, int x, int y, int slotX, int slotY, int scaled, float t) {
        if (scaled <= 0) return;
        g.blit(texture, x + slotX, y + slotY, 176, 52, scaled, 17);
        MachineGuiFx.progressGlow(g, x + slotX, y + slotY, scaled, t);
    }

    protected void blitBurnFlame(GuiGraphics g, int x, int y, int slotX, int slotY, int scaled, float t) {
        if (scaled <= 0) return;
        g.blit(texture, x + slotX, y + slotY + (14 - scaled), 176, 70 + (14 - scaled), 14, scaled);
        MachineGuiFx.flameFlicker(g, x + slotX, y + slotY, scaled, t);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
        if (showEnergy && isHovering(energyBarX, energyBarY, energyBarW, energyBarH, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, energyTooltip(), mouseX, mouseY);
        }
    }

    protected Component energyTooltip() {
        return Component.literal("0 / 0 FE");
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!drawLabels) {
            return;
        }
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, UiDraw.COLOR_PRIMARY, false);
        if (isMachineActive()) {
            guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                    MachineGuiFx.withAlpha(0x7DD3FC, 0.35F), false);
        }
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, UiDraw.COLOR_MUTED, false);
    }
}
