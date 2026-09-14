package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.ManaFabricatorBlockEntity;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class ManaFabricatorMenu extends BaseMachineMenu {

    private final BlockPos pos;

    public ManaFabricatorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ManaFabricatorMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.MANA_FABRICATOR.get(), id, inv, be, BaseMachineMenu.makeData((ManaFabricatorBlockEntity) be));
        this.pos = be.getBlockPos();
    }

    @Override
    protected void addMachineSlots(Inventory inv) {
        // [0] Батарея, [1] Скорость, [2] Энергоэффективность (выносное крыло)
        addSlot(batterySlot(ManaFabricatorBlockEntity.SLOT_BATTERY, 185, 19));
        addSlot(speedUpgradeSlot(ManaFabricatorBlockEntity.SLOT_SPEED, 185, 41));
        addSlot(efficiencyUpgradeSlot(ManaFabricatorBlockEntity.SLOT_EFF, 185, 63));
    }

    public int getNeighborMana() {
        return ((ManaFabricatorBlockEntity) blockEntity).neighborMana();
    }

    public BlockPos getPos() {
        return pos;
    }
}
