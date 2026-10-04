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
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Рыбный генератор: как печка жжёт уголь, дерево и любое горючее (или рыбу) и превращает горение в FE. Выработка
 * зависит от топлива (см. {@link FishGeneratorLogic#rateForBurnTicks}). Энергию получают только авторыболовы
 * (Авто-Рыболов MK-2 и Рыболов MK-1), стоящие вплотную: наружу генератор энергию не выставляет, поэтому ни трубы, ни
 * другие машины от него не питаются.
 */
public class FishGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_FUEL = 0;

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

    private int energy;
    private int burnTime;
    /** Сколько тиков горит текущая единица топлива: нужно экрану для шкалы, у угля и рыбы оно разное. */
    private int burnTotal;
    /** Выработка в FE/t у топлива, которое горит сейчас. */
    private int burnRate;
    /** Сколько FE за последний тик ушло авторыболовам: экран показывает настоящую отдачу. */
    private int lastTransferred;

    public FishGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_GENERATOR.get(), pos, state);
    }

    public static boolean isFuel(ItemStack stack) {
        return burnTicksOf(stack) > 0;
    }

    /** Сколько тиков горит предмет: рыба фиксированно, остальное как в печке (уголь 1600, доски 300 и так далее). */
    public static int burnTicksOf(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (isFish(stack)) {
            return FishGeneratorLogic.FISH_BURN_TICKS;
        }
        return Math.max(0, ForgeHooks.getBurnTime(stack, RecipeType.SMELTING));
    }

    /** Выработка предмета в FE/t: рыба считается как уголь, остальное по длительности горения. */
    public static int rateOf(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (isFish(stack)) {
            return FishGeneratorLogic.FISH_RATE;
        }
        return FishGeneratorLogic.rateForBurnTicks(burnTicksOf(stack));
    }

    private static boolean isFish(ItemStack stack) {
        return stack.is(ItemTags.FISHES) || FishRosterService.isCatalogFish(BuiltInRegistries.ITEM.getKey(stack.getItem()));
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

    /** Выработка прямо сейчас: ноль, если ничего не горит. */
    public int getCurrentRate() {
        return burnTime > 0 ? burnRate : 0;
    }

    public int getLastTransferred() {
        return lastTransferred;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return FishGeneratorLogic.CAPACITY;
    }

    public static void serverTick(FishGeneratorBlockEntity be) {
        boolean changed = false;
        if (be.burnTime <= 0) {
            int nextRate = rateOf(be.fuel.getStackInSlot(SLOT_FUEL));
            if (nextRate > 0 && FishGeneratorLogic.canBurnThisTick(be.energy, FishGeneratorLogic.CAPACITY, nextRate)) {
                changed = be.igniteNextFuel();
            }
        }
        boolean producing = be.burnTime > 0
                && FishGeneratorLogic.canBurnThisTick(be.energy, FishGeneratorLogic.CAPACITY, be.burnRate);
        if (producing) {
            be.burnTime--;
            be.energy = Math.min(FishGeneratorLogic.CAPACITY, be.energy + be.burnRate);
            changed = true;
        }
        be.updateLit(producing);
        int moved = be.pushEnergyToFishers();
        if (moved != be.lastTransferred) {
            be.lastTransferred = moved;
            changed = true;
        }
        if (moved > 0) {
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
        int rate = rateOf(inSlot);
        ItemStack leftover = inSlot.getCraftingRemainingItem();
        fuel.extractItem(SLOT_FUEL, 1, false);
        if (!leftover.isEmpty() && fuel.getStackInSlot(SLOT_FUEL).isEmpty()) {
            fuel.setStackInSlot(SLOT_FUEL, leftover);
        }
        burnTime = ticks;
        burnTotal = ticks;
        burnRate = rate;
        return true;
    }

    /** Отдаёт энергию только авторыболовам вплотную. Возвращает, сколько FE ушло за этот тик. */
    private int pushEnergyToFishers() {
        if (level == null || energy <= 0) return 0;
        int total = 0;
        for (Direction side : Direction.values()) {
            if (energy <= 0) break;
            if (!(level.getBlockEntity(worldPosition.relative(side)) instanceof BaseMachineBlockEntity machine)) continue;
            if (!machine.acceptsFishGeneratorPower()) continue;
            int accepted = machine.receiveGeneratorEnergy(Math.min(FishGeneratorLogic.MAX_EXTRACT, energy));
            energy -= accepted;
            total += accepted;
        }
        return total;
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
        tag.putInt("Energy", energy);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTotal", burnTotal);
        tag.putInt("BurnRate", burnRate);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel.deserializeNBT(tag.getCompound("Fuel"));
        energy = Math.max(0, Math.min(FishGeneratorLogic.CAPACITY, tag.getInt("Energy")));
        burnTime = tag.getInt("BurnTime");
        burnTotal = tag.contains("BurnTotal") ? tag.getInt("BurnTotal") : Math.max(burnTime, 0);
        burnRate = tag.contains("BurnRate") ? tag.getInt("BurnRate") : FishGeneratorLogic.RATE_COAL;
    }

    /** Наружу выставлен только слот топлива (воронки): энергетической способности у генератора нет. */
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return fuelOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fuelOptional.invalidate();
    }
}
