package net.aquatech.machines.client.gui;

import net.aquatech.machines.inventory.ExcavatorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ExcavatorScreen extends AbstractMachineScreen<ExcavatorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("aquatech_machines", "textures/gui/excavator.png");

    public ExcavatorScreen(ExcavatorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, TEXTURE, 52, 35, 8, 18);
    }
}
