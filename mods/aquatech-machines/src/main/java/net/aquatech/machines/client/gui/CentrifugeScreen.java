package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.CentrifugeBlockEntity;
import net.aquatech.machines.inventory.CentrifugeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class CentrifugeScreen extends AbstractMachineScreen<CentrifugeMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/centrifuge.png");

    public CentrifugeScreen(CentrifugeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 65, 37, 8, 20);
    }

    @Override
    protected boolean hasUpgradeWing() {
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        super.renderBg(g, partialTick, mouseX, mouseY);
        int x = leftPos, y = topPos;

        // 1. Уровень сырой морской воды в баке (24, 22) 12x50 px
        int raw = menu.getRawWaterAmount();
        int maxRaw = CentrifugeBlockEntity.TANK_CAPACITY;
        int rawHeight = (int) Math.min(50, ((long) raw * 50) / maxRaw);
        if (rawHeight > 0) {
            // Глубокий океанический синий
            g.fill(x + 24, y + 22 + (50 - rawHeight), x + 36, y + 72, 0xFF1565C0);
            // Водяной зеркальный блик
            g.fill(x + 25, y + 22 + (50 - rawHeight), x + 27, y + 72, 0xFF42A5F5);
            // Тень глубины резервуара
            g.fill(x + 33, y + 22 + (50 - rawHeight), x + 35, y + 72, 0xFF0D47A1);
        }

        // 2. Уровень очищенного дистиллята в баке (138, 22) 12x50 px
        int dist = menu.getDistillateAmount();
        int maxDist = CentrifugeBlockEntity.TANK_CAPACITY;
        int distHeight = (int) Math.min(50, ((long) dist * 50) / maxDist);
        if (distHeight > 0) {
            // Кристальный циан ультраочищенной воды
            g.fill(x + 138, y + 22 + (50 - distHeight), x + 150, y + 72, 0xFF00E5FF);
            // Ультрачистый белый блик
            g.fill(x + 139, y + 22 + (50 - distHeight), x + 141, y + 72, 0xFFE0F7FA);
            // Тень резервуара
            g.fill(x + 147, y + 22 + (50 - distHeight), x + 149, y + 72, 0xFF00B0FF);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);

        int x = leftPos, y = topPos;

        // Тултип бака сырой морской воды (23..36, 21..72)
        if (mouseX >= x + 23 && mouseX <= x + 36 && mouseY >= y + 21 && mouseY <= y + 72) {
            List<Component> tip = List.of(
                    Component.literal("§9Резервуар морской воды: §f" + menu.getRawWaterAmount() + " §7/ §f" + CentrifugeBlockEntity.TANK_CAPACITY + " mB"),
                    Component.literal("§8Расход: §e1,000 mB §8на цикл сепарации"),
                    Component.literal("§7Заполняется ведрами с водой или трубами")
            );
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }

        // Тултип бака дистиллята (137..150, 21..72)
        if (mouseX >= x + 137 && mouseX <= x + 150 && mouseY >= y + 21 && mouseY <= y + 72) {
            List<Component> tip = List.of(
                    Component.literal("§bРезервуар дистиллята: §f" + menu.getDistillateAmount() + " §7/ §f" + CentrifugeBlockEntity.TANK_CAPACITY + " mB"),
                    Component.literal("§8Выход: §a800 mB §8очищенной воды за цикл"),
                    Component.literal("§7Откачивается трубами или забирается ведрами/колбами")
            );
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }

        // Тултип для матрицы сепарации минералов (94..134, 25..68) при наведении на пустые слоты
        if (mouseX >= x + 94 && mouseX <= x + 134 && mouseY >= y + 25 && mouseY <= y + 68) {
            net.minecraft.world.inventory.Slot s = this.getSlotUnderMouse();
            if (s != null && !s.hasItem()) {
                g.renderComponentTooltip(font, List.of(
                        Component.literal("§6Матрица сепарации минералов"),
                        Component.literal("§7Экстрагирует из морской воды:"),
                        Component.literal(" §f• Морскую соль §8(1-2 шт, 100%)"),
                        Component.literal(" §c• Литий / Редстоун §8(70%)"),
                        Component.literal(" §6• Золотой самородок §8(45%)"),
                        Component.literal(" §b• Осколок призмарина §8(30%)")
                ), mouseX, mouseY);
            }
        }
    }

    @Override
    protected boolean openJeiRecipes() {
        return net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.showRecipes(
                net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.CENTRIFUGE_TYPE);
    }

    @Override
    protected void onProgressBarClicked() {
        printChat("§8[§bAquaTech§8] §3=== Сепарация в Центрифуге ===");
        printChat(" §7• §9Морская вода §7(1,000 mB) → §bДистиллят §7(800 mB) + §fМорская соль §7(1-2 шт)");
        printChat(" §7• §eШанс минералов: §cЛитий/Редстоун (70%)§7, §6Золото (45%)§7, §bПризмарин (30%)");
        printChat("§8[§7Поддерживает автоматизацию трубами для бесконечной опреснительной станции!§8]");
    }
}
