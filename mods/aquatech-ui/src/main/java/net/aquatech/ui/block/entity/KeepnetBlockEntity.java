package net.aquatech.ui.block.entity;

import net.aquatech.ui.block.KeepnetBlock;
import net.aquatech.ui.block.KeepnetTier;
import net.aquatech.ui.inventory.KeepnetMenu;
import net.aquatech.ui.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Садок: ячейки зависят от тира блока, принимает только рыбу (тег minecraft:fishes, в него Starcatcher добавляет весь свой улов). */
public class KeepnetBlockEntity extends BlockEntity implements MenuProvider {

    private final KeepnetTier tier;
    private final ItemStackHandler inventory;
    private LazyOptional<ItemStackHandler> handlerCap;

    public KeepnetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_KEEPNET.get(), pos, state);
        this.tier = ((KeepnetBlock) state.getBlock()).getTier();
        this.inventory = new ItemStackHandler(tier.slots()) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return isFish(stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.handlerCap = LazyOptional.of(() -> inventory);
    }

    public static boolean isFish(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    public KeepnetTier getTier() {
        return tier;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inventory.getStackInSlot(i));
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new KeepnetMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Items"));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handlerCap.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        handlerCap = LazyOptional.of(() -> inventory);
    }
}
