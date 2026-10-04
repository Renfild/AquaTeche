package net.aquatech.machines.inventory;

import net.aquatech.machines.util.MachineLayout;
import net.aquatech.machines.block.entity.FlowerCollectorBlockEntity;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class FlowerCollectorMenu extends BaseMachineMenu {

    private final BlockPos pos;

    public FlowerCollectorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public FlowerCollectorMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.FLOWER_COLLECTOR.get(), id, inv, be, BaseMachineMenu.makeData((FlowerCollectorBlockEntity) be));
        this.pos = be.getBlockPos();
    }

    @Override
    protected void addMachineSlots(Inventory inv) {
        // [0..2] Три слота выхода (собранные цветы/лепестки)
        addSlot(output(FlowerCollectorBlockEntity.SLOT_OUTPUT_1, MachineLayout.FLOWER_COLLECTOR_OUT1_X, MachineLayout.FLOWER_COLLECTOR_OUT1_Y));
        addSlot(output(FlowerCollectorBlockEntity.SLOT_OUTPUT_2, MachineLayout.FLOWER_COLLECTOR_OUT2_X, MachineLayout.FLOWER_COLLECTOR_OUT2_Y));
        addSlot(output(FlowerCollectorBlockEntity.SLOT_OUTPUT_3, MachineLayout.FLOWER_COLLECTOR_OUT3_X, MachineLayout.FLOWER_COLLECTOR_OUT3_Y));
        // [3] Апгрейд скорости, [4] Батарея, [5] Энергоэффективность (выносное крыло)
        addSlot(speedUpgradeSlot(FlowerCollectorBlockEntity.SLOT_SPEED, MachineLayout.UPG_X, MachineLayout.UPG_SPEED_Y));
        addSlot(batterySlot(FlowerCollectorBlockEntity.SLOT_BATTERY, MachineLayout.UPG_X, MachineLayout.UPG_BATTERY_Y));
        addSlot(efficiencyUpgradeSlot(FlowerCollectorBlockEntity.SLOT_EFF, MachineLayout.UPG_X, MachineLayout.UPG_EFF_Y));
    }

    public int getNeighborMana() {
        return ((FlowerCollectorBlockEntity) blockEntity).neighborMana();
    }

    public BlockPos getPos() {
        return pos;
    }
}
