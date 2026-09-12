# -*- coding: utf-8 -*-
# Rewrites BaseMachineBlockEntity with energy pull + LIT animation support.
code = '''package net.aquatech.machines.block.entity;

import net.aquatech.machines.util.MachineEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Общая база механизмов: FE-энергия (принимает И тянет от соседей), слоты,
 * прогресс, авто-выход в сундук, LIT-состояние для анимации off/on.
 */
public abstract class BaseMachineBlockEntity extends BlockEntity implements MenuProvider {

    protected final MachineEnergyStorage energy;
    private final LazyOptional<IEnergyStorage> energyOptional;
    protected final ItemStackHandler items;
    private final LazyOptional<IItemHandler> itemsOptional;

    protected int progress;
    protected final int maxProgress;
    protected final int energyPerTick;
    protected final int maxEnergy;

    protected BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                     int slots, int energyCapacity, int maxReceive,
                                     int maxProgress, int energyPerTick) {
        this(type, pos, state, slots, energyCapacity, maxReceive, maxProgress, energyPerTick, energyCapacity);
    }

    protected BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                     int slots, int energyCapacity, int maxReceive,
                                     int maxProgress, int energyPerTick, int maxEnergyStored) {
        super(type, pos, state);
        this.energy = new MachineEnergyStorage(energyCapacity, maxReceive);
        this.energyOptional = LazyOptional.of(() -> energy);
        this.items = new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.itemsOptional = LazyOptional.of(() -> items);
        this.maxProgress = maxProgress;
        this.energyPerTick = energyPerTick;
        this.maxEnergy = maxEnergyStored;
    }

    protected abstract int outputSlots();

    protected abstract void craftOnce();

    protected abstract boolean hasWork();

    public boolean isWorking() {
        return progress > 0;
    }

    public int getScaledProgress(int width) {
        return maxProgress == 0 ? 0 : progress * width / maxProgress;
    }

    public int getEnergy() {
        return energy.getEnergy();
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public static void serverTick(BaseMachineBlockEntity be) {
        boolean changed = false;

        // Пул энергии из соседей (IU-панели и батареи не пушат сами)
        if (be.energy.getEnergy() < be.maxEnergy) {
            be.pullEnergyFromNeighbors();
        }

        if (be.hasWork() && be.energy.getEnergy() >= be.energyPerTick) {
            be.energy.consumeInternal(be.energyPerTick);
            be.progress++;
            changed = true;
            if (be.progress >= be.maxProgress) {
                be.progress = 0;
                be.craftOnce();
            }
        } else if (be.progress > 0) {
            be.progress = Math.max(0, be.progress - 2);
            changed = true;
        }

        be.updateLit(be.progress > 0);

        if (be.pushOutputToChests()) {
            changed = true;
        }

        if (changed) {
            be.setChanged();
        }
    }

    /** Тянет FE из соседних блоков — по 512 FE/тик со стороны. */
    private void pullEnergyFromNeighbors() {
        for (Direction side : Direction.values()) {
            if (energy.getEnergy() >= maxEnergy) return;
            var neighbor = level.getBlockEntity(worldPosition.relative(side));
            if (neighbor == null) continue;
            var cap = neighbor.getCapability(ForgeCapabilities.ENERGY, side.getOpposite()).orElse(null);
            if (cap == null || !cap.canExtract()) continue;
            int want = Math.min(512, maxEnergy - energy.getEnergy());
            if (want <= 0) return;
            int got = cap.extractEnergy(want, false);
            if (got > 0) {
                energy.receiveInternal(got);
                setChanged();
            }
        }
    }

    /** LIT blockstate: анимация off/on (front_on текстура + свечение). */
    protected void updateLit(boolean lit) {
        BlockState st = getBlockState();
        if (st.hasProperty(BlockStateProperties.LIT)
                && st.getValue(BlockStateProperties.LIT) != lit) {
            level.setBlock(worldPosition, st.setValue(BlockStateProperties.LIT, lit), 3);
        }
    }

    /** Авто-выход: содержимое выходных слотов -> соседние инвентари (сундуки). */
    private boolean pushOutputToChests() {
        boolean moved = false;
        int outEnd = 1 + outputSlots();
        for (int slot = 1; slot < outEnd; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            for (Direction side : Direction.values()) {
                var neighbor = level.getBlockEntity(worldPosition.relative(side));
                if (neighbor == null) continue;
                var cap = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER,
                        side.getOpposite()).orElse(null);
                if (cap == null) continue;
                ItemStack rest = pushAll(cap, stack);
                if (rest.getCount() != stack.getCount()) {
                    items.setStackInSlot(slot, rest);
                    moved = true;
                }
                if (rest.isEmpty()) break;
            }
        }
        return moved;
    }

    private static ItemStack pushAll(IItemHandler target, ItemStack stack) {
        ItemStack rest = stack;
        for (int i = 0; i < target.getSlots() && !rest.isEmpty(); i++) {
            rest = target.insertItem(i, rest, false);
        }
        return rest;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemsOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemsOptional.invalidate();
    }

    public void dropContents() {
        if (level == null) return;
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < items.getSlots(); i++) {
            drops.add(items.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, drops);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());
        tag.putInt("Energy", energy.getEnergy());
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
    }
}
'''
open("src/main/java/net/aquatech/machines/block/entity/BaseMachineBlockEntity.java", "w", encoding="utf-8", newline="\n").write(code)
print("base rewritten")
