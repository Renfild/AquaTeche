package net.aquatech.ui.block.entity;

import net.aquatech.ui.registry.ModBlockEntities;
import net.aquatech.ui.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OceanAltarBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);

    private int progress = 0;

    public boolean isWorking() {
        return progress > 0;
    }
    private static final int MAX_PROGRESS = 100;
    protected final ContainerData dataAccess;

    public OceanAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OCEAN_ALTAR.get(), pos, state);
        this.dataAccess = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> MAX_PROGRESS;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) progress = value;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OceanAltarBlockEntity entity) {
        if (level.isClientSide) {
            if (entity.progress > 0) {
                double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
                double y = pos.getY() + 1.2 + level.random.nextDouble() * 0.5;
                double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
                level.addParticle(ParticleTypes.GLOW, x, y, z, 0, 0.05, 0);
                level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.02, 0);
            }
            return;
        }

        // Neptune Trident recipe: Heart of the Sea + Echo Shard + Prismarine Crystals + Nether Star
        // (order-independent across the 4 input slots)
        if (matchesNeptuneRecipe(entity) && entity.itemHandler.getStackInSlot(4).isEmpty()) {
            entity.progress++;
            entity.setChanged();

            if (entity.progress >= MAX_PROGRESS) {
                for (int i = 0; i < 4; i++) {
                    entity.itemHandler.getStackInSlot(i).shrink(1);
                }
                entity.itemHandler.setStackInSlot(4, new ItemStack(ModItems.NEPTUNE_TRIDENT.get()));
                entity.progress = 0;
                level.playSound(null, pos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
                entity.setChanged();
            }
        } else if (entity.progress != 0) {
            entity.progress = 0;
            entity.setChanged();
        }
        WorkingMachineTracker.setWorking(level, pos, entity.progress > 0);
    }

    private static boolean matchesNeptuneRecipe(OceanAltarBlockEntity entity) {
        boolean heart = false, echo = false, crystals = false, star = false;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = entity.itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) return false;
            if (stack.is(Items.HEART_OF_THE_SEA) && !heart) heart = true;
            else if (stack.is(Items.ECHO_SHARD) && !echo) echo = true;
            else if (stack.is(Items.PRISMARINE_CRYSTALS) && !crystals) crystals = true;
            else if (stack.is(Items.NETHER_STAR) && !star) star = true;
            else return false;
        }
        return heart && echo && crystals && star;
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
        return Component.literal("Алтарь Морских Реликвий");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new net.aquatech.ui.inventory.OceanAltarMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOptional.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
    }
}
