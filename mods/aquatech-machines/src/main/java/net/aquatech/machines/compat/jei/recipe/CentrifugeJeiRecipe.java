package net.aquatech.machines.compat.jei.recipe;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public record CentrifugeJeiRecipe(ItemStack rawWater, ItemStack distillate, List<ItemStack> minerals, String label) {
}
