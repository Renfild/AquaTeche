package net.aquatech.machines.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin;
import net.aquatech.machines.compat.jei.recipe.ExtractorJeiRecipe;
import net.aquatech.machines.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ExtractorRecipeCategory extends AbstractRecipeCategory<ExtractorJeiRecipe> {

    private final IDrawable background;
    private final IDrawableStatic slotDrawable;
    private final IDrawableStatic outputSlotDrawable;
    private final IDrawableAnimated arrow;

    public ExtractorRecipeCategory(IGuiHelper helper) {
        super(AquaTechMachinesJeiPlugin.EXTRACTOR_TYPE,
                Component.literal("Экстрактор"),
                helper.createDrawableItemStack(new ItemStack(ModBlocks.EXTRACTOR.get())),
                120, 36);
        this.background = helper.createBlankDrawable(120, 36);
        this.slotDrawable = helper.getSlotDrawable();
        this.outputSlotDrawable = helper.getOutputSlot();
        this.arrow = helper.createAnimatedRecipeArrow(40);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ExtractorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 10).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 6).addItemStack(recipe.output());
    }

    @Override
    public void draw(ExtractorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        slotDrawable.draw(g, 4, 9);
        outputSlotDrawable.draw(g, 87, 2);
        arrow.draw(g, 42, 10);
    }
}
