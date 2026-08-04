package net.aquatech.ui.util;

import net.aquatech.ui.item.UpgradeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Shared helpers for reading AquaTech machine upgrade modules from an inventory slot.
 */
public final class MachineUpgrades {

    private MachineUpgrades() {
    }

    public static UpgradeItem.UpgradeType typeInSlot(ItemStackHandler handler, int slot) {
        if (slot < 0 || slot >= handler.getSlots()) return null;
        ItemStack stack = handler.getStackInSlot(slot);
        if (stack.getItem() instanceof UpgradeItem upgrade) {
            return upgrade.getType();
        }
        return null;
    }

    public static boolean has(ItemStackHandler handler, int slot, UpgradeItem.UpgradeType type) {
        return typeInSlot(handler, slot) == type;
    }

    /** Progress increment per tick: 1 normally, 2 with SPEED. */
    public static int progressPerTick(ItemStackHandler handler, int upgradeSlot) {
        return has(handler, upgradeSlot, UpgradeItem.UpgradeType.SPEED) ? 2 : 1;
    }

    /** FE cost multiplier: 0.5 with EFFICIENCY, else 1.0. */
    public static float energyCostFactor(ItemStackHandler handler, int upgradeSlot) {
        return has(handler, upgradeSlot, UpgradeItem.UpgradeType.EFFICIENCY) ? 0.5f : 1.0f;
    }

    public static int energyCost(ItemStackHandler handler, int upgradeSlot, int baseCost) {
        return Math.max(1, Math.round(baseCost * energyCostFactor(handler, upgradeSlot)));
    }

    public static boolean doubleOutput(ItemStackHandler handler, int upgradeSlot) {
        return has(handler, upgradeSlot, UpgradeItem.UpgradeType.DOUBLE_HOOK);
    }
}
