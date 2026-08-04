package net.aquatech.ui.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;

/**
 * ItemStackHandler.deserializeNBT() resets Size from NBT, so world-saved
 * machines with fewer slots crash when menus add upgrade slots.
 * Always force the current code size after (or before) deserialize.
 */
public final class InventoryNbt {

    private InventoryNbt() {
    }

    public static void loadFixedSize(ItemStackHandler handler, CompoundTag inventoryTag, int expectedSlots) {
        CompoundTag copy = inventoryTag == null ? new CompoundTag() : inventoryTag.copy();
        copy.putInt("Size", expectedSlots);
        handler.deserializeNBT(copy);
    }
}
