package com.casesmod.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * High-Tech Liquid Glass card button with crisp 1px borders, smooth lift animation, and glowing hover states.
 */
public class CustomButton extends Button {
    private static final int APPEAR_DURATION_MS = 220;

    private final int accentColor;
    private final long createdAt = System.currentTimeMillis();
    private final int appearDelayMs;
    private final String iconType;
    private float hoverAnim = 0f;
    private boolean lastHovered = false;

    public CustomButton(int x, int y, int w, int h, Component text, int baseColor, int accentColor, OnPress onPress) {
        this(x, y, w, h, text, accentColor, onPress, 0, "");
    }

    public CustomButton(int x, int y, int w, int h, Component text, int accentColor, OnPress onPress, int appearDelayMs) {
        this(x, y, w, h, text, accentColor, onPress, appearDelayMs, "");
    }

    public CustomButton(int x, int y, int w, int h, Component text, int accentColor, OnPress onPress, int appearDelayMs, String iconType) {
        super(x, y, w, h, text, onPress, DEFAULT_NARRATION);
        this.accentColor = accentColor;
        this.appearDelayMs = appearDelayMs;
        this.iconType = iconType != null ? iconType : "";
    }

    public CustomButton(int x, int y, int w, int h, Component text, int accentColor, OnPress onPress,
                        net.minecraft.world.item.ItemStack icon, int appearDelayMs) {
        this(x, y, w, h, text, accentColor, onPress, appearDelayMs, "");
    }

    private static float easeOutCubic(float t) {
        float p = t - 1;
        return p * p * p + 1;
    }

    @Override
    public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        long elapsed = System.currentTimeMillis() - createdAt - appearDelayMs;
        float appear = elapsed <= 0 ? 0f : Math.min(1f, elapsed / (float) APPEAR_DURATION_MS);
        if (appear <= 0f) return;
        float eased = easeOutCubic(appear);
        int yOffset = (int) ((1f - eased) * 10f);
        float alphaMul = eased;

        boolean hovered = isHovered();

        if (hovered && !lastHovered && active && alphaMul > 0.8f) {
            try {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.8f, 0.15f)
                );
            } catch (Exception ignored) {}
        }
        lastHovered = hovered;

        hoverAnim += (hovered ? 1f : -1f) * 0.22f;
        hoverAnim = Math.max(0f, Math.min(1f, hoverAnim));

        int lift = (int) (-1.5f * hoverAnim);

        gfx.pose().pushPose();
        gfx.pose().translate(0, yOffset + lift, 0);

        int fillBase = GlassUI.mix(GlassUI.GLASS_FILL, GlassUI.GLASS_FILL_HOVER, hoverAnim);
        int fillAlpha = (int) (((fillBase >> 24) & 0xFF) * alphaMul);
        int fill = (fillAlpha << 24) | (fillBase & 0xFFFFFF);

        int borderBase = GlassUI.mix(GlassUI.GLASS_BORDER, GlassUI.GLASS_BORDER_HOVER, hoverAnim);
        int borderAlpha = (int) (((borderBase >> 24) & 0xFF) * alphaMul);
        int border = (borderAlpha << 24) | (borderBase & 0xFFFFFF);

        GlassUI.drawGlassPanel(gfx, getX(), getY(), getX() + width, getY() + height, 2, fill, border,
                hovered && active && alphaMul > 0.9f);

        int textAlpha = (int) (255 * alphaMul);
        int textColorFull = (textAlpha << 24) | (hovered ? 0x00E5FF : (active ? 0xF1F5F9 : 0x64748B));

        var font = Minecraft.getInstance().font;
        Component label = getMessage();
        if (!label.getString().isEmpty()) {
            int tw = font.width(label);
            int tx = getX() + (width - tw) / 2;
            int ty = getY() + (height - 8) / 2;
            gfx.drawString(font, label, tx, ty, textColorFull, false);
        }

        gfx.pose().popPose();
    }
}

