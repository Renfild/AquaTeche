package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.BaseMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Общий экран механизма: фон-текстура, стрелка прогресса (24x17 @ 79,34),
 * полоса энергии (12x50 @ 8,18), без надписи «Инвентарь».
 */
public abstract class AbstractMachineScreen<T extends BaseMachineMenu> extends AbstractContainerScreen<T> {

    protected final ResourceLocation texture;

    protected AbstractMachineScreen(T menu, Inventory inv, Component title, ResourceLocation texture) {
        super(menu, inv, title);
        this.texture = texture;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 5;
    }

    protected abstract int progressU();

    protected abstract int progressV();

    protected ResourceLocation texture() {
        return texture;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, delta);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.blit(texture(), x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        int progress = menu.getScaledProgress(24);
        if (progress > 0) {
            g.blit(texture(), x + 79, y + 34, 176, 52, progress, 17, 256, 256);
        }

        int energy = menu.getMaxEnergy() == 0 ? 0 : menu.getEnergy() * 50 / menu.getMaxEnergy();
        if (energy > 0) {
            g.blit(texture(), x + 8, y + 18 + (50 - energy), 176, 0, 12, energy, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
    }
}
