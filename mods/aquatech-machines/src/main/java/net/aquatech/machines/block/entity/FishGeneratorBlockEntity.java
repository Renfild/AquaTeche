package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.util.FishGeneratorLogic;
import net.aquatech.machines.util.FishRosterService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Рыбный генератор: сжигает рыбу и превращает её в FE. Не принимает энергию снаружи, отдаёт её соседям
 * (машины сами её тянут, а соседей-приёмников он ещё и подпитывает).
 */
public class FishGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_FUEL = 0;

    private final GeneratorEnergy storage = new GeneratorEnergy();
    private final LazyOptional<EnergyStorage> energyOptional = LazyOptional.of(() -> storage);
    private final ItemStackHandler fuel = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isFuel(stack);
        }
    };
    private final LazyOptional<ItemStackHandler> fuelOptional = LazyOptional.of(() -> fuel);

    private int burnTime;

    public FishGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_GENERATOR.get(), pos, state);
    }

    public static boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ItemTags.FISHES) || FishRosterService.isCatalogFish(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public ItemStackHandler getFuel() {
        return fuel;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getEnergy() {
        return storage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return storage.getMaxEnergyStored();
    }

    public static void serverTick(FishGeneratorBlockEntity be) {
        boolean changed = false;
        if (be.burnTime > 0) {
            be.burnTime--;
            be.storage.generate(FishGeneratorLogic.generatedThisTick(be.storage.getEnergyStored(), be.storage.getMaxEnergyStored()));
            changed = true;
        }
        if (be.burnTime <= 0 && FishGeneratorLogic.canStartBurn(be.storage.getEnergyStored(), be.storage.getMaxEnergyStored())) {
            ItemStack inSlot = be.fuel.getStackInSlot(SLOT_FUEL);
            if (isFuel(inSlot)) {
                be.fuel.extractItem(SLOT_FUEL, 1, false);
                be.burnTime = FishGeneratorLogic.BURN_TICKS;
                changed = true;
            }
        }
        be.updateLit(be.burnTime > 0);
        if (be.pushEnergyToNeighbors()) {
            changed = true;
        }
        if (changed) {
            be.setChanged();
        }
    }

    private boolean pushEnergyToNeighbors() {
        if (level == null || storage.getEnergyStored() <= 0) return false;
        boolean moved = false;
        for (Direction side : Direction.values()) {
            if (storage.getEnergyStored() <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(side));
            if (neighbor == null) continue;
            var target = neighbor.getCapability(ForgeCapabilities.ENERGY, side.getOpposite()).orElse(null);
            if (target == null || !target.canReceive()) continue;
            int offered = storage.extractEnergy(FishGeneratorLogic.MAX_EXTRACT, true);
            int accepted = target.receiveEnergy(offered, false);
            if (accepted > 0) {
                storage.extractEnergy(accepted, false);
                moved = true;
            }
        }
        return moved;
    }

    private void updateLit(boolean lit) {
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, lit), 3);
        }
    }

    public void dropContents() {
        if (level == null) return;
        NonNullList<ItemStack> drops = NonNullList.create();
        drops.add(fuel.getStackInSlot(SLOT_FUEL));
        Containers.dropContents(level, worldPosition, drops);
        fuel.setStackInSlot(SLOT_FUEL, ItemStack.EMPTY);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new net.aquatech.machines.inventory.FishGeneratorMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Fuel", fuel.serializeNBT());
        tag.putInt("Energy", storage.getEnergyStored());
        tag.putInt("BurnTime", burnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel.deserializeNBT(tag.getCompound("Fuel"));
        storage.set(tag.getInt("Energy"));
        burnTime = tag.getInt("BurnTime");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return fuelOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        fuelOptional.invalidate();
    }

    /** Накопитель, который сам копит энергию и отдаёт её наружу, но извне ничего не принимает. */
    private final class GeneratorEnergy extends EnergyStorage {

        GeneratorEnergy() {
            super(FishGeneratorLogic.CAPACITY, 0, FishGeneratorLogic.MAX_EXTRACT);
        }

        void generate(int amount) {
            energy = Math.min(capacity, energy + Math.max(0, amount));
        }

        void set(int value) {
            energy = Math.max(0, Math.min(capacity, value));
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) {
                setChanged();
            }
            return extracted;
        }
    }
}
