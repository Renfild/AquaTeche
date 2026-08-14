package com.casesmod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Forwarder to AquaWebScreen web server menu.
 */
public class MainMenuScreen extends Screen {

    public MainMenuScreen() {
        super(Component.literal("AquaTech"));
    }

    @Override
    protected void init() {
        WebOverlay.openMainMenu(minecraft);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Forwarded to web overlay
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
