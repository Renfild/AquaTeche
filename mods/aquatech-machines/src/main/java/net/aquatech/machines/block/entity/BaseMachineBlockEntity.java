package net.aquatech.machines.block.entity;

import net.aquatech.machines.util.MachineEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Общая база механизмов AquaTech: FE-энергия, слоты, прогресс, авто-выход в сундук.
 * Сетка слотов (наследники объявляют константы):
 *   [0] вход/инструмент, [1..outputEnd) выходы, последний слот — апгрейд-зарезерв (пока не активен).
 */
public abstract class BaseMachineBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {

    protected final MachineEnergyStorage energy;
    private final LazyOptional<IEnergyStorage> energyOptional;
    protected final ItemStackHandler items;
    private final LazyOptional<IItemHandler> itemsOptional;

    protected int progress;
    protected final int maxProgress;
    protected final int energyPerTick;

    protected BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                     int slots, int energyCapacity, int maxReceive,
                                     int maxProgress, int energyPerTick) {
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
    }

    /** Сколько выходных слотов (сразу после входа). */
    protected abstract int outputSlots();

    /** Выполнить один цикл: положить результат в выходные слоты. */
    protected abstract void craftOnce();

    /** Есть ли смысл работать (вход есть / выход есть). */
    protected abstract boolean hasWork();

    public boolean isWorking() {
        return progress > 0;
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable(
                "block.aquatech_machines." + getBlockState().getBlock().getDescriptionId()
                        .replace("block.aquatech_machines.", ""));
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player player) {
        return createMenu(id, inv);
    }

    protected abstract net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv);

    public int getScaledProgress(int width) {
        return maxProgress == 0 ? 0 : progress * width / maxProgress;
    }

    public int getEnergy() {
        return energy.getEnergy();
    }

    public int getProgressValue() {
        return progress;
    }

    public int getMaxProgressValue() {
        return maxProgress;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getMaxEnergy() {
        return 100000;
    }

    public static void serverTick(BaseMachineBlockEntity be) {
        boolean changed = false;

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

        if (be.pushOutputToChests()) {
            changed = true;
        }

        if (changed) {
            be.setChanged();
        }
    }

    /** Авто-выход: выталкивает содержимое выходных слотов в соседние инвентари (сундуки). */
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
