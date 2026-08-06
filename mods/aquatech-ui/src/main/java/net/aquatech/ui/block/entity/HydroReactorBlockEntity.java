package net.aquatech.ui.block.entity;

import net.aquatech.ui.capability.SkillEffects;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HydroReactorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int CAPACITY = 250000;
    /** Balanced output ~1.2k FE/t (was 25k). Efficiency upgrade boosts further. */
    public static final int GENERATION_RATE = 1200;
    public static final int UPGRADE_SLOT = 1;

    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(CAPACITY, 0, GENERATION_RATE * 4);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energyStorage);

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) return stack.is(ModItems.KELP_BIO_PELLET.get());
            if (slot == UPGRADE_SLOT) {
                return stack.getItem() instanceof UpgradeItem up
                        && up.getType() == UpgradeItem.UpgradeType.EFFICIENCY;
            }
            return false;
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> fuelOptional = LazyOptional.of(() -> new RangedWrapper(itemHandler, 0, 1));

    private int burnTime = 0;
    private int skillCacheTick = -1;
    private float cachedHydroBonus = 1f;

    public boolean isWorking() {
        return burnTime > 0;
    }

    private void refreshSkillCache(Level level, BlockPos pos) {
        int t = (int) (level.getGameTime() & 0x7FFFFFFF);
        if (skillCacheTick >= 0 && (t - skillCacheTick) < 20) return;
        skillCacheTick = t;
        Player nearby = SkillEffects.nearestPlayer(level, pos, 16.0);
        cachedHydroBonus = SkillEffects.hydroFeBonus(nearby);
    }
    private int maxBurnTime = 200;
    protected final ContainerData dataAccess;

    public HydroReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDRO_REACTOR.get(), pos, state);
        this.energyStorage.withListener(this::setChanged);
        this.dataAccess = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> energyStorage.getEnergyStored() & 0xFFFF;
                    case 1 -> (energyStorage.getEnergyStored() >> 16) & 0xFFFF;
                    case 2 -> energyStorage.getMaxEnergyStored() & 0xFFFF;
                    case 3 -> (energyStorage.getMaxEnergyStored() >> 16) & 0xFFFF;
                    case 4 -> burnTime;
                    case 5 -> maxBurnTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 4 -> burnTime = value;
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HydroReactorBlockEntity entity) {
        if (level.isClientSide) return;

        entity.refreshSkillCache(level, pos);
        int gen = GENERATION_RATE;
        if (MachineUpgrades.has(entity.itemHandler, UPGRADE_SLOT, UpgradeItem.UpgradeType.EFFICIENCY)) {
            gen = Math.round(gen * 1.35f);
        }
        gen = Math.round(gen * entity.cachedHydroBonus);

        if (entity.burnTime > 0) {
            int energyBefore = entity.energyStorage.getEnergyStored();
            entity.burnTime--;
            entity.energyStorage.addEnergy(gen);
            if (energyBefore != entity.energyStorage.getEnergyStored() || entity.burnTime == 0) {
                entity.setChanged();
            }
        } else {
            ItemStack fuelStack = entity.itemHandler.getStackInSlot(0);
            if (!fuelStack.isEmpty() && entity.energyStorage.getEnergyStored() < CAPACITY) {
                fuelStack.shrink(1);
                entity.burnTime = 200;
                entity.maxBurnTime = 200;
                entity.setChanged();
            }
        }

        WorkingMachineTracker.setWorking(level, pos, entity.burnTime > 0);

        if (entity.energyStorage.getEnergyStored() > 0) {
            final int pushRate = GENERATION_RATE * 4;
            for (Direction dir : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor != null) {
                    neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(cap -> {
                        if (cap.canReceive()) {
                            int received = cap.receiveEnergy(Math.min(entity.energyStorage.getEnergyStored(), pushRate), false);
                            entity.energyStorage.consumeEnergy(received);
                        }
                    });
                }
            }
        }
    }

    public void drops() {
        if (level == null) return;
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            drops.add(itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, drops);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Гидро-Термальный Реактор");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new net.aquatech.ui.inventory.HydroReactorMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyOptional.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandlerOptional.cast();
            if (side == Direction.UP) return fuelOptional.cast();
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemHandlerOptional.invalidate();
        fuelOptional.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("BurnTime", burnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        InventoryNbt.loadFixedSize(itemHandler, tag.getCompound("Inventory"), 2);
        energyStorage.setEnergy(tag.getInt("Energy"));
        burnTime = tag.getInt("BurnTime");
    }
}
