package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.FisherMk1BlockEntity;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class FisherMk1Menu extends BaseMachineMenu {

    public FisherMk1Menu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public FisherMk1Menu(int id, Inventory inv, BlockEntity be) {
        super(ModMenuTypes.FISHER_MK1.get(), id, inv, be, makeData((FisherMk1BlockEntity) be));
    }

    @Override
    protected void addMachineSlots(Inventory inv) {
        // [0] Удочка StarCatcher
        addSlot(new SlotItemHandler(blockEntity.getItems(), FisherMk1BlockEntity.SLOT_ROD, 45, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && (stack.getItem() instanceof FishingRodItem
                        || BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("starcatcher"));
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        // [1] Батарея / редстоун
        addSlot(batterySlot(FisherMk1BlockEntity.SLOT_BATTERY, 45, 49));
        // [2] Выход улова
        addSlot(output(FisherMk1BlockEntity.SLOT_OUTPUT, 115, 35));
    }
}
