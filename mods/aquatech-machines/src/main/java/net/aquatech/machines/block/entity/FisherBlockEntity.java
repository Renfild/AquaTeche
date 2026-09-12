package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.util.FisherLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Рыболов MK-2: вставь удочку StarCatcher — ловит рыбу по ростеру тира удочки.
 * FE-энергия за цикл, улов в выходной слот и авто-выходом в сундук.
 */
public class FisherBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_ROD = 0;
    public static final int SLOT_OUTPUT = 1;

    public FisherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISHER.get(), pos, state, 2, 100000, 256, 120, 40);
    }
    @Override
    protected net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.FisherMenu(id, inv, this);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, net.minecraft.world.entity.player.Player player) {
        return new net.aquatech.machines.inventory.FisherMenu(id, inv, this);
    }

    @Override
    protected int outputSlots() {
        return 1;
    }

    private int rodTier() {
        ItemStack rod = items.getStackInSlot(SLOT_ROD);
        if (rod.isEmpty()) return 0;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(rod.getItem());
        return FisherLoot.tierOf(id);
    }

    @Override
    protected boolean hasWork() {
        return rodTier() > 0;
    }

    @Override
    protected void craftOnce() {
        int tier = rodTier();
        if (tier <= 0 || level == null) return;
        ItemStack fish = FisherLoot.roll(tier, level.getRandom());
        if (fish == null || fish.isEmpty()) return;
        ItemStack rest = items.insertItem(SLOT_OUTPUT, fish, false);
        if (!rest.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity drop =
                    new net.minecraft.world.entity.item.ItemEntity(level, worldPosition.getX() + 0.5,
                            worldPosition.getY() - 0.5, worldPosition.getZ() + 0.5, rest);
            level.addFreshEntity(drop);
        }
    }
}
