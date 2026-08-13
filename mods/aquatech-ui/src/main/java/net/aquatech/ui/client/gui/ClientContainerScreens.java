package net.aquatech.ui.client.gui;

import net.aquatech.ui.network.packet.C2SOpenContainerPacket;
import net.minecraft.client.Minecraft;

public final class ClientContainerScreens {

    private ClientContainerScreens() {
    }

    public static void open(C2SOpenContainerPacket.ContainerType type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || type == null) {
            return;
        }
        switch (type) {
            case BLOCK_LIMITERS -> mc.setScreen(new IslandLimiterScreen());
            case PERSONALIZATION -> mc.setScreen(new PersonalizationScreen());
            case STORAGE_VAULT -> {
                // Server opens vanilla ChestMenu for the ender chest.
            }
        }
    }
}
