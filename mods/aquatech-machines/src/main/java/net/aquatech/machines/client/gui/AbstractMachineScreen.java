package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.BaseMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Общий экран механизма. Координаты прогресса/энергии задаются наследником
 * и обязаны совпадать с GUI-текстурой (бокс-в-бокс).
 * Стрелка-заливка: strip (176,52) 24x17. Энергия: strip (176,0) 12x50, растёт снизу вверх.
 */
public abstract class AbstractMachineScreen<T extends BaseMachineMenu> extends AbstractContainerScreen<T> {

    protected final ResourceLocation texture;
    protected final int progressX;
    protected final int progressY;
    protected final int energyX;
    protected final int energyY;

    protected AbstractMachineScreen(T menu, Inventory inv, Component title, ResourceLocation texture,
                                    int progressX, int progressY, int energyX, int energyY) {
        super(menu, inv, title);
        this.texture = texture;
        this.progressX = progressX;
        this.progressY = progressY;
        this.energyX = energyX;
        this.energyY = energyY;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 5;
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
        g.blit(texture, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        // Прогресс: лево-право по strip (176,52)
        int progress = menu.getScaledProgress(24);
        if (progress > 0) {
            g.blit(texture, x + progressX, y + progressY, 176, 52, progress, 17, 256, 256);
        }

        // Энергия: низ-верх по strip (176,0) 12x50
        int max = menu.getMaxEnergy();
        int energy = max == 0 ? 0 : menu.getEnergy() * 50 / max;
        if (energy > 0) {
            g.blit(texture, x + energyX, y + energyY + (50 - energy), 176, 50 - energy, 12, energy, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
    }
}
