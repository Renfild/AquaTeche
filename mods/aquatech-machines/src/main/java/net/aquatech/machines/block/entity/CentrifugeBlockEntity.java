package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Батиметрическая Центрифуга / Десалинатор:
 * - Разделяет морскую воду на чистейший дистиллят и минеральные соли;
 * - Два резервуара по 10,000 mB: сырая вода и дистиллят;
 * - Сетка 2×2 под сухие минералы и соли (Морская соль, литий, магний, самородки).
 */
public class CentrifugeBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_RAW_WATER_IN = 0;
    public static final int SLOT_RAW_WATER_OUT = 1;
    public static final int SLOT_DISTILL_EMPTY = 2;
    public static final int SLOT_DISTILL_FULL = 3;
    public static final int SLOT_MINERAL_0 = 4;
    public static final int SLOT_MINERAL_1 = 5;
    public static final int SLOT_MINERAL_2 = 6;
    public static final int SLOT_MINERAL_3 = 7;
    public static final int SLOT_SPEED = 8;
    public static final int SLOT_EFF = 9;
    public static final int SLOT_BATTERY = 10;

    public static final int TANK_CAPACITY = 10000;
    public static final int WATER_PER_CYCLE = 1000;
    public static final int DISTILLATE_PRODUCED = 800;

    private final FluidTank rawWaterTank;
    private final FluidTank distillateTank;
    private final LazyOptional<IFluidHandler> fluidOptional;

    private final Random random = new Random();

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CENTRIFUGE.get(), pos, state, 11, 250000, 1024, 120, 50, 250000);
        this.rawWaterTank = new FluidTank(TANK_CAPACITY, f -> f.getFluid() == Fluids.WATER) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
        this.distillateTank = new FluidTank(TANK_CAPACITY, f -> f.getFluid() == Fluids.WATER) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };

        // Двойной IFluidHandler: заполняет сырой бак, сливает дистиллят
        this.fluidOptional = LazyOptional.of(() -> new IFluidHandler() {
            @Override
            public int getTanks() {
                return 2;
            }

            @NotNull
            @Override
            public FluidStack getFluidInTank(int tank) {
                return tank == 0 ? rawWaterTank.getFluid() : distillateTank.getFluid();
            }

            @Override
            public int getTankCapacity(int tank) {
                return TANK_CAPACITY;
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                return tank == 0 ? rawWaterTank.isFluidValid(stack) : distillateTank.isFluidValid(stack);
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return rawWaterTank.fill(resource, action);
            }

            @NotNull
            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return distillateTank.drain(resource, action);
            }

            @NotNull
            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return distillateTank.drain(maxDrain, action);
            }
        });

        defineSlots(SLOT_MINERAL_0, SLOT_MINERAL_3, SLOT_SPEED, SLOT_EFF, SLOT_BATTERY);
    }

    public FluidTank getRawWaterTank() {
        return rawWaterTank;
    }

    public FluidTank getDistillateTank() {
        return distillateTank;
    }

    @Override
    protected int outputSlots() {
        return 4;
    }

    @Override
    protected boolean hasWork() {
        // Требуется хотя бы 1,000 mB сырой воды и место под 800 mB дистиллята
        if (rawWaterTank.getFluidAmount() < WATER_PER_CYCLE) return false;
        if (distillateTank.getSpace() < DISTILLATE_PRODUCED) return false;

        // Должно быть место хотя бы в одном слоте минералов
        return canFitAnyMineral();
    }

    private boolean canFitAnyMineral() {
        for (int i = SLOT_MINERAL_0; i <= SLOT_MINERAL_3; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void craftOnce() {
        if (rawWaterTank.getFluidAmount() < WATER_PER_CYCLE) return;
        if (distillateTank.getSpace() < DISTILLATE_PRODUCED) return;

        // Расходуем воду и производим дистиллят
        rawWaterTank.drain(WATER_PER_CYCLE, IFluidHandler.FluidAction.EXECUTE);
        distillateTank.fill(new FluidStack(Fluids.WATER, DISTILLATE_PRODUCED), IFluidHandler.FluidAction.EXECUTE);

        // 1. Морская соль (гарантированно 1-2 шт в слот 4)
        ItemStack salt = new ItemStack(ModItems.SEA_SALT.get(), 1 + random.nextInt(2));
        insertOrDrop(SLOT_MINERAL_0, salt);

        // 2. Литиево-магниевый концентрат (70% шанс в слот 5)
        if (random.nextInt(100) < 70) {
            Item mineral = findItemOr("industrialupgrade:baseore2/lithium", Items.REDSTONE);
            insertOrDrop(SLOT_MINERAL_1, new ItemStack(mineral, 1 + random.nextInt(2)));
        }

        // 3. Золотые/платиновые микро-самородки (45% шанс в слот 6)
        if (random.nextInt(100) < 45) {
            insertOrDrop(SLOT_MINERAL_2, new ItemStack(Items.GOLD_NUGGET, 1 + random.nextInt(3)));
        }

        // 4. Осадок призмарина / морской кристалл (30% шанс в слот 7)
        if (random.nextInt(100) < 30) {
            Item marine = random.nextBoolean() ? Items.PRISMARINE_SHARD : Items.PRISMARINE_CRYSTALS;
            insertOrDrop(SLOT_MINERAL_3, new ItemStack(marine, 1));
        }
    }

    private void insertOrDrop(int slot, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack rest = items.insertItem(slot, stack, false);
        if (!rest.isEmpty()) {
            for (int i = SLOT_MINERAL_0; i <= SLOT_MINERAL_3; i++) {
                if (i != slot) {
                    rest = items.insertItem(i, rest, false);
                    if (rest.isEmpty()) break;
                }
            }
        }
    }

    private Item findItemOr(String id, Item fallback) {
        ResourceLocation loc = new ResourceLocation(id);
        Item item = ForgeRegistries.ITEMS.getValue(loc);
        return (item != null && item != Items.AIR) ? item : fallback;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.CentrifugeMenu(id, inv, this);
    }

    @Override
    protected void onServerTick() {
        // Обработка ведер и емкостей для сырой воды и дистиллята
        drainWaterBuckets();
        fillDistillateBuckets();
    }

    private void drainWaterBuckets() {
        ItemStack in = items.getStackInSlot(SLOT_RAW_WATER_IN);
        if (in.isEmpty()) return;

        // 1. Стандартное ванильное ведро воды
        if (in.is(Items.WATER_BUCKET)) {
            if (rawWaterTank.getSpace() >= 1000) {
                ItemStack out = items.getStackInSlot(SLOT_RAW_WATER_OUT);
                if (out.isEmpty() || (out.is(Items.BUCKET) && out.getCount() < out.getMaxStackSize())) {
                    in.shrink(1);
                    rawWaterTank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                    if (out.isEmpty()) {
                        items.setStackInSlot(SLOT_RAW_WATER_OUT, new ItemStack(Items.BUCKET));
                    } else {
                        out.grow(1);
                    }
                    setChanged();
                }
            }
            return;
        }

        // 2. Generic Forge FluidHandler Item
        var capOpt = in.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if (capOpt.isPresent()) {
            net.minecraftforge.fluids.capability.IFluidHandlerItem handler = capOpt.orElse(null);
            if (handler != null && rawWaterTank.getSpace() > 0) {
                FluidStack sim = handler.drain(new FluidStack(Fluids.WATER, rawWaterTank.getSpace()), IFluidHandler.FluidAction.SIMULATE);
                if (!sim.isEmpty() && sim.getFluid() == Fluids.WATER) {
                    ItemStack emptyContainer = handler.getContainer();
                    ItemStack out = items.getStackInSlot(SLOT_RAW_WATER_OUT);
                    if (out.isEmpty() || (ItemStack.isSameItemSameTags(out, emptyContainer) && out.getCount() < out.getMaxStackSize())) {
                        FluidStack executed = handler.drain(new FluidStack(Fluids.WATER, rawWaterTank.getSpace()), IFluidHandler.FluidAction.EXECUTE);
                        rawWaterTank.fill(executed, IFluidHandler.FluidAction.EXECUTE);
                        in.shrink(1);
                        if (out.isEmpty()) {
                            items.setStackInSlot(SLOT_RAW_WATER_OUT, emptyContainer);
                        } else {
                            out.grow(1);
                        }
                        setChanged();
                    }
                }
            }
        }
    }

    private void fillDistillateBuckets() {
        ItemStack empty = items.getStackInSlot(SLOT_DISTILL_EMPTY);
        if (empty.isEmpty()) return;

        // 1. Наполнение ведра
        if (empty.is(Items.BUCKET)) {
            if (distillateTank.getFluidAmount() >= 1000) {
                ItemStack full = items.getStackInSlot(SLOT_DISTILL_FULL);
                if (full.isEmpty() || (full.is(Items.WATER_BUCKET) && full.getCount() < full.getMaxStackSize())) {
                    empty.shrink(1);
                    distillateTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    if (full.isEmpty()) {
                        items.setStackInSlot(SLOT_DISTILL_FULL, new ItemStack(Items.WATER_BUCKET));
                    } else {
                        full.grow(1);
                    }
                    setChanged();
                }
            }
            return;
        }

        // 2. Наполнение стеклянных колб
        if (empty.is(Items.GLASS_BOTTLE)) {
            if (distillateTank.getFluidAmount() >= 250) {
                ItemStack full = items.getStackInSlot(SLOT_DISTILL_FULL);
                ItemStack potion = net.minecraft.world.item.alchemy.PotionUtils.setPotion(
                        new ItemStack(Items.POTION), net.minecraft.world.item.alchemy.Potions.WATER);
                if (full.isEmpty() || (ItemStack.isSameItemSameTags(full, potion) && full.getCount() < full.getMaxStackSize())) {
                    empty.shrink(1);
                    distillateTank.drain(250, IFluidHandler.FluidAction.EXECUTE);
                    if (full.isEmpty()) {
                        items.setStackInSlot(SLOT_DISTILL_FULL, potion);
                    } else {
                        full.grow(1);
                    }
                    setChanged();
                }
            }
        }
    }

    public ContainerData createContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> energy.getEnergy() & 0xFFFF;
                    case 3 -> (energy.getEnergy() >> 16) & 0xFFFF;
                    case 4 -> maxEnergy & 0xFFFF;
                    case 5 -> (maxEnergy >> 16) & 0xFFFF;
                    case 6 -> rawWaterTank.getFluidAmount();
                    case 7 -> distillateTank.getFluidAmount();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 6 -> rawWaterTank.setFluid(new FluidStack(Fluids.WATER, value));
                    case 7 -> distillateTank.setFluid(new FluidStack(Fluids.WATER, value));
                }
            }

            @Override
            public int getCount() {
                return 8;
            }
        };
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidOptional.invalidate();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag rawTag = new CompoundTag();
        rawWaterTank.writeToNBT(rawTag);
        tag.put("RawTank", rawTag);

        CompoundTag distTag = new CompoundTag();
        distillateTank.writeToNBT(distTag);
        tag.put("DistillTank", distTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("RawTank")) {
            rawWaterTank.readFromNBT(tag.getCompound("RawTank"));
        }
        if (tag.contains("DistillTank")) {
            distillateTank.readFromNBT(tag.getCompound("DistillTank"));
        }
    }
}
