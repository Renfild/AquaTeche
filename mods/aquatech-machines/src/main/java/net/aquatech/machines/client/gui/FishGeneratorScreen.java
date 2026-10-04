package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.FishGeneratorMenu;
import net.aquatech.machines.util.FishGeneratorLogic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/** Экран Рыбного генератора: слот рыбы, шкала горения и столбик энергии из той же раскладки, что у машин. */
public class FishGeneratorScreen extends AbstractContainerScreen<FishGeneratorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/fish_generator.png");
    private static final int ENERGY_X = 8;
    private static final int ENERGY_Y = 20;
    private static final int BURN_X = 74;
    private static final int BURN_Y = 36;

    public FishGeneratorScreen(FishGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, delta);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        int burn = menu.getScaledBurn(24);
        if (burn > 0) {
            g.blit(TEXTURE, leftPos + BURN_X, topPos + BURN_Y, 176, 52, burn, 17, 256, 256);
        }

        int max = menu.getMaxEnergy();
        int height = max <= 0 ? 0 : (int) Math.min(50, ((long) menu.getEnergy() * 50) / max);
        if (height > 0) {
            g.blit(TEXTURE, leftPos + ENERGY_X, topPos + ENERGY_Y + (50 - height), 176, 50 - height, 12, height, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xDEE3EA, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        if (inside(mouseX, mouseY, ENERGY_X, ENERGY_Y, 12, 50)) {
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§bЭнергия: §f" + menu.getEnergy() + " §7/ §f" + menu.getMaxEnergy() + " FE"),
                    Component.literal("§8Отдаёт до §e" + FishGeneratorLogic.MAX_EXTRACT + " FE/t §8соседним машинам")
            ), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, BURN_X, BURN_Y, 24, 17)) {
            int seconds = (menu.getBurnTime() + 19) / 20;
            g.renderComponentTooltip(font, List.of(
                    Component.literal(menu.getBurnTime() > 0 ? "§6Горит ещё §f" + seconds + " с" : "§7Нет топлива в огне"),
                    Component.literal("§8Даёт §e" + FishGeneratorLogic.FE_PER_TICK + " FE/t§8. Топливо: уголь, дерево, любое горючее или рыба")
            ), mouseX, mouseY);
        }
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        int left = leftPos + x;
        int top = topPos + y;
        return mouseX >= left && mouseX <= left + w && mouseY >= top && mouseY <= top + h;
    }
}
