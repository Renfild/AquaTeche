package net.aquatech.ui.inventory;

import net.aquatech.ui.item.RateModItem;
import net.aquatech.ui.registry.ModMenuTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/** Legacy AquaTech rod rate slots (RateMod only — lures/tackles removed). */
public class TackleBoxMenu extends AbstractContainerMenu {

    private final ItemStack rodStack;
    private final ItemStackHandler tackleInventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            saveTacklesToNbt();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof RateModItem;
        }
    };

    public TackleBoxMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.getMainHandItem());
    }

    public TackleBoxMenu(int containerId, Inventory inv, ItemStack rodStack) {
        super(ModMenuTypes.TACKLE_BOX_MENU.get(), containerId);
        this.rodStack = rodStack;
        loadTacklesFromNbt();

        int[] slotXs = {26, 62, 98, 134};
        for (int i = 0; i < 4; i++) {
            this.addSlot(new SlotItemHandler(tackleInventory, i, slotXs[i], 32) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof RateModItem;
                }
            });
        }

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    private void loadTacklesFromNbt() {
        if (!rodStack.isEmpty() && rodStack.hasTag() && rodStack.getTag().contains("TackleInventory")) {
            tackleInventory.deserializeNBT(rodStack.getTag().getCompound("TackleInventory"));
        }
    }

    private void saveTacklesToNbt() {
        if (!rodStack.isEmpty()) {
            CompoundTag tag = rodStack.getOrCreateTag();
            tag.put("TackleInventory", tackleInventory.serializeNBT());
        }
    }

    public ItemStack getRodStack() {
        return rodStack;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack sourceStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            sourceStack = stackInSlot.copy();

            if (index < 4) {
                if (!this.moveItemStackTo(stackInSlot, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.getItem() instanceof RateModItem) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
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
        return !rodStack.isEmpty() && player.getMainHandItem() == rodStack;
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
