package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.ExcavatorBlockEntity;
import net.aquatech.machines.inventory.ExcavatorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class ExcavatorScreen extends AbstractMachineScreen<ExcavatorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/excavator.png");

    public ExcavatorScreen(ExcavatorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 48, 36, 8, 20);
    }

    @Override
    protected boolean openJeiRecipes() {
        return net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.showRecipes(
                net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.EXCAVATOR_TYPE);
    }

    @Override
    protected void onProgressBarClicked() {
        printChat("§8[§bAquaTech§8] §6=== Ресурсы Экскаватора (Шансы) ===");
        int totalWeight = 0;
        for (ExcavatorBlockEntity.Entry e : ExcavatorBlockEntity.POOL) {
            totalWeight += e.weight();
        }
        for (ExcavatorBlockEntity.Entry e : ExcavatorBlockEntity.POOL) {
            double pct = (e.weight() * 100.0) / (double) totalWeight;
            String count = (e.min() == e.max()) ? (e.min() + " шт") : (e.min() + "-" + e.max() + " шт");
            printChat(String.format(Locale.ROOT, " §7• §f%s §8(%s): §a%.1f%%", e.label(), count, pct));
        }
        printChat("§8[§7Шансы рассчитаны на каждый цикл работы. Поддерживает апгрейды скорости и энергоэффективности§8]");
    }
}
