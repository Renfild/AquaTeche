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
import net.aquatech.machines.compat.jei.recipe.FisherJeiRecipe;
import net.aquatech.machines.registry.ModBlocks;
import net.aquatech.machines.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FisherRecipeCategory extends AbstractRecipeCategory<FisherJeiRecipe> {

    private final IDrawable background;
    private final IDrawableStatic slotDrawable;
    private final IDrawableStatic arrow;

    public FisherRecipeCategory(IGuiHelper helper) {
        super(AquaTechMachinesJeiPlugin.FISHER_TYPE,
                Component.literal("Авторыболов MK-2"),
                helper.createDrawableItemStack(new ItemStack(ModBlocks.FISHER.get())),
                176, 54);
        this.background = helper.createBlankDrawable(176, 54);
        this.slotDrawable = helper.getSlotDrawable();
        this.arrow = helper.getRecipeArrow();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FisherJeiRecipe recipe, IFocusGroup focuses) {
        if (recipe.hasCore()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 6, 6).addItemStack(recipe.rod());
            builder.addSlot(RecipeIngredientRole.CATALYST, 6, 30).addItemStack(new ItemStack(ModItems.FISHING_CORE.get()));
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, 6, 18).addItemStack(recipe.rod());
        }

        int count = Math.min(5, recipe.sampleDrops().size());
        for (int i = 0; i < count; i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 57 + i * 20, 18).addItemStack(recipe.sampleDrops().get(i));
        }
    }

    @Override
    public void draw(FisherJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        if (recipe.hasCore()) {
            slotDrawable.draw(g, 5, 5);
            slotDrawable.draw(g, 5, 29);
        } else {
            slotDrawable.draw(g, 5, 17);
        }

        arrow.draw(g, 28, 17);

        int count = Math.min(5, recipe.sampleDrops().size());
        for (int i = 0; i < count; i++) {
            slotDrawable.draw(g, 56 + i * 20, 17);
        }
    }
}
