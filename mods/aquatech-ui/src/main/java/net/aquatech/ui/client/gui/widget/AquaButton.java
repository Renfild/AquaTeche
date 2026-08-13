package net.aquatech.ui.client.gui.widget;

import net.aquatech.ui.client.render.AquaFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class AquaButton extends AbstractButton {

    private final Runnable onPressAction;
    private final String iconSymbol;
    private final Component subtitleText;
    private final int accentColor;

    private boolean lastHovered = false;

    public AquaButton(int x, int y, int width, int height, Component title, Component subtitleText, String iconSymbol, int accentColor, Runnable onPressAction) {
        super(x, y, width, height, title);
        this.subtitleText = subtitleText;
        this.iconSymbol = iconSymbol;
        this.accentColor = accentColor;
        this.onPressAction = onPressAction;
    }

    public AquaButton(int x, int y, int width, int height, Component title, Runnable onPressAction) {
        this(x, y, width, height, title, null, null, 0xFF00E5FF, onPressAction);
    }

    @Override
    public void onPress() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.5f, 0.25f)
        );
        if (onPressAction != null) {
            onPressAction.run();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHoveredOrFocused();
        if (hovered && !lastHovered) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.75f, 0.15f)
            );
        }
        lastHovered = hovered;

        int fill = hovered ? 0xFF0F2E4A : 0xEE0D2136;
        int border = hovered ? accentColor : 0xFF1E293B;
        AquaGlassPanel.draw(g, this.getX(), this.getY(), this.width, this.height, fill, border, 3, hovered);

        Font font = Minecraft.getInstance().font;
        int cy = this.getY() + this.height / 2;
        int textLeft = this.getX() + 12;
        if (iconSymbol != null && !iconSymbol.isEmpty()) {
            AquaFontRenderer.draw(g, font, iconSymbol, this.getX() + 10, cy - 4, accentColor);
            textLeft += 14;
        }

        int titleY = (subtitleText != null) ? this.getY() + 6 : cy - 4;
        int textColor = hovered ? accentColor : 0xFFFFFFFF;
        Component title = AquaFontRenderer.withMain(getMessage());
        if (hovered) {
            AquaFontRenderer.drawGlowText(g, font, title, textLeft, titleY, textColor, (0x44 << 24) | (accentColor & 0xFFFFFF));
        } else {
            g.drawString(font, title, textLeft, titleY, textColor, false);
        }

        if (subtitleText != null) {
            g.drawString(font, AquaFontRenderer.withMain(subtitleText), textLeft, this.getY() + 18, 0xFF94A3B8, false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
