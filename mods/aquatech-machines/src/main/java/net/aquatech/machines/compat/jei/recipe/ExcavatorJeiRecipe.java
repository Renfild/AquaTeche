package net.aquatech.machines.compat.jei.recipe;

import net.minecraft.world.item.ItemStack;

public record ExcavatorJeiRecipe(ItemStack output, int weight, double chancePercent, String label, String rangeText) {
}
