package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.util.FisherLoot;
import net.aquatech.machines.util.RodLootBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Рыболов MK-1: удочка [0], батарея [1], выход [2].
 * Раз в 5 секунд (100 тиков по 40 FE) достаёт один ресурс из улова самой удочки: тот же пул и тот же тир, что при
 * обычной ловле. Без ядра рыбы, множителя улова и апгрейдов, в этом он проще MK-2. Энергию берёт и от батареи, и от
 * Рыбного генератора рядом.
 */
public class FisherMk1BlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_ROD = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_OUTPUT = 2;

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

    @Override
    public boolean acceptsFishGeneratorPower() {
        return true;
    }

    public int rodTier() {
        ItemStack rod = items.getStackInSlot(SLOT_ROD);
        if (rod.isEmpty()) return 0;
        return FisherLoot.tierOf(rod);
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
        // Те же ресурсы, что даёт сама удочка: берём один стек из её улова. Запасная таблица нужна, если aquatech_ui нет.
        java.util.List<ItemStack> rolled = RodLootBridge.rollLikeRod(items.getStackInSlot(SLOT_ROD), level.getRandom());
        ItemStack loot = rolled.isEmpty() ? FisherLoot.rollResources(tier, level.getRandom())
                : rolled.get(level.getRandom().nextInt(rolled.size()));
        if (loot == null || loot.isEmpty()) {
            loot = new ItemStack(Items.RAW_IRON, 1);
        }
        emitToOutput(SLOT_OUTPUT, loot, Math.max(1, loot.getCount()));
    }
}
