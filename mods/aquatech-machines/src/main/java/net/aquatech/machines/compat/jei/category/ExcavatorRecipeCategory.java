package net.aquatech.machines.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin;
import net.aquatech.machines.compat.jei.recipe.ExcavatorJeiRecipe;
import net.aquatech.machines.registry.ModBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class ExcavatorRecipeCategory extends AbstractRecipeCategory<ExcavatorJeiRecipe> {

    private final IDrawable background;
    private final IDrawableStatic slotDrawable;

    public ExcavatorRecipeCategory(IGuiHelper helper) {
        super(AquaTechMachinesJeiPlugin.EXCAVATOR_TYPE,
                Component.literal("Экскаватор"),
                helper.createDrawableItemStack(new ItemStack(ModBlocks.EXCAVATOR.get())),
                166, 36);
        this.background = helper.createBlankDrawable(166, 36);
        this.slotDrawable = helper.getSlotDrawable();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ExcavatorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 75, 9)
                .addItemStack(recipe.output())
                .addTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.literal("§e" + recipe.label()));
                    tooltip.add(Component.literal(String.format(Locale.ROOT, "§bШанс: §a%.1f%%  §7(%s)", recipe.chancePercent(), recipe.rangeText())));
                });
    }

    @Override
    public void draw(ExcavatorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        slotDrawable.draw(g, 74, 8);
    }
}
