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
import net.aquatech.machines.compat.jei.recipe.SynthesizerJeiRecipe;
import net.aquatech.machines.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SynthesizerRecipeCategory extends AbstractRecipeCategory<SynthesizerJeiRecipe> {

    private final IDrawable background;
    private final IDrawableStatic slotDrawable;
    private final IDrawableStatic outputSlotDrawable;
    private final IDrawableAnimated arrow;

    public SynthesizerRecipeCategory(IGuiHelper helper) {
        super(AquaTechMachinesJeiPlugin.SYNTHESIZER_TYPE,
                Component.literal("Гидротермальный Синтезатор"),
                helper.createDrawableItemStack(new ItemStack(ModBlocks.SYNTHESIZER.get())),
                150, 46);
        this.background = helper.createBlankDrawable(150, 46);
        this.slotDrawable = helper.getSlotDrawable();
        this.outputSlotDrawable = helper.getOutputSlot();
        this.arrow = helper.createAnimatedRecipeArrow(50);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SynthesizerJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 4).addItemStack(recipe.inputA());
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 26).addItemStack(recipe.inputB());
        builder.addSlot(RecipeIngredientRole.CATALYST, 26, 15).addItemStack(new ItemStack(Items.LAVA_BUCKET));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 4).addItemStack(recipe.out1());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 26).addItemStack(recipe.out2());
        if (recipe.bonus() != null && !recipe.bonus().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 15).addItemStack(recipe.bonus());
        }
    }

    @Override
    public void draw(SynthesizerJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        slotDrawable.draw(g, 4, 3);
        slotDrawable.draw(g, 4, 25);
        slotDrawable.draw(g, 25, 14);

        slotDrawable.draw(g, 79, 3);
        slotDrawable.draw(g, 79, 25);
        if (recipe.bonus() != null && !recipe.bonus().isEmpty()) {
            outputSlotDrawable.draw(g, 104, 11);
        }

        arrow.draw(g, 48, 15);
    }
}
