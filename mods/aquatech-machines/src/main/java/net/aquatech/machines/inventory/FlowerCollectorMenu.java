package net.aquatech.machines.inventory;

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
        addSlot(output(FlowerCollectorBlockEntity.SLOT_OUTPUT_1, 79, 37));
        addSlot(output(FlowerCollectorBlockEntity.SLOT_OUTPUT_2, 103, 37));
        addSlot(output(FlowerCollectorBlockEntity.SLOT_OUTPUT_3, 127, 37));
        // [3] Апгрейд скорости, [4] Батарея, [5] Энергоэффективность (выносное крыло)
        addSlot(speedUpgradeSlot(FlowerCollectorBlockEntity.SLOT_SPEED, 185, 19));
        addSlot(batterySlot(FlowerCollectorBlockEntity.SLOT_BATTERY, 185, 41));
        addSlot(efficiencyUpgradeSlot(FlowerCollectorBlockEntity.SLOT_EFF, 185, 63));
    }

    public int getNeighborMana() {
        return ((FlowerCollectorBlockEntity) blockEntity).neighborMana();
    }

    public BlockPos getPos() {
        return pos;
    }
}
