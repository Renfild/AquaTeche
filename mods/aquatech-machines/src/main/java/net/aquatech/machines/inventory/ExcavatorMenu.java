package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.ExcavatorBlockEntity;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

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
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(output(row * 3 + col, 79 + col * 18, 17 + row * 18));
            }
        }
    }

    public BlockPos getPos() {
        return pos;
    }
}
