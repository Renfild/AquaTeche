package net.aquatech.ui.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Output-only view of a slot range: pipes can extract, inserts are rejected.
 */
public final class OutputOnlyWrapper extends RangedWrapper {

    public OutputOnlyWrapper(IItemHandlerModifiable compose, int minSlot, int maxSlotExclusive) {
        super(compose, minSlot, maxSlotExclusive);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }
}
