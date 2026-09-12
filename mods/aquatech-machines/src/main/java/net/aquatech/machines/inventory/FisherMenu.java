package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.FisherBlockEntity;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class FisherMenu extends BaseMachineMenu {

    private final BlockPos pos;

    public FisherMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public FisherMenu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.FISHER.get(), id, inv, be, makeData((FisherBlockEntity) be));
        this.pos = be.getBlockPos();
    }


    @Override
    protected void addMachineSlots(Inventory inv) {
        addSlot(new SlotItemHandler(blockEntity.getItems(), 0, 44, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof FishingRodItem
                        || BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("starcatcher");
            }
        });
        addSlot(output(1, 116, 35));
    }

    public BlockPos getPos() {
        return pos;
    }
}
