package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.ManaFabricatorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ManaFabricatorScreen extends AbstractMachineScreen<ManaFabricatorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/mana_fabricator.png");

    public ManaFabricatorScreen(ManaFabricatorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 52, 36, 8, 20);
    }

    @Override
    protected void renderTooltip(net.minecraft.client.gui.GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        int x = leftPos, y = topPos;
        if (mouseX >= x + 51 && mouseX <= x + 77 && mouseY >= y + 35 && mouseY <= y + 54) {
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
