package net.aquatech.ui.client.tab;

import net.aquatech.ui.client.gui.AquaBlurredScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class OceanTabScreen extends AquaBlurredScreen {

    public OceanTabScreen() {
        super(Component.literal("AquaTech Tab"));
        setEnableAtmosphericParticles(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderScreenContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        OceanTabOverlay.render(graphics, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        OceanTabOverlay.scroll(delta);
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null && this.minecraft.options != null && this.minecraft.options.keyPlayerList.matches(keyCode, scanCode)) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
