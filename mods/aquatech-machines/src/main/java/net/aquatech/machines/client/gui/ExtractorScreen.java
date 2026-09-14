package net.aquatech.machines.client.gui;

import net.aquatech.machines.block.entity.ExtractorBlockEntity;
import net.aquatech.machines.inventory.ExtractorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ExtractorScreen extends AbstractMachineScreen<ExtractorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/extractor.png");

    public ExtractorScreen(ExtractorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 74, 36, 8, 20);
    }

    @Override
    protected boolean openJeiRecipes() {
        return net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.showRecipes(
                net.aquatech.machines.compat.jei.AquaTechMachinesJeiPlugin.EXTRACTOR_TYPE);
    }

    @Override
    protected void onProgressBarClicked() {
        printChat("§8[§bAquaTech§8] §6=== Рецепты Экстрактора ===");
        for (ExtractorBlockEntity.RecipeEntry r : ExtractorBlockEntity.RECIPE_LIST) {
            printChat(" §7• §f" + r.label());
        }
        printChat("§8[§7Выход удвоен! Поддерживает ускорение до ×4 и энергоэффективность -75%§8]");
    }
}
