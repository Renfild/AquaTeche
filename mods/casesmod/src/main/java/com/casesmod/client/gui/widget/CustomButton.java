package com.casesmod.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Liquid-glass chip: soft fill, accent border on hover, fade+slide enter, vector icons and sounds.
 */
public class CustomButton extends Button {
    private static final int APPEAR_DURATION_MS = 260;

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
        int yOffset = (int) ((1f - eased) * 12f);
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

        hoverAnim += (hovered ? 1f : -1f) * 0.18f;
        hoverAnim = Math.max(0f, Math.min(1f, hoverAnim));

        int lift = (int) (-1.5f * hoverAnim);

        gfx.pose().pushPose();
        gfx.pose().translate(0, yOffset + lift, 0);

        int radius = Math.min(14, height / 2);
        int fillBase = GlassUI.mix(GlassUI.GLASS_FILL, GlassUI.GLASS_FILL_HOVER, hoverAnim);
        int fillAlpha = (int) (((fillBase >> 24) & 0xFF) * alphaMul);
        int fill = (fillAlpha << 24) | (fillBase & 0xFFFFFF);

        int borderBase = GlassUI.mix(GlassUI.GLASS_BORDER, GlassUI.GLASS_BORDER_HOVER, hoverAnim);
        int borderAlpha = (int) (((borderBase >> 24) & 0xFF) * alphaMul);
        int border = (borderAlpha << 24) | (accentColor & 0xFFFFFF);

        GlassUI.drawGlassPanel(gfx, getX(), getY(), getX() + width, getY() + height, radius, fill, border,
                hovered && active && alphaMul > 0.9f);

        int textAlpha = (int) (255 * alphaMul);
        int textColorFull = (textAlpha << 24) | (active ? 0xFFFFFF : 0x8A99AA);

        var font = Minecraft.getInstance().font;
        Component label = getMessage();
        if (!label.getString().isEmpty()) {
            if (iconType != null && !iconType.isEmpty()) {
                gfx.drawString(font, label, getX() + 18, getY() + (height - 8) / 2, textColorFull, false);
                int iconX = getX() + width - 24;
                int iconY = getY() + height / 2;
                drawButtonIcon(gfx, iconX, iconY, iconType, textAlpha);
            } else {
                int tw = font.width(label);
                gfx.drawString(font, label, getX() + (width - tw) / 2, getY() + (height - 8) / 2, textColorFull, false);
            }
        }

        gfx.pose().popPose();
    }

    private void drawButtonIcon(GuiGraphics gfx, int cx, int cy, String type, int alpha) {
        int color = (alpha << 24) | 0x8EE6FF;
        int dimColor = (alpha << 24) | 0x507A99;

        switch (type.toLowerCase()) {
            case "rod", "kits" -> {
                gfx.fill(cx - 5, cy + 3, cx + 3, cy - 5, color);
                gfx.fill(cx + 3, cy - 5, cx + 5, cy - 3, color);
                gfx.fill(cx + 4, cy - 3, cx + 4, cy + 2, dimColor);
                gfx.fill(cx + 3, cy + 2, cx + 5, cy + 4, color);
            }
            case "warp", "warps" -> {
                GlassUI.fillDisk(gfx, cx, cy, 5, color);
                GlassUI.fillDisk(gfx, cx, cy, 3, (alpha << 24) | 0x101D2A);
                GlassUI.fillDisk(gfx, cx, cy, 1, color);
            }
            case "spawn" -> {
                GlassUI.fillDisk(gfx, cx, cy, 5, dimColor);
                GlassUI.fillDisk(gfx, cx, cy, 4, (alpha << 24) | 0x101D2A);
                gfx.fill(cx - 2, cy - 2, cx + 3, cy + 3, color);
            }
            case "donate" -> {
                gfx.fill(cx - 4, cy - 2, cx + 5, cy + 1, color);
                gfx.fill(cx - 2, cy + 1, cx + 3, cy + 3, color);
                gfx.fill(cx - 1, cy + 3, cx + 2, cy + 5, color);
            }
            case "quest", "quests" -> {
                gfx.fill(cx - 4, cy - 4, cx + 5, cy + 5, dimColor);
                gfx.fill(cx - 3, cy - 3, cx + 4, cy + 4, (alpha << 24) | 0x101D2A);
                gfx.fill(cx - 2, cy - 1, cx + 3, cy, color);
                gfx.fill(cx - 2, cy + 1, cx + 2, cy + 2, color);
            }
            case "case", "cases" -> {
                gfx.fill(cx - 5, cy - 3, cx + 6, cy + 4, dimColor);
                gfx.fill(cx - 4, cy - 2, cx + 5, cy + 3, color);
                gfx.fill(cx - 1, cy - 1, cx + 2, cy + 1, (alpha << 24) | 0xFFD700);
            }
            default -> {}
        }
    }
}

