package com.casesmod.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Clickable region over a PNG GUI — draws nothing (or a faint hover outline)
 * so the texture provides all visuals.
 */
public class GuiHotspot extends AbstractButton {
    private final OnPress onPress;
    private final boolean showHoverOutline;

    public GuiHotspot(int x, int y, int w, int h, Component narration, OnPress onPress) {
        this(x, y, w, h, narration, onPress, true);
    }

    public GuiHotspot(int x, int y, int w, int h, Component narration, OnPress onPress, boolean showHoverOutline) {
        super(x, y, w, h, narration);
        this.onPress = onPress;
        this.showHoverOutline = showHoverOutline;
    }

    @Override
    public void onPress() {
        onPress.onPress(this);
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        if (showHoverOutline && isHoveredOrFocused()) {
            gfx.fill(getX(), getY(), getX() + width, getY() + height, 0x33FFFFFF);
            gfx.renderOutline(getX(), getY(), width, height, 0xAA5CE1FF);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @FunctionalInterface
    public interface OnPress {
        void onPress(GuiHotspot button);
    }
}
