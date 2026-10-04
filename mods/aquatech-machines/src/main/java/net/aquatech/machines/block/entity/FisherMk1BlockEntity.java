package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.util.FisherLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Рыболов MK-1: удочка [0], батарея [1], выход [2].
 * Раз в 5 секунд (100 тиков по 40 FE) добывает один ресурс по тиру удочки, но не выше тира 4.
 * Без Ядра рыбы, множителя улова и апгрейдов: всё это у MK-2.
 */
public class FisherMk1BlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_ROD = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_OUTPUT = 2;

    /** Удочки выше четвёртого тира дают MK-1 тот же пул, что и тир 4. */
    public static final int MAX_TIER = 4;
    public static final int CYCLE_TICKS = 100;
    public static final int ENERGY_PER_TICK = 40;
    public static final int ENERGY_CAPACITY = 20000;
    public static final int MAX_RECEIVE = 200;

    public FisherMk1BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISHER_MK1.get(), pos, state, 3, ENERGY_CAPACITY, MAX_RECEIVE, CYCLE_TICKS, ENERGY_PER_TICK);
        defineSlots(SLOT_OUTPUT, SLOT_OUTPUT, -1, -1, SLOT_BATTERY);
    }

    @Override
    protected net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.FisherMk1Menu(id, inv, this);
    }

    @Override
    protected int outputSlots() {
        return 1;
    }

    public int rodTier() {
        ItemStack rod = items.getStackInSlot(SLOT_ROD);
        if (rod.isEmpty()) return 0;
        return Math.min(MAX_TIER, FisherLoot.tierOf(rod));
    }

    @Override
    protected boolean hasWork() {
        if (rodTier() <= 0) return false;
        ItemStack out = items.getStackInSlot(SLOT_OUTPUT);
        return out.isEmpty() || out.getCount() < out.getMaxStackSize();
    }

    @Override
    protected void craftOnce() {
        int tier = rodTier();
        if (tier <= 0 || level == null) return;
        ItemStack loot = FisherLoot.rollResources(tier, level.getRandom());
        if (loot == null || loot.isEmpty()) {
            loot = new ItemStack(Items.RAW_IRON, 1);
        }
        emitToOutput(SLOT_OUTPUT, loot, Math.max(1, loot.getCount()));
    }
}
