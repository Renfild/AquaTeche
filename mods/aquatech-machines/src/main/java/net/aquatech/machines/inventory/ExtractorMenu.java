package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.ExtractorBlockEntity;
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

public class ExtractorMenu extends BaseMachineMenu {

    private final BlockPos pos;

    public ExtractorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ExtractorMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.EXTRACTOR.get(), id, inv, be, makeData((ExtractorBlockEntity) be));
        this.pos = be.getBlockPos();
    }


    @Override
    protected void addMachineSlots(Inventory inv) {
        addSlot(new SlotItemHandler(blockEntity.getItems(), 0, 44, 17));
        addSlot(output(1, 116, 35));
    }

    public BlockPos getPos() {
        return pos;
    }
}
