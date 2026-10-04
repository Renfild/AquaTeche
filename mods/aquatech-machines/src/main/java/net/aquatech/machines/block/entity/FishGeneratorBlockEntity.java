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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Рыбный генератор: как печка жжёт уголь, дерево и любое горючее (или рыбу) и превращает горение в FE. Не принимает
 * энергию снаружи, отдаёт её соседям (машины сами её тянут, а соседей-приёмников он ещё и подпитывает).
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
    /** Сколько тиков горит текущая единица топлива: нужно экрану для шкалы, у угля и рыбы оно разное. */
    private int burnTotal;

    public FishGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_GENERATOR.get(), pos, state);
    }

    public static boolean isFuel(ItemStack stack) {
        return burnTicksOf(stack) > 0;
    }

    /** Сколько тиков горит предмет: рыба фиксированно, остальное как в печке (уголь 1600, доски 300 и так далее). */
    public static int burnTicksOf(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(ItemTags.FISHES) || FishRosterService.isCatalogFish(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return FishGeneratorLogic.FISH_BURN_TICKS;
        }
        return Math.max(0, ForgeHooks.getBurnTime(stack, RecipeType.SMELTING));
    }

    public ItemStackHandler getFuel() {
        return fuel;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getBurnTotal() {
        return burnTotal;
    }

    public int getEnergy() {
        return storage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return storage.getMaxEnergyStored();
    }

    public static void serverTick(FishGeneratorBlockEntity be) {
        boolean changed = false;
        boolean roomForTick = FishGeneratorLogic.canBurnThisTick(be.storage.getEnergyStored(), be.storage.getMaxEnergyStored());
        if (be.burnTime <= 0 && roomForTick) {
            changed = be.igniteNextFuel();
        }
        boolean producing = be.burnTime > 0 && roomForTick;
        if (producing) {
            be.burnTime--;
            be.storage.generate(FishGeneratorLogic.FE_PER_TICK);
            changed = true;
        }
        be.updateLit(producing);
        if (be.pushEnergyToNeighbors()) {
            changed = true;
        }
        if (changed) {
            be.setChanged();
        }
    }

    /** Берёт одну единицу топлива из слота. Ведро и подобная тара возвращается в слот. */
    private boolean igniteNextFuel() {
        ItemStack inSlot = fuel.getStackInSlot(SLOT_FUEL);
        int ticks = burnTicksOf(inSlot);
        if (ticks <= 0) return false;
        ItemStack leftover = inSlot.getCraftingRemainingItem();
        fuel.extractItem(SLOT_FUEL, 1, false);
        if (!leftover.isEmpty() && fuel.getStackInSlot(SLOT_FUEL).isEmpty()) {
            fuel.setStackInSlot(SLOT_FUEL, leftover);
        }
        burnTime = ticks;
        burnTotal = ticks;
        return true;
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
        tag.putInt("BurnTotal", burnTotal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel.deserializeNBT(tag.getCompound("Fuel"));
        storage.set(tag.getInt("Energy"));
        burnTime = tag.getInt("BurnTime");
        burnTotal = tag.contains("BurnTotal") ? tag.getInt("BurnTotal") : Math.max(burnTime, 0);
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
