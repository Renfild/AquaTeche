package net.aquatech.machines.compat.jei.recipe;

import net.minecraft.world.item.ItemStack;

public record ExtractorJeiRecipe(ItemStack input, ItemStack output, String label) {
}
