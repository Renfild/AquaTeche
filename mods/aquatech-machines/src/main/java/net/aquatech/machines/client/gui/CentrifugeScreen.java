package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.CentrifugeBlockEntity;
import net.aquatech.machines.inventory.CentrifugeMenu;
import net.aquatech.machines.util.MachineLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class CentrifugeScreen extends AbstractMachineScreen<CentrifugeMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/centrifuge.png");

    public CentrifugeScreen(CentrifugeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, MachineLayout.CENTRIFUGE_ARROW_X, MachineLayout.CENTRIFUGE_ARROW_Y, 24, 17);
    }

    @Override
    protected void renderMachine(GuiGraphics g, float time) {
        MachineGuiFx.arrow(g, texture, MachineLayout.CENTRIFUGE_ARROW_X, MachineLayout.CENTRIFUGE_ARROW_Y, progressFraction(), time);
        // Резервуар сырой морской воды
        float raw = menu.getRawWaterAmount() / (float) CentrifugeBlockEntity.TANK_CAPACITY;
        MachineGuiFx.tank(g, MachineLayout.CENTRIFUGE_TANK_RAW_X, MachineLayout.CENTRIFUGE_TANK_RAW_Y,
                MachineLayout.CENTRIFUGE_TANK_RAW_W, MachineLayout.CENTRIFUGE_TANK_RAW_H, raw, 0xFF1565C0, 0xFF42A5F5, 0xFF0D47A1, time);
        // Резервуар очищенного дистиллята
        float distillate = menu.getDistillateAmount() / (float) CentrifugeBlockEntity.TANK_CAPACITY;
        MachineGuiFx.tank(g, MachineLayout.CENTRIFUGE_TANK_DIST_X, MachineLayout.CENTRIFUGE_TANK_DIST_Y,
                MachineLayout.CENTRIFUGE_TANK_DIST_W, MachineLayout.CENTRIFUGE_TANK_DIST_H, distillate, 0xFF00E5FF, 0xFFE0F7FA, 0xFF00B0FF, time);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);

        if (inside(mouseX, mouseY, MachineLayout.CENTRIFUGE_TANK_RAW_X - 2, MachineLayout.CENTRIFUGE_TANK_RAW_Y - 2,
                MachineLayout.CENTRIFUGE_TANK_RAW_W + 4, MachineLayout.CENTRIFUGE_TANK_RAW_H + 4)) {
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§9Резервуар морской воды: §f" + menu.getRawWaterAmount() + " §7/ §f" + CentrifugeBlockEntity.TANK_CAPACITY + " mB"),
                    Component.literal("§8Расход: §e1,000 mB §8на цикл сепарации"),
                    Component.literal("§7Заполняется ведрами с водой или трубами")
            ), mouseX, mouseY);
        }
        if (inside(mouseX, mouseY, MachineLayout.CENTRIFUGE_TANK_DIST_X - 2, MachineLayout.CENTRIFUGE_TANK_DIST_Y - 2,
                MachineLayout.CENTRIFUGE_TANK_DIST_W + 4, MachineLayout.CENTRIFUGE_TANK_DIST_H + 4)) {
            g.renderComponentTooltip(font, List.of(
                    Component.literal("§bРезервуар дистиллята: §f" + menu.getDistillateAmount() + " §7/ §f" + CentrifugeBlockEntity.TANK_CAPACITY + " mB"),
                    Component.literal("§8Выход: §a800 mB §8очищенной воды за цикл"),
                    Component.literal("§7Откачивается трубами или забирается ведрами/колбами")
            ), mouseX, mouseY);
        }
        // Матрица минералов: подсказка на пустых слотах сетки 2x2
        net.minecraft.world.inventory.Slot slot = this.getSlotUnderMouse();
        if (slot != null && !slot.hasItem() && inside(mouseX, mouseY, MachineLayout.CENTRIFUGE_MINERAL0_X - 3, MachineLayout.CENTRIFUGE_MINERAL0_Y - 3, 42, 42)) {
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
