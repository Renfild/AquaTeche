package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.FisherMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FisherScreen extends AbstractMachineScreen<FisherMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/fisher.png");

    public FisherScreen(FisherMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 79, 34, 8, 18);
    }
}
