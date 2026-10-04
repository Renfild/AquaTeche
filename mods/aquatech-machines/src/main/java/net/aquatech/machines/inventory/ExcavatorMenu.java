package net.aquatech.machines.inventory;

import net.aquatech.machines.util.MachineLayout;
import net.aquatech.machines.block.entity.ExcavatorBlockEntity;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ExcavatorMenu extends BaseMachineMenu {

    private final BlockPos pos;

    public ExcavatorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ExcavatorMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.EXCAVATOR.get(), id, inv, be, makeData((ExcavatorBlockEntity) be));
        this.pos = be.getBlockPos();
    }

    @Override
    protected void addMachineSlots(Inventory inv) {
        // [0..8] Сетка выходов 3x3
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(output(row * 3 + col, MachineLayout.EXCAVATOR_OUT0_X + col * 18, MachineLayout.EXCAVATOR_OUT0_Y + row * 18));
            }
        }
        // [9] Апгрейд скорости (выносное крыло)
        addSlot(speedUpgradeSlot(ExcavatorBlockEntity.SLOT_SPEED, MachineLayout.UPG_X, MachineLayout.UPG_SPEED_Y));
        // [10] Слот батареи / аккумулятора (выносное крыло)
        addSlot(batterySlot(ExcavatorBlockEntity.SLOT_BATTERY, MachineLayout.UPG_X, MachineLayout.UPG_BATTERY_Y));
        // [11] Апгрейд энергоэффективности (выносное крыло)
        addSlot(efficiencyUpgradeSlot(ExcavatorBlockEntity.SLOT_EFF, MachineLayout.UPG_X, MachineLayout.UPG_EFF_Y));
    }

    public BlockPos getPos() {
        return pos;
    }
}
