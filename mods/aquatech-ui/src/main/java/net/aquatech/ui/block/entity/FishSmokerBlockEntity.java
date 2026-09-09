package net.aquatech.ui.block.entity;

import net.aquatech.ui.block.FishSmokerBlock;
import net.aquatech.ui.inventory.FishSmokerMenu;
import net.aquatech.ui.registry.ModBlockEntities;
import net.aquatech.ui.registry.ModItems;
import net.aquatech.ui.util.FishPriceTable;
import net.aquatech.ui.util.InventoryNbt;
import net.aquatech.ui.util.OutputOnlyWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Коптильня: raw starcatcher fish + kelp bio pellet → smoked fish (×2 in fish shop).
 * Junk fish (cheap per fish_shop.json) is ground into fish meal instead.
 */
public class FishSmokerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int OP_TICKS = 80;
    public static final int OPS_PER_PELLET = 4;
    public static final int PELLET_BURN_TICKS = OP_TICKS * OPS_PER_PELLET;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // insertItem() refuses slots where isItemValid is false — output must stay allowed
            if (slot == SLOT_FUEL) {
                return stack.is(ModItems.KELP_BIO_PELLET.get());
            }
            return true;
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> inputOptional = LazyOptional.of(() -> new RangedWrapper(itemHandler, SLOT_INPUT, SLOT_FUEL));
    private final LazyOptional<IItemHandler> outputOptional = LazyOptional.of(() -> new OutputOnlyWrapper(itemHandler, SLOT_OUTPUT, SLOT_OUTPUT + 1));

    private int progress = 0;
    private int burnLeft = 0;
    private int burnMax = 0;

    public boolean isWorking() {
        return progress > 0;
    }

    protected final ContainerData dataAccess;

    public FishSmokerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_SMOKER.get(), pos, state);
        this.dataAccess = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> OP_TICKS;
                    case 2 -> burnLeft;
                    case 3 -> burnMax;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 2 -> burnLeft = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FishSmokerBlockEntity entity) {
        if (level.isClientSide) return;

        ItemStack input = entity.itemHandler.getStackInSlot(SLOT_INPUT);
        boolean canProcess = entity.isSmokableInput(input) && entity.hasOutputRoom();

        if (entity.burnLeft <= 0 && canProcess) {
            ItemStack fuel = entity.itemHandler.getStackInSlot(SLOT_FUEL);
            if (fuel.is(ModItems.KELP_BIO_PELLET.get())) {
                fuel.shrink(1);
                entity.burnLeft = PELLET_BURN_TICKS;
                entity.burnMax = PELLET_BURN_TICKS;
                entity.setChanged();
            }
        }

        int progressBefore = entity.progress;
        int burnBefore = entity.burnLeft;
        if (entity.burnLeft > 0) {
            entity.burnLeft--;
            if (canProcess) {
                entity.progress++;
                if (entity.progress >= OP_TICKS) {
                    entity.progress = 0;
                    entity.process(input);
                }
            } else {
                entity.progress = 0;
            }
        } else if (entity.progress > 0) {
            entity.progress = Math.max(0, entity.progress - 2);
        }

        boolean lit = entity.burnLeft > 0;
        if (state.getValue(FishSmokerBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(FishSmokerBlock.LIT, lit), 3);
        }
        if (progressBefore != entity.progress || burnBefore != entity.burnLeft) {
            entity.setChanged();
        }
        WorkingMachineTracker.setWorking(level, pos, entity.progress > 0);
    }

    /** Only sellable starcatcher fish (fresh or already stamped), never smoked twice. */
    private boolean isSmokableInput(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.hasTag() && stack.getTag().getBoolean("AquaSmoked")) return false;
        return net.aquatech.ui.fishing.FishingLootHandler.isStarCatcherFishItem(stack);
    }

    private boolean hasOutputRoom() {
        ItemStack out = itemHandler.getStackInSlot(SLOT_OUTPUT);
        return out.isEmpty() || out.getCount() < out.getMaxStackSize();
    }

    private void process(ItemStack input) {
        ItemStack out;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(input.getItem());
        if (FishPriceTable.isJunkFish(id != null ? id.toString() : "")) {
            out = new ItemStack(ModItems.FISH_MEAL.get());
        } else {
            out = input.copy();
            out.setCount(1);
            CompoundTag tag = out.getOrCreateTag();
            tag.putBoolean("AquaSmoked", true);
            String baseName = input.getHoverName().getString();
            out.setHoverName(Component.literal(baseName + " (копчёный)")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD).withItalic(false)));
        }

        ItemStack rest = itemHandler.insertItem(SLOT_OUTPUT, out, false);
        if (!rest.isEmpty()) {
            return;
        }
        input.shrink(1);
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
        return Component.translatable("block.aquatech_ui.fish_smoker");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FishSmokerMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
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
        itemHandlerOptional.invalidate();
        inputOptional.invalidate();
        outputOptional.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("Progress", progress);
        tag.putInt("BurnLeft", burnLeft);
        tag.putInt("BurnMax", burnMax);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        InventoryNbt.loadFixedSize(itemHandler, tag.getCompound("Inventory"), 3);
        progress = tag.getInt("Progress");
        burnLeft = tag.getInt("BurnLeft");
        burnMax = tag.getInt("BurnMax");
    }
}
