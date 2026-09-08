package net.aquatech.ui.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class AquaTechJeiPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_UID = new ResourceLocation("aquatech_ui", "jei_plugin");
    private static volatile IJeiRuntime jeiRuntime;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    @Override
    public void onRuntimeUnavailable() {
        jeiRuntime = null;
    }

    public static boolean isAvailable() {
        return jeiRuntime != null;
    }

    public static boolean showRecipes(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        IJeiRuntime runtime = jeiRuntime;
        if (runtime != null) {
            try {
                var focusFactory = runtime.getJeiHelpers().getFocusFactory();
                java.util.List<IFocus<?>> focuses = java.util.List.of(
                        focusFactory.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack),
                        focusFactory.createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, stack)
                );
                runtime.getRecipesGui().show(focuses);
                return true;
            } catch (Throwable t) {
                try {
                    String name = stack.getHoverName().getString();
                    runtime.getIngredientFilter().setFilterText(name);
                    return true;
                } catch (Throwable ignored) {
                }
            }
        }
        return false;
    }
}
