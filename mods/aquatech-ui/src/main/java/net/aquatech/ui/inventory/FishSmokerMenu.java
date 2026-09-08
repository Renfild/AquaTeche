package net.aquatech.ui.inventory;

import net.aquatech.ui.block.entity.FishSmokerBlockEntity;
import net.aquatech.ui.fishing.FishingLootHandler;
import net.aquatech.ui.registry.ModBlocks;
import net.aquatech.ui.registry.ModItems;
import net.aquatech.ui.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Slot layout matches {@code textures/gui/fish_smoker.png}:
 * input (56,17), fuel (56,53), output (116,35).
 */
public class FishSmokerMenu extends AbstractContainerMenu {

    public final FishSmokerBlockEntity blockEntity;
    private final ContainerData data;

    public FishSmokerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public FishSmokerMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.FISH_SMOKER_MENU.get(), containerId);
        checkContainerSize(inv, 3);
        this.blockEntity = (FishSmokerBlockEntity) entity;
        this.data = data;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, FishSmokerBlockEntity.SLOT_INPUT, 56, 17) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return FishingLootHandler.isStarCatcherFishItem(stack) && !isSmoked(stack);
                }
            });

            this.addSlot(new SlotItemHandler(handler, FishSmokerBlockEntity.SLOT_FUEL, 56, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.KELP_BIO_PELLET.get());
                }
            });

            this.addSlot(new SlotItemHandler(handler, FishSmokerBlockEntity.SLOT_OUTPUT, 116, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        });

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    private static boolean isSmoked(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("AquaSmoked");
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        return maxProgress != 0 && progress != 0 ? progress * 24 / maxProgress : 0;
    }

    public int getScaledBurn() {
        int burn = this.data.get(2);
        int burnMax = this.data.get(3);
        return burnMax != 0 && burn != 0 ? burn * 14 / burnMax : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack sourceStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            sourceStack = stackInSlot.copy();

            // machine: input + fuel + output = 3 slots (menu order 0..2)
            if (index < 3) {
                if (!this.moveItemStackTo(stackInSlot, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (FishingLootHandler.isStarCatcherFishItem(stackInSlot) && !isSmoked(stackInSlot)) {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.is(ModItems.KELP_BIO_PELLET.get())) {
                if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == sourceStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, stackInSlot);
        }

        return sourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.FISH_SMOKER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
