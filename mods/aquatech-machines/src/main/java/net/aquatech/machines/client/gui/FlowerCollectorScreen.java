package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.FlowerCollectorBlockEntity;
import net.aquatech.machines.inventory.FlowerCollectorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class FlowerCollectorScreen extends AbstractMachineScreen<FlowerCollectorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/flower_collector.png");

    public FlowerCollectorScreen(FlowerCollectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 52, 36, 8, 20);
    }

    @Override
    protected void renderTooltip(net.minecraft.client.gui.GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        int x = leftPos, y = topPos;
        // Тултип на зону прогресса: стоимость цикла
        if (mouseX >= x + 51 && mouseX <= x + 77 && mouseY >= y + 35 && mouseY <= y + 54) {
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
