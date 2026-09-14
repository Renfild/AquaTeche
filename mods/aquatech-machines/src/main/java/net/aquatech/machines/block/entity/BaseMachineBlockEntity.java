package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModItems;
import net.aquatech.machines.util.MachineEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
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
    protected final int defaultSlots;
    protected int speedSlot = -1;
    protected int effSlot = -1;
    protected int batterySlot = -1;
    protected int firstOutputSlot = 0;
    protected int lastOutputSlot = 0;
    private boolean iuRegistered = false;

    protected BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                     int slots, int energyCapacity, int maxReceive,
                                     int maxProgress, int energyPerTick) {
        this(type, pos, state, slots, energyCapacity, maxReceive, maxProgress, energyPerTick, energyCapacity);
    }

    protected BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                     int slots, int energyCapacity, int maxReceive,
                                     int maxProgress, int energyPerTick, int maxEnergyStored) {
        super(type, pos, state);
        this.defaultSlots = slots;
        this.energy = new MachineEnergyStorage(energyCapacity, energyCapacity, this::setChanged);
        this.energyOptional = LazyOptional.of(() -> energy);
        this.items = new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public void setSize(int size) {
                super.setSize(Math.max(defaultSlots, size));
            }
        };
        this.itemsOptional = LazyOptional.of(() -> items);
        this.maxProgress = maxProgress;
        this.energyPerTick = energyPerTick;
        this.maxEnergy = maxEnergyStored;
    }

    /** Слоты апгрейдов/батареи/выходов: машина объявляет после super(). */
    protected void defineSlots(int firstOut, int lastOut, int speed, int eff, int battery) {
        this.firstOutputSlot = firstOut;
        this.lastOutputSlot = lastOut;
        this.speedSlot = speed;
        this.effSlot = eff;
        this.batterySlot = battery;
    }

    /** Скорость прогресса: х4 со speed_upgrade_4, х2 со speed_upgrade_1 / speed_upgrade, иначе х1. */
    protected int progressPerTick() {
        if (isSlot(ModItems.SPEED_UPGRADE_4.get(), speedSlot)) return 4;
        if (isSlot(ModItems.SPEED_UPGRADE_1.get(), speedSlot) || isSlot(ModItems.SPEED_UPGRADE.get(), speedSlot)) return 2;
        return 1;
    }

    /** Энергоэффективность: −75% расхода. */
    protected int effectiveEnergyPerTick() {
        if (isSlot(ModItems.ENERGY_EFFICIENCY.get(), effSlot)) {
            return Math.max(5, energyPerTick / 4);
        }
        return energyPerTick;
    }

    protected boolean hasUpgrade(Item item) {
        return (speedSlot >= 0 && isSlot(item, speedSlot))
                || (effSlot >= 0 && isSlot(item, effSlot));
    }

    protected boolean isSlot(Item item, int slot) {
        if (slot < 0 || slot >= items.getSlots()) return false;
        ItemStack s = items.getStackInSlot(slot);
        return !s.isEmpty() && s.is(item);
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

    public int getProgressValue() {
        return progress;
    }

    public int getMaxProgressValue() {
        return maxProgress;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable(
                getBlockState().getBlock().getDescriptionId());
    }

    protected abstract net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id,
            net.minecraft.world.entity.player.Inventory inv);

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id,
            net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player player) {
        return createMenu(id, inv);
    }

    protected void onServerTick() {
        // Переопределяется подклассами для специфической тиковой логики (жидкости, баки, давление)
    }

    public static void serverTick(BaseMachineBlockEntity be) {
        if (!be.iuRegistered && be.level != null && !be.level.isClientSide) {
            net.aquatech.machines.compat.energy.IndustrialUpgradeEnergyCompat.registerMachine(be);
            be.iuRegistered = true;
        }

        // Кастомная логика подклассов (дренаж ведер, обновление флюидов и давления)
        be.onServerTick();

        boolean changed = false;

        // Пул энергии: из соседей и из батареи в слоте
        if (be.energy.getEnergy() < be.maxEnergy) {
            be.pullEnergyFromNeighbors();
            be.pullEnergyFromBattery();
        }

        int eff = be.effectiveEnergyPerTick();
        if (be.hasWork() && be.energy.getEnergy() >= eff) {
            be.energy.consumeInternal(eff);
            be.progress += be.progressPerTick();
            changed = true;
            if (be.progress >= be.maxProgress) {
                be.progress = 0;
                be.craftOnce();
            }
        } else if (be.progress > 0) {
            be.progress = Math.max(0, be.progress - 2);
            changed = true;
        }

        boolean working = (be.hasWork() && be.energy.getEnergy() >= eff) || be.progress > 0;
        be.updateLit(working);

        if (be.pushOutputToChests()) {
            changed = true;
        }

        if (changed) {
            be.setChanged();
        }
    }

    /** Тянет FE из соседних блоков — до 10000 FE/тик со стороны. */
    private void pullEnergyFromNeighbors() {
        if (level == null) return;
        for (Direction side : Direction.values()) {
            if (energy.getEnergy() >= maxEnergy) return;
            var neighbor = level.getBlockEntity(worldPosition.relative(side));
            if (neighbor == null) continue;

            // 1. Forge Energy
            var cap = neighbor.getCapability(ForgeCapabilities.ENERGY, side.getOpposite())
                    .orElseGet(() -> neighbor.getCapability(ForgeCapabilities.ENERGY, null).orElse(null));
            if (cap != null && cap.canExtract()) {
                int want = Math.min(10000, maxEnergy - energy.getEnergy());
                if (want > 0) {
                    int got = cap.extractEnergy(want, false);
                    if (got > 0) {
                        energy.receiveInternal(got);
                        setChanged();
                        continue;
                    }
                }
            }

            // 2. Industrial Upgrade EnergySource (генераторы, панели, аккумуляторы IU)
            int want = Math.min(10000, maxEnergy - energy.getEnergy());
            if (want > 0) {
                int fromIu = net.aquatech.machines.compat.energy.IndustrialUpgradeEnergyCompat
                        .extractFromNeighbor(neighbor, side.getOpposite(), want);
                if (fromIu > 0) {
                    energy.receiveInternal(fromIu);
                    setChanged();
                }
            }
        }
    }

    /** LIT blockstate: анимация off/on (front_on текстура + свечение). */
    protected void updateLit(boolean lit) {
        if (level == null || level.isClientSide) return;
        BlockState st = getBlockState();
        if (st.hasProperty(BlockStateProperties.LIT)
                && st.getValue(BlockStateProperties.LIT) != lit) {
            level.setBlock(worldPosition, st.setValue(BlockStateProperties.LIT, lit), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            net.aquatech.machines.compat.energy.IndustrialUpgradeEnergyCompat.registerMachine(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            net.aquatech.machines.compat.energy.IndustrialUpgradeEnergyCompat.unregisterMachine(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            net.aquatech.machines.compat.energy.IndustrialUpgradeEnergyCompat.unregisterMachine(this);
        }
    }

    /** Батарея в слоте: тянем FE из предмета (редстоун, Forge Energy батареи, IU-аккумуляторы). */
    private void pullEnergyFromBattery() {
        if (batterySlot < 0 || batterySlot >= items.getSlots()) return;
        ItemStack battery = items.getStackInSlot(batterySlot);
        if (battery.isEmpty()) return;

        // 0. Редстоун (+1000 FE) и редстоун-блок (+9000 FE)
        if (battery.is(net.minecraft.world.item.Items.REDSTONE)) {
            int room = maxEnergy - energy.getEnergy();
            if (room >= 1000) {
                battery.shrink(1);
                energy.receiveInternal(1000);
                setChanged();
                return;
            }
        } else if (battery.is(net.minecraft.world.item.Items.REDSTONE_BLOCK)) {
            int room = maxEnergy - energy.getEnergy();
            if (room >= 9000) {
                battery.shrink(1);
                energy.receiveInternal(9000);
                setChanged();
                return;
            }
        }

        int want = Math.min(10000, maxEnergy - energy.getEnergy());
        if (want <= 0) return;

        // 1. Попытка через Forge Energy
        var feCap = battery.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (feCap != null && feCap.canExtract()) {
            int got = feCap.extractEnergy(want, false);
            if (got > 0) {
                energy.receiveInternal(got);
                setChanged();
                return;
            }
        }

        // 2. Попытка через Industrial Upgrade ElectricItem
        int fromIu = net.aquatech.machines.compat.energy.IndustrialUpgradeEnergyCompat.dischargeBattery(battery, want);
        if (fromIu > 0) {
            energy.receiveInternal(fromIu);
            setChanged();
        }
    }

    /** Авто-выход: содержимое выходных слотов -> соседние инвентари (сундуки). */
    protected boolean pushOutputToChests() {
        boolean moved = false;
        for (int slot = firstOutputSlot; slot <= lastOutputSlot; slot++) {
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

    public static ItemStack pushAll(IItemHandler target, ItemStack stack) {
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
        if (items.getSlots() < defaultSlots) {
            items.setSize(defaultSlots);
        }
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
    }
}
