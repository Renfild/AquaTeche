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
import net.aquatech.machines.compat.jei.recipe.CentrifugeJeiRecipe;
import net.aquatech.machines.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CentrifugeRecipeCategory extends AbstractRecipeCategory<CentrifugeJeiRecipe> {

    private final IDrawable background;
    private final IDrawableStatic slotDrawable;
    private final IDrawableAnimated arrow;

    public CentrifugeRecipeCategory(IGuiHelper helper) {
        super(AquaTechMachinesJeiPlugin.CENTRIFUGE_TYPE,
                Component.literal("Батиметрическая Центрифуга"),
                helper.createDrawableItemStack(new ItemStack(ModBlocks.CENTRIFUGE.get())),
                140, 46);
        this.background = helper.createBlankDrawable(140, 46);
        this.slotDrawable = helper.getSlotDrawable();
        this.arrow = helper.createAnimatedRecipeArrow(40);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 15).addItemStack(recipe.rawWater());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 15).addItemStack(recipe.distillate());

        int[] xs = {85, 105, 85, 105};
        int[] ys = {5, 5, 25, 25};
        for (int i = 0; i < Math.min(4, recipe.minerals().size()); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, xs[i], ys[i]).addItemStack(recipe.minerals().get(i));
        }
    }

    @Override
    public void draw(CentrifugeJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        slotDrawable.draw(g, 4, 14);
        slotDrawable.draw(g, 54, 14);

        slotDrawable.draw(g, 84, 4);
        slotDrawable.draw(g, 104, 4);
        slotDrawable.draw(g, 84, 24);
        slotDrawable.draw(g, 104, 24);

        arrow.draw(g, 26, 15);
    }
}
