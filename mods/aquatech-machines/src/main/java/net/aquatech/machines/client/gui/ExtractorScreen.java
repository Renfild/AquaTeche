package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.ExtractorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ExtractorScreen extends AbstractMachineScreen<ExtractorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/extractor.png");

    public ExtractorScreen(ExtractorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE);
    }

    @Override
    protected int progressU() {
        return 176;
    }

    @Override
    protected int progressV() {
        return 52;
    }
}
