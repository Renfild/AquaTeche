package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.FlowerCollectorBlockEntity;
import net.aquatech.machines.inventory.FlowerCollectorMenu;
import net.aquatech.machines.util.MachineLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class FlowerCollectorScreen extends AbstractMachineScreen<FlowerCollectorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/flower_collector.png");

    public FlowerCollectorScreen(FlowerCollectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, MachineLayout.FLOWER_COLLECTOR_ARROW_X, MachineLayout.FLOWER_COLLECTOR_ARROW_Y, 24, 17);
    }

    @Override
    protected void renderMachine(GuiGraphics g, float time) {
        MachineGuiFx.arrow(g, texture, MachineLayout.FLOWER_COLLECTOR_ARROW_X, MachineLayout.FLOWER_COLLECTOR_ARROW_Y, progressFraction(), time);
    }

    /** Вторая шкала у мана-машин показывает ману в соседнем пуле. */
    @Override
    protected void renderSecondBar(GuiGraphics g, float time) {
        MachineGuiFx.bar(g, MachineLayout.BAR2_X, MachineLayout.BAR2_Y, MachineLayout.BAR2_W, MachineLayout.BAR2_H,
                Math.min(1f, menu.getNeighborMana() / (float) MANA_POOL_CAPACITY), 0xFFB072FF, 0xFFE0C8FF, 0xFF7A3FD0, time);
    }

    @Override
    protected void secondBarTooltip(GuiGraphics g, int mouseX, int mouseY) {
        g.renderComponentTooltip(font, List.of(
                Component.literal("§dМана в соседнем пуле: §f" + menu.getNeighborMana() + " §7/ §f" + MANA_POOL_CAPACITY)
        ), mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        // Тултип на зону прогресса: стоимость цикла
        if (inside(mouseX, mouseY, MachineLayout.FLOWER_COLLECTOR_ARROW_X, MachineLayout.FLOWER_COLLECTOR_ARROW_Y, 24, 17)) {
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§dЦветолов: §7ловит цветы Botania из воздуха"),
                    Component.literal("§8Расход: §e9 600 FE §8+ §d250 маны §8за цветок"),
                    Component.literal("§7Мана берётся из соседнего мана-пула (ставь вплотную)"),
                    Component.literal("§2Маны рядом: §f" + menu.getNeighborMana())
            ), mouseX, mouseY);
        }
    }

    @Override
    protected boolean openJeiRecipes() {
        return false;
    }

    @Override
    protected void onProgressBarClicked() {
        printChat("§8[§bAquaTech§8] §d=== Цветолов ===");
        printChat(" §7Ловит случайные цветы Botania из воздуха (162 вида) и складывает в выход.");
        printChat(" §7Ставь вплотную к §dмана-пулу§7: за каждый цветок — 250 маны + 9 600 FE.");
        printChat("§8Поддерживает ускорение до x4, батарею и энергоэффективность.");
    }
}
