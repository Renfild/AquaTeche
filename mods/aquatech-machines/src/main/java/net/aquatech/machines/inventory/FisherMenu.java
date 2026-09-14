package net.aquatech.machines.inventory;

import net.aquatech.machines.block.entity.FisherBlockEntity;
import net.aquatech.machines.registry.ModItems;
import net.aquatech.machines.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
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
        // [0] Удочка StarCatcher
        addSlot(new SlotItemHandler(blockEntity.getItems(), FisherBlockEntity.SLOT_ROD, 45, 25) {
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
        // [1] Ядро Рыболова
        addSlot(new SlotItemHandler(blockEntity.getItems(), FisherBlockEntity.SLOT_CORE, 45, 49) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.is(ModItems.FISHING_CORE.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        // [2] Апгрейд скорости (выносное крыло)
        addSlot(speedUpgradeSlot(FisherBlockEntity.SLOT_SPEED, 185, 19));
        // [3] Слот батареи / аккумулятора (выносное крыло)
        addSlot(batterySlot(FisherBlockEntity.SLOT_BATTERY, 185, 41));
        // [4] Выход улова
        addSlot(output(FisherBlockEntity.SLOT_OUTPUT, 115, 35));
        // [5] Апгрейд энергоэффективности (выносное крыло)
        addSlot(efficiencyUpgradeSlot(FisherBlockEntity.SLOT_EFF, 185, 63));
    }

    public BlockPos getPos() {
        return pos;
    }
}
