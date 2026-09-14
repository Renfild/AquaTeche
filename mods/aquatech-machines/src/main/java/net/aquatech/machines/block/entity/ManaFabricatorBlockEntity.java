package net.aquatech.machines.block.entity;

import net.aquatech.machines.compat.BotaniaManaBridge;
import net.aquatech.machines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Мана-Фабрикатор: превращает FE в ману Botania и льёт её в соседний мана-пул.
 * Реверс Цветолова — замыкает цепочку: генераторы FE → фабрикатор → пул → цветолов.
 * Слоты: [0] Батарея, [1] Скорость, [2] Энергоэффективность.
 */
public class ManaFabricatorBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_SPEED = 1;
    public static final int SLOT_EFF = 2;

    /** Мана за один завершённый цикл — льётся в соседний мана-пул. */
    public static final int MANA_PER_CYCLE = 200;

    public ManaFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_FABRICATOR.get(), pos, state, 3, 500000, 4096, 40, 60, 500000);
        defineSlots(-1, -1, SLOT_SPEED, SLOT_EFF, SLOT_BATTERY);
    }

    @Override
    protected net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.ManaFabricatorMenu(id, inv, this);
    }

    @Override
    protected int outputSlots() {
        return 0;
    }

    /** Выходов-слотов нет: продукт машины — мана в соседнем пуле. */
    @Override
    protected boolean pushOutputToChests() {
        return false;
    }

    @Override
    protected boolean hasWork() {
        return BotaniaManaBridge.hasCapacity(level, worldPosition);
    }

    @Override
    protected void craftOnce() {
        if (level == null || level.isClientSide) return;
        BotaniaManaBridge.depositToNeighbors(level, worldPosition, MANA_PER_CYCLE);
    }

    /** Мана в соседних пулах (для тултипа GUI). */
    public int neighborMana() {
        return BotaniaManaBridge.neighborMana(level, worldPosition);
    }
}
