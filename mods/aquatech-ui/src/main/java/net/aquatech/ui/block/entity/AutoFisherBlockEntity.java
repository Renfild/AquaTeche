package net.aquatech.ui.block.entity;

import net.aquatech.ui.capability.SkillEffects;
import net.aquatech.ui.fishing.AquaTechFishingRodItem;
import net.aquatech.ui.fishing.FishingLootHandler;
import net.aquatech.ui.fishing.FishingRodCompat;
import net.aquatech.ui.fishing.RodDurability;
import net.aquatech.ui.fishing.StarCatcherAttachments;
import net.aquatech.ui.inventory.AutoFisherMenu;
import net.aquatech.ui.item.RateModItem;
import net.aquatech.ui.item.UpgradeItem;
import net.aquatech.ui.registry.ModBlockEntities;
import net.aquatech.ui.util.CustomEnergyStorage;
import net.aquatech.ui.util.InventoryNbt;
import net.aquatech.ui.util.MachineUpgrades;
import net.aquatech.ui.util.OutputOnlyWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AutoFisherBlockEntity extends BlockEntity implements MenuProvider {

    public static final int CAPACITY = 50000;
    public static final int MAX_RECEIVE = 1000;
    public static final int ENERGY_PER_TICK = 25;
    public static final int MAX_PROGRESS = 100;
    /** Rod=0, output 1..6 (3×2), upgrade=7. */
    public static final int OUTPUT_SLOTS = 6;
    public static final int UPGRADE_SLOT = 7;
    public static final int SLOT_COUNT = 8;

    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(CAPACITY, MAX_RECEIVE, 0);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energyStorage);

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return FishingRodCompat.isSupportedRod(stack);
            }
            if (slot >= 1 && slot <= OUTPUT_SLOTS) return true;
            if (slot == UPGRADE_SLOT) return stack.getItem() instanceof UpgradeItem || stack.getItem() instanceof RateModItem;
            return false;
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> inputOptional = LazyOptional.of(() -> new RangedWrapper(itemHandler, 0, 1));
    private final LazyOptional<IItemHandler> outputOptional = LazyOptional.of(
            () -> new OutputOnlyWrapper(itemHandler, 1, 1 + OUTPUT_SLOTS));

    private int progress = 0;
    private int skillCacheTick = -1;
    private float cachedEnergyFactor = 1f;
    private float cachedSpeedMult = 1f;
    private Player cachedNearby;
    protected final ContainerData dataAccess;

    public boolean isWorking() {
        return progress > 0;
    }

    private void refreshSkillCache(Level level, BlockPos pos) {
        int t = (int) (level.getGameTime() & 0x7FFFFFFF);
        if (skillCacheTick >= 0 && (t - skillCacheTick) < 20) return;
        skillCacheTick = t;

        this.cachedNearby = SkillEffects.nearestPlayer(level, pos, 16.0);
        if (cachedNearby != null) {
            this.cachedEnergyFactor = SkillEffects.energyCostFactor(cachedNearby);
            this.cachedSpeedMult = SkillEffects.machineSpeedMultiplier(cachedNearby);
        } else {
            this.cachedEnergyFactor = 1f;
            this.cachedSpeedMult = 1f;
        }
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T be) {
        if (be instanceof AutoFisherBlockEntity entity) {
            tick(level, pos, state, entity);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AutoFisherBlockEntity entity) {
        if (entity.isWorking()) {
            if (level.random.nextFloat() < 0.25f) {
                level.addParticle(ParticleTypes.SPLASH,
                        pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4,
                        pos.getY() + 0.8,
                        pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4,
                        0, 0.1, 0);
            }
        }
    }

    public AutoFisherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTO_FISHER.get(), pos, state);
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

    public static void tick(Level level, BlockPos pos, BlockState state, AutoFisherBlockEntity entity) {
        if (level.isClientSide) return;

        entity.refreshSkillCache(level, pos);
        Player nearby = entity.cachedNearby;
        ItemStack rodStack = entity.itemHandler.getStackInSlot(0);
        boolean hasValidRod = !rodStack.isEmpty() && FishingRodCompat.isSupportedRod(rodStack);
        int baseCost = MachineUpgrades.energyCost(entity.itemHandler, UPGRADE_SLOT, ENERGY_PER_TICK);
        int cost = Math.max(1, Math.round(baseCost * entity.cachedEnergyFactor));
        boolean hasEnergy = entity.energyStorage.getEnergyStored() >= cost;
        boolean wasWorking = entity.progress > 0;

        if (hasValidRod && hasEnergy && entity.hasWaterNearby(level, pos) && entity.hasSpaceInOutput()) {
            int energyBefore = entity.energyStorage.getEnergyStored();
            int progressBefore = entity.progress;
            entity.energyStorage.consumeEnergy(cost);
            float progress = MachineUpgrades.progressPerTick(entity.itemHandler, UPGRADE_SLOT) * entity.cachedSpeedMult;
            entity.progress += Math.max(1, Math.round(progress));

            if (entity.progress >= MAX_PROGRESS) {
                entity.progress = 0;
                entity.doFishOperation(level, rodStack, nearby);
            }
            if (energyBefore != entity.energyStorage.getEnergyStored() || progressBefore != entity.progress) {
                entity.setChanged();
            }
        } else if (entity.progress > 0) {
            entity.progress = Math.max(0, entity.progress - 2);
            entity.setChanged();
        }
        WorkingMachineTracker.setWorking(level, pos, entity.progress > 0);
        if (wasWorking != entity.progress > 0) {
            entity.setChanged();
        }
    }

    private boolean hasWaterNearby(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            var fluid = level.getFluidState(pos.relative(dir));
            if (fluid.getType() == Fluids.WATER || fluid.getType() == Fluids.FLOWING_WATER) {
                return true;
            }
        }
        var above = level.getFluidState(pos.above());
        return above.getType() == Fluids.WATER || above.getType() == Fluids.FLOWING_WATER;
    }

    private boolean hasSpaceInOutput() {
        for (int i = 1; i <= OUTPUT_SLOTS; i++) {
            ItemStack slot = itemHandler.getStackInSlot(i);
            if (slot.isEmpty() || slot.getCount() < slot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private void doFishOperation(Level level, ItemStack rodStack, Player nearby) {
        AquaTechFishingRodItem.RodType rodType = FishingRodCompat.resolveRodType(rodStack);
        if (rodType == null) {
            rodType = AquaTechFishingRodItem.RodType.NOVICE;
        }

        int rodRate = FishingLootHandler.readRateMultiplier(rodStack);
        ItemStack upgradeStack = itemHandler.getStackInSlot(UPGRADE_SLOT);
        int upgradeRate = (upgradeStack.getItem() instanceof RateModItem r) ? r.getMultiplier() : 1;
        int effectiveRate = Math.max(1, Math.max(rodRate, upgradeRate));

        if (nearby instanceof ServerPlayer serverPlayer) {
            FishingLootHandler.bumpCatchStat(serverPlayer);
        }
        List<ItemStack> loot = FishingLootHandler.generateLoot(rodType, level.getRandom(), rodStack, nearby, effectiveRate);

        int remainingBudget = 64;
        for (ItemStack drop : loot) {
            if (remainingBudget <= 0) {
                break;
            }
            if (drop.getCount() > remainingBudget) {
                drop.setCount(remainingBudget);
            }
            remainingBudget -= drop.getCount();
            insertIntoOutput(drop);
        }

        if (MachineUpgrades.has(itemHandler, UPGRADE_SLOT, UpgradeItem.UpgradeType.OCEAN_BOUNTY)) {
            List<ItemStack> bonusFish = generateOceanBountyFish(level.getRandom());
            for (ItemStack fish : bonusFish) {
                if (remainingBudget <= 0) {
                    break;
                }
                if (effectiveRate > 1) {
                    fish.setCount(Math.min(fish.getMaxStackSize(), fish.getCount() * effectiveRate));
                }
                if (fish.getCount() > remainingBudget) {
                    fish.setCount(remainingBudget);
                }
                remainingBudget -= fish.getCount();
                insertIntoOutput(fish);
            }
        }

        StarCatcherAttachments.consumeRateCatch(rodStack);
        if (!RodDurability.wearOne(rodStack, null) || rodStack.isEmpty()) {
            itemHandler.setStackInSlot(0, ItemStack.EMPTY);
        } else {
            itemHandler.setStackInSlot(0, rodStack);
        }
        setChanged();
    }

    private List<ItemStack> generateOceanBountyFish(RandomSource random) {
        List<ItemStack> fish = new ArrayList<>();
        int count = 1 + random.nextInt(3); // 1-3 fish per catch
        for (int i = 0; i < count; i++) {
            float r = random.nextFloat();
            if (r < 0.04f) {
                fish.add(getModItem("starcatcher:elderscale", Items.COD, 1)); // Legendary (100c)
            } else if (r < 0.12f) {
                fish.add(getModItem("starcatcher:silverveil_perch", Items.SALMON, 1)); // Epic (50c)
            } else if (r < 0.22f) {
                fish.add(getModItem("starcatcher:hollowbelly_darter", Items.TROPICAL_FISH, 1)); // Epic (40c)
            } else if (r < 0.35f) {
                fish.add(getModItem("starcatcher:carpenjoe", Items.SALMON, 1)); // Epic (30c)
            } else if (r < 0.50f) {
                fish.add(getModItem("starcatcher:silverfin_pike", Items.COD, 1)); // Epic (25c)
            } else if (r < 0.65f) {
                fish.add(getModItem("starcatcher:sunny_sturgeon", Items.SALMON, 1)); // Rare (20c)
            } else if (r < 0.80f) {
                fish.add(getModItem("starcatcher:rockgill", Items.COD, 1)); // Rare (15c)
            } else if (r < 0.92f) {
                fish.add(getModItem("starcatcher:driftfin", Items.COD, 1)); // Common (10c)
            } else {
                fish.add(new ItemStack(random.nextBoolean() ? Items.PUFFERFISH : Items.TROPICAL_FISH, 1));
            }
        }
        return fish;
    }

    private static ItemStack getModItem(String regId, Item fallback, int count) {
        ResourceLocation rl = ResourceLocation.tryParse(regId);
        if (rl != null && ForgeRegistries.ITEMS.containsKey(rl)) {
            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item != null && item != Items.AIR) {
                return new ItemStack(item, count);
            }
        }
        return new ItemStack(fallback, count);
    }

    private void insertIntoOutput(ItemStack stackToInsert) {
        if (stackToInsert == null || stackToInsert.isEmpty()) return;
        ItemStack copy = stackToInsert.copy();
        for (int i = 1; i <= OUTPUT_SLOTS; i++) {
            copy = itemHandler.insertItem(i, copy, false);
            if (copy.isEmpty()) return;
        }
        copy = pushToNeighbors(copy);
        if (!copy.isEmpty() && level != null && !level.isClientSide) {
            Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    copy);
        }
    }

    private ItemStack pushToNeighbors(ItemStack stack) {
        if (level == null || stack.isEmpty()) return stack;
        ItemStack remaining = stack;
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            if (be == null) continue;
            IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
                remaining = handler.insertItem(slot, remaining, false);
            }
            if (remaining.isEmpty()) return ItemStack.EMPTY;
        }
        return remaining;
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
        return Component.translatable("block.aquatech_ui.auto_fisher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AutoFisherMenu(containerId, playerInventory, this, this.dataAccess);
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

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            load(pkt.getTag());
        }
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
        InventoryNbt.loadFixedSize(itemHandler, tag.getCompound("Inventory"), SLOT_COUNT);
        energyStorage.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
    }
}

