package net.aquatech.machines.compat.jei.recipe;

import net.minecraft.world.item.ItemStack;

public record SynthesizerJeiRecipe(ItemStack inputA, ItemStack inputB, ItemStack out1, ItemStack out2, ItemStack bonus, String label) {
}
