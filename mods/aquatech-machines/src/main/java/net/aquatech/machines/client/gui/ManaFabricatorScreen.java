package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.ManaFabricatorMenu;
import net.aquatech.machines.util.MachineLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ManaFabricatorScreen extends AbstractMachineScreen<ManaFabricatorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/mana_fabricator.png");

    public ManaFabricatorScreen(ManaFabricatorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, MachineLayout.MANA_FABRICATOR_ARROW_X, MachineLayout.MANA_FABRICATOR_ARROW_Y, 24, 17);
    }

    @Override
    protected void renderMachine(GuiGraphics g, float time) {
        MachineGuiFx.arrow(g, texture, MachineLayout.MANA_FABRICATOR_ARROW_X, MachineLayout.MANA_FABRICATOR_ARROW_Y, progressFraction(), time);
        MachineGuiFx.crystal(g, texture, MachineLayout.MANA_FABRICATOR_CRYSTAL_X, MachineLayout.MANA_FABRICATOR_CRYSTAL_Y,
                MachineLayout.MANA_FABRICATOR_CRYSTAL_W, isWorking(), time);
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
        if (inside(mouseX, mouseY, MachineLayout.MANA_FABRICATOR_ARROW_X, MachineLayout.MANA_FABRICATOR_ARROW_Y, 24, 17)) {
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§dМана-Фабрикатор: §7превращает FE в ману Botania"),
                    Component.literal("§8Расход: §e2 400 FE §8→ §d200 маны §8за цикл (2с)"),
                    Component.literal("§7Мана льётся в соседний мана-пул (ставь вплотную)"),
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
        printChat("§8[§bAquaTech§8] §d=== Мана-Фабрикатор ===");
        printChat(" §7Превращает энергию (FE) в ману Botania и льёт её в соседний пул.");
        printChat(" §7Цикл: §e2 400 FE §7→ §d200 маны§7 каждые 2 секунды.");
        printChat("§8Ставь вплотную к мана-пулу. Поддерживает ускорение, батарею и энергоэффективность.");
    }
}
