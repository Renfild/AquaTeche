package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.aquatech.machines.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Гидротермальный Синтезатор:
 * - Питается FE-энергией и лавой (10,000 mB бак + заправка ведрами);
 * - Давление (0..100%): в Зелёной Зоне (60..85%) гарантирован крит-выход редкого бонуса;
 * - 3 слота готовой продукции.
 */
public class SynthesizerBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_FLUID_IN = 0;
    public static final int SLOT_FLUID_OUT = 1;
    public static final int SLOT_INPUT_A = 2;
    public static final int SLOT_INPUT_B = 3;
    public static final int SLOT_OUTPUT_1 = 4;
    public static final int SLOT_OUTPUT_2 = 5;
    public static final int SLOT_OUTPUT_3 = 6; // Bonus output (pressure green zone)
    public static final int SLOT_SPEED = 7;
    public static final int SLOT_EFF = 8;
    public static final int SLOT_BATTERY = 9;

    public static final int TANK_CAPACITY = 10000;
    public static final int LAVA_PER_CRAFT = 1000;
    public static final int PRESSURE_MIN_GREEN = 60;
    public static final int PRESSURE_MAX_GREEN = 85;

    private final FluidTank lavaTank;
    private final LazyOptional<IFluidHandler> fluidOptional;

    private int pressure = 20; // Стартовое атмосферное давление (20%)

    public record SynthRecipe(
            String inputA, String inputB,
            String out1, int count1,
            String out2, int count2,
            String bonus, int bonusCount,
            String label
    ) {}

    public static final List<SynthRecipe> RECIPES = List.of(
            new SynthRecipe(
                    "minecraft:gunpowder", "minecraft:basalt",
                    "aquatech_machines:volcanic_crystal", 1,
                    "minecraft:obsidian", 1,
                    "aquatech_machines:volcanic_crystal", 1,
                    "Сера/Порох + Базальт + Лава → Вулканический кристалл + Обсидиан (+Бонус кристалл)"
            ),
            new SynthRecipe(
                    "minecraft:copper_ingot", "minecraft:prismarine_crystals",
                    "aquatech_machines:abyssal_alloy", 1,
                    "minecraft:prismarine_shard", 2,
                    "aquatech_machines:abyssal_alloy", 1,
                    "Медный слиток + Кристаллы призмарина + Лава → Абиссальный сплав (+Бонус сплав)"
            ),
            new SynthRecipe(
                    "minecraft:redstone", "minecraft:glowstone_dust",
                    "minecraft:blaze_powder", 2,
                    "minecraft:magma_cream", 2,
                    "minecraft:blaze_rod", 1,
                    "Редстоун + Светопыль + Лава → 2x Огненный порошок + 2x Сгусток магмы (+Стержень ифрита)"
            ),
            new SynthRecipe(
                    "minecraft:diamond", "minecraft:obsidian",
                    "aquatech_machines:abyssal_alloy", 1,
                    "aquatech_machines:volcanic_crystal", 1,
                    "minecraft:diamond", 1,
                    "Алмаз + Обсидиан + Лава → Абиссальный сплав + Вулканический кристалл (+Алмаз)"
            ),
            new SynthRecipe(
                    "minecraft:iron_ingot", "minecraft:quartz",
                    "minecraft:nether_brick", 4,
                    "minecraft:iron_ingot", 1,
                    "aquatech_machines:volcanic_crystal", 1,
                    "Железо + Кварц + Лава → 4x Незерский кирпич + Слиток железа (+Кристалл)"
            )
    );

    public SynthesizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SYNTHESIZER.get(), pos, state, 10, 300000, 2048, 160, 80, 300000);
        this.lavaTank = new FluidTank(TANK_CAPACITY, f -> f.getFluid() == Fluids.LAVA) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
        this.fluidOptional = LazyOptional.of(() -> lavaTank);
        defineSlots(SLOT_OUTPUT_1, SLOT_OUTPUT_3, SLOT_SPEED, SLOT_EFF, SLOT_BATTERY);
    }

    public FluidTank getLavaTank() {
        return lavaTank;
    }

    public int getPressure() {
        return pressure;
    }

    public boolean isPressureGreen() {
        return pressure >= PRESSURE_MIN_GREEN && pressure <= PRESSURE_MAX_GREEN;
    }

    @Override
    protected int outputSlots() {
        return 3;
    }

    private boolean matchesIngredient(ItemStack stack, String targetId) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String str = id.toString();
        if (str.equals(targetId)) return true;

        String path = id.getPath();
        if ("minecraft:gunpowder".equals(targetId)) {
            return path.contains("sulfur") || path.contains("gunpowder");
        } else if ("minecraft:basalt".equals(targetId)) {
            return path.contains("basalt");
        } else if ("minecraft:copper_ingot".equals(targetId)) {
            return path.contains("copper_ingot") || path.equals("copper");
        } else if ("minecraft:iron_ingot".equals(targetId)) {
            return path.contains("iron_ingot") || path.equals("iron");
        } else if ("minecraft:quartz".equals(targetId)) {
            return path.contains("quartz");
        } else if ("minecraft:diamond".equals(targetId)) {
            return path.contains("diamond");
        } else if ("minecraft:obsidian".equals(targetId)) {
            return path.contains("obsidian");
        } else if ("minecraft:glowstone_dust".equals(targetId)) {
            return path.contains("glowstone");
        } else if ("minecraft:redstone".equals(targetId)) {
            return path.contains("redstone");
        } else if ("minecraft:prismarine_crystals".equals(targetId)) {
            return path.contains("prismarine_crystals");
        }
        return false;
    }

    private SynthRecipe currentRecipe() {
        ItemStack inA = items.getStackInSlot(SLOT_INPUT_A);
        ItemStack inB = items.getStackInSlot(SLOT_INPUT_B);
        if (inA.isEmpty() || inB.isEmpty()) return null;

        for (SynthRecipe r : RECIPES) {
            boolean matchDirect = matchesIngredient(inA, r.inputA()) && matchesIngredient(inB, r.inputB());
            boolean matchSwap = matchesIngredient(inB, r.inputA()) && matchesIngredient(inA, r.inputB());
            if (matchDirect || matchSwap) {
                return r;
            }
        }
        return null;
    }

    @Override
    protected boolean hasWork() {
        if (lavaTank.getFluidAmount() < LAVA_PER_CRAFT) return false;
        SynthRecipe r = currentRecipe();
        if (r == null) return false;

        ItemStack out1 = makeStack(r.out1(), r.count1());
        ItemStack out2 = makeStack(r.out2(), r.count2());
        if (out1.isEmpty() || out2.isEmpty()) return false;

        // Проверяем свободное место в слотах выходов
        return canFit(SLOT_OUTPUT_1, out1) && canFit(SLOT_OUTPUT_2, out2);
    }

    private boolean canFit(int slot, ItemStack stack) {
        ItemStack cur = items.getStackInSlot(slot);
        if (cur.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(cur, stack)
                && cur.getCount() + stack.getCount() <= cur.getMaxStackSize();
    }

    @Override
    protected void craftOnce() {
        SynthRecipe r = currentRecipe();
        if (r == null) return;
        if (lavaTank.getFluidAmount() < LAVA_PER_CRAFT) return;

        ItemStack out1 = makeStack(r.out1(), r.count1());
        ItemStack out2 = makeStack(r.out2(), r.count2());
        if (out1.isEmpty() || out2.isEmpty()) return;

        // Расходуем лаву и ингредиенты
        lavaTank.drain(LAVA_PER_CRAFT, IFluidHandler.FluidAction.EXECUTE);
        items.extractItem(SLOT_INPUT_A, 1, false);
        items.extractItem(SLOT_INPUT_B, 1, false);

        // Выдаем основную продукцию
        items.insertItem(SLOT_OUTPUT_1, out1, false);
        items.insertItem(SLOT_OUTPUT_2, out2, false);

        // Крит-выход в Зеленой Зоне давления (60..85%)
        if (isPressureGreen() && r.bonus() != null && !r.bonus().isEmpty()) {
            ItemStack bonus = makeStack(r.bonus(), r.bonusCount());
            if (!bonus.isEmpty()) {
                items.insertItem(SLOT_OUTPUT_3, bonus, false);
            }
        }
    }

    private ItemStack makeStack(String id, int count) {
        ResourceLocation loc = new ResourceLocation(id);
        Item item = ForgeRegistries.ITEMS.getValue(loc);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item, count);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.SynthesizerMenu(id, inv, this);
    }

    @Override
    protected void onServerTick() {
        // 1. Дренаж ведра с лавой из входного слота
        drainLavaBucket();

        // 2. Логика нагнетания и сброса давления
        if (isWorking()) {
            // При работе давление нагнетается до максимума
            if (level != null && level.getGameTime() % 2 == 0 && pressure < 100) {
                pressure++;
                setChanged();
            }
        } else {
            // При простое давление плавно нормализуется к 20%
            if (level != null && level.getGameTime() % 4 == 0) {
                if (pressure > 20) {
                    pressure--;
                    setChanged();
                } else if (pressure < 20) {
                    pressure++;
                    setChanged();
                }
            }
        }
    }

    private void drainLavaBucket() {
        ItemStack in = items.getStackInSlot(SLOT_FLUID_IN);
        if (in.isEmpty()) return;

        // 1. Стандартное ванильное ведро лавы
        if (in.is(Items.LAVA_BUCKET)) {
            if (lavaTank.getSpace() >= 1000) {
                ItemStack out = items.getStackInSlot(SLOT_FLUID_OUT);
                if (out.isEmpty() || (out.is(Items.BUCKET) && out.getCount() < out.getMaxStackSize())) {
                    in.shrink(1);
                    lavaTank.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
                    if (out.isEmpty()) {
                        items.setStackInSlot(SLOT_FLUID_OUT, new ItemStack(Items.BUCKET));
                    } else {
                        out.grow(1);
                    }
                    setChanged();
                }
            }
            return;
        }

        // 2. Любая модовая емкость (FluidHandlerItem - капсулы, универсальные ведра и т.д.)
        var capOpt = in.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if (capOpt.isPresent()) {
            net.minecraftforge.fluids.capability.IFluidHandlerItem handler = capOpt.orElse(null);
            if (handler != null && lavaTank.getSpace() > 0) {
                FluidStack sim = handler.drain(new FluidStack(Fluids.LAVA, lavaTank.getSpace()), IFluidHandler.FluidAction.SIMULATE);
                if (!sim.isEmpty() && sim.getFluid() == Fluids.LAVA) {
                    ItemStack emptyContainer = handler.getContainer();
                    ItemStack out = items.getStackInSlot(SLOT_FLUID_OUT);
                    if (out.isEmpty() || (ItemStack.isSameItemSameTags(out, emptyContainer) && out.getCount() < out.getMaxStackSize())) {
                        FluidStack executed = handler.drain(new FluidStack(Fluids.LAVA, lavaTank.getSpace()), IFluidHandler.FluidAction.EXECUTE);
                        lavaTank.fill(executed, IFluidHandler.FluidAction.EXECUTE);
                        in.shrink(1);
                        if (out.isEmpty()) {
                            items.setStackInSlot(SLOT_FLUID_OUT, emptyContainer);
                        } else {
                            out.grow(1);
                        }
                        setChanged();
                    }
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
                    case 6 -> pressure;
                    case 7 -> lavaTank.getFluidAmount();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 6 -> pressure = value;
                    case 7 -> lavaTank.setFluid(new FluidStack(Fluids.LAVA, value));
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
        tag.putInt("Pressure", pressure);
        CompoundTag tankTag = new CompoundTag();
        lavaTank.writeToNBT(tankTag);
        tag.put("LavaTank", tankTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.pressure = tag.getInt("Pressure");
        if (tag.contains("LavaTank")) {
            lavaTank.readFromNBT(tag.getCompound("LavaTank"));
        }
    }
}
