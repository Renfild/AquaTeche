package net.aquatech.machines.compat.jei.recipe;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record FisherJeiRecipe(ItemStack rod, int tier, boolean hasCore, List<ItemStack> sampleDrops, String modeLabel) {
}
