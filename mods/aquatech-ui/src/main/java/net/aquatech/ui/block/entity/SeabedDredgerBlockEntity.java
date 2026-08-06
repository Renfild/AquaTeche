package net.aquatech.ui.block.entity;

import net.aquatech.ui.capability.SkillEffects;
import net.aquatech.ui.inventory.SeabedDredgerMenu;
import net.aquatech.ui.item.UpgradeItem;
import net.aquatech.ui.registry.ModBlockEntities;
import net.aquatech.ui.registry.ModItems;
import net.aquatech.ui.util.CustomEnergyStorage;
import net.aquatech.ui.util.InventoryNbt;
import net.aquatech.ui.util.MachineUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.aquatech.ui.util.OutputOnlyWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SeabedDredgerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int CAPACITY = 50000;
    public static final int MAX_RECEIVE = 1000;
    public static final int ENERGY_PER_TICK = 50;
    public static final int MAX_PROGRESS = 100;
    public static final int UPGRADE_SLOT = 10;

    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(CAPACITY, MAX_RECEIVE, 0);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energyStorage);

    private final ItemStackHandler itemHandler = new ItemStackHandler(11) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) return stack.is(ModItems.DREDGER_DRILL_BIT.get());
            if (slot == UPGRADE_SLOT) return stack.getItem() instanceof UpgradeItem;
            return true;
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> inputOptional = LazyOptional.of(() -> new RangedWrapper(itemHandler, 0, 1));
    private final LazyOptional<IItemHandler> outputOptional = LazyOptional.of(() -> new OutputOnlyWrapper(itemHandler, 1, 10));

    private int progress = 0;
    private int skillCacheTick = -1;
    private float cachedEnergyFactor = 1f;
    private float cachedSpeedMult = 1f;

    public boolean isWorking() {
        return progress > 0;
    }

    private void refreshSkillCache(Level level, BlockPos pos) {
        int t = (int) (level.getGameTime() & 0x7FFFFFFF);
        if (skillCacheTick >= 0 && (t - skillCacheTick) < 20) return;
        skillCacheTick = t;
        Player nearby = SkillEffects.nearestPlayer(level, pos, 16.0);
        cachedEnergyFactor = SkillEffects.energyCostFactor(nearby);
        cachedSpeedMult = SkillEffects.machineSpeedMultiplier(nearby);
    }
    protected final ContainerData dataAccess;

    public SeabedDredgerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEABED_DREDGER.get(), pos, state);
        this.energyStorage.withListener(this::setChanged);
        this.dataAccess = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> energyStorage.getEnergyStored() & 0xFFFF;
                    case 1 -> (energyStorage.getEnergyStored() >> 16) & 0xFFFF;
                    case 2 -> energyStorage.getMaxEnergyStored() & 0xFFFF;
                    case 3 -> (energyStorage.getMaxEnergyStored() >> 16) & 0xFFFF;
                    case 4 -> progress;
                    case 5 -> MAX_PROGRESS;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 4 -> progress = value;
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SeabedDredgerBlockEntity entity) {
        if (level.isClientSide) return;

        entity.refreshSkillCache(level, pos);
        ItemStack bitStack = entity.itemHandler.getStackInSlot(0);
        boolean hasBit = !bitStack.isEmpty();
        int baseCost = MachineUpgrades.energyCost(entity.itemHandler, UPGRADE_SLOT, ENERGY_PER_TICK);
        int cost = Math.max(1, Math.round(baseCost * entity.cachedEnergyFactor));
        boolean hasEnergy = entity.energyStorage.getEnergyStored() >= cost;

        if (hasBit && hasEnergy && entity.hasSpaceInOutput()) {
            int energyBefore = entity.energyStorage.getEnergyStored();
            int progressBefore = entity.progress;
            entity.energyStorage.consumeEnergy(cost);
            float progress = MachineUpgrades.progressPerTick(entity.itemHandler, UPGRADE_SLOT) * entity.cachedSpeedMult;
            entity.progress += Math.max(1, Math.round(progress));

            if (entity.progress >= MAX_PROGRESS) {
                entity.progress = 0;
                entity.doDredgeOperation(level, bitStack);
            }
            if (energyBefore != entity.energyStorage.getEnergyStored() || progressBefore != entity.progress) {
                entity.setChanged();
            }
        } else if (entity.progress > 0) {
            entity.progress = Math.max(0, entity.progress - 2);
            entity.setChanged();
        }
        WorkingMachineTracker.setWorking(level, pos, entity.progress > 0);
    }

    private boolean hasSpaceInOutput() {
        for (int i = 1; i < 10; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) return true;
        }
        return false;
    }

    private void doDredgeOperation(Level level, ItemStack bitStack) {
        List<ItemStack> dredgedLoot = new ArrayList<>();
        float rng = level.getRandom().nextFloat();

        // Early/mid loot only — no free diamond ore / netherite / echo
        if (rng < 0.55f) dredgedLoot.add(new ItemStack(Items.SAND, 2 + level.getRandom().nextInt(4)));
        if (rng < 0.45f) dredgedLoot.add(new ItemStack(Items.GRAVEL, 2 + level.getRandom().nextInt(3)));
        if (rng < 0.35f) dredgedLoot.add(new ItemStack(Items.CLAY_BALL, 2 + level.getRandom().nextInt(3)));
        if (rng < 0.25f) dredgedLoot.add(new ItemStack(Items.PRISMARINE_SHARD, 1 + level.getRandom().nextInt(2)));
        if (rng < 0.12f) dredgedLoot.add(new ItemStack(Items.RAW_IRON, 1));
        if (rng < 0.06f) dredgedLoot.add(new ItemStack(Items.RAW_GOLD, 1));
        if (dredgedLoot.isEmpty()) dredgedLoot.add(new ItemStack(Items.SAND, 3));

        boolean doubleOut = MachineUpgrades.doubleOutput(itemHandler, UPGRADE_SLOT);
        for (ItemStack drop : dredgedLoot) {
            insertIntoOutput(drop);
            if (doubleOut && level.getRandom().nextFloat() < 0.5f) insertIntoOutput(drop.copy());
        }

        if (bitStack.isDamageableItem()) {
            bitStack.setDamageValue(bitStack.getDamageValue() + 1);
            if (bitStack.getDamageValue() >= bitStack.getMaxDamage()) {
                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
    }

    private void insertIntoOutput(ItemStack stackToInsert) {
        ItemStack copy = stackToInsert.copy();
        for (int i = 1; i < 10; i++) {
            copy = itemHandler.insertItem(i, copy, false);
            if (copy.isEmpty()) break;
        }
    }

    public void drops() {
        if (level == null) return;
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < itemHandler.getSlots(); i++) drops.add(itemHandler.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, drops);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.aquatech_ui.seabed_dredger");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SeabedDredgerMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyOptional.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandlerOptional.cast();
            if (side == Direction.UP) return inputOptional.cast();
            return outputOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemHandlerOptional.invalidate();
        inputOptional.invalidate();
        outputOptional.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        InventoryNbt.loadFixedSize(itemHandler, tag.getCompound("Inventory"), 11);
        energyStorage.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
    }
}
