package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.FisherMk1BlockEntity;
import net.aquatech.machines.inventory.FisherMk1Menu;
import net.aquatech.machines.util.MachineLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FisherMk1Screen extends AbstractMachineScreen<FisherMk1Menu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/fisher_mk1.png");

    public FisherMk1Screen(FisherMk1Menu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, MachineLayout.FISHER_MK1_POND_X, MachineLayout.FISHER_MK1_POND_Y, MachineLayout.FISHER_MK1_POND_W, MachineLayout.FISHER_MK1_POND_H);
    }

    @Override
    protected boolean hasUpgradeWing() {
        return false;
    }

    @Override
    protected void renderMachine(GuiGraphics g, float time) {
        MachineGuiFx.pond(g, texture, MachineLayout.FISHER_MK1_POND_X, MachineLayout.FISHER_MK1_POND_Y, MachineLayout.FISHER_MK1_POND_W,
                MachineLayout.FISHER_MK1_POND_H, progressFraction(), isWorking(), time);
    }

    @Override
    protected void onProgressBarClicked() {
        printChat("§8[§bAquaTech§8] §6=== Рыболов MK-1 ===");
        printChat(" §7Раз в §e5 секунд §7добывает один ресурс и тратит §e" + FisherMk1BlockEntity.ENERGY_PER_TICK
                + " FE/t §7(§e" + FisherMk1BlockEntity.ENERGY_PER_TICK * FisherMk1BlockEntity.CYCLE_TICKS + " FE §7на улов).");
        if (!(menu.getBlockEntity() instanceof FisherMk1BlockEntity fisher)) {
            return;
        }
        ItemStack rod = fisher.getItems().getStackInSlot(FisherMk1BlockEntity.SLOT_ROD);
        if (rod.isEmpty()) {
            printChat(" §c• Вставьте удочку StarCatcher в верхний левый слот.");
            return;
        }
        printChat(" §eУдочка: §b" + rod.getHoverName().getString() + " §7(тир §a" + fisher.rodTier() + "§7)");
        printChat(" §7Ресурсы те же, что даёт сама удочка при обычной ловле: по одному стеку из её улова за цикл.");
        printChat(" §7Энергию даёт Рыбный генератор рядом, батарея в нижнем слоте или редстоун.");
    }
}
