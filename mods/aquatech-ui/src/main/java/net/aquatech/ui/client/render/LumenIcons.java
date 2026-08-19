package net.aquatech.ui.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Vector icon set rendering from anti-aliased texture atlas.
 */
public final class LumenIcons {

    private static final ResourceLocation ATLAS_AQUATECH =
            new ResourceLocation("aquatech_ui", "textures/gui/icons.png");
    private static final ResourceLocation ATLAS_AQUALUMEN =
            new ResourceLocation("aqualumen", "textures/gui/icons.png");

    private static final int CELL = 64;
    private static final int COLUMNS = 7;
    private static final int ROWS = 4;
    private static final int ATLAS_WIDTH = CELL * COLUMNS;
    private static final int ATLAS_HEIGHT = CELL * ROWS;

    public enum Icon {
        PLAYER, BAG, CASE, STAR, CHART, GEAR, COIN,
        GEM, KEY, FISH, WAVE, CLOCK, LOCK, CHECK,
        ARROW, BELL, HEART, BOLT, SHIELD, CLOSE, REFRESH,
        PANEL, LAYERS;

        int u() {
            return (ordinal() % COLUMNS) * CELL;
        }

        int v() {
            return (ordinal() / COLUMNS) * CELL;
        }
    }

    private LumenIcons() {
    }

    private static void smooth(ResourceLocation atlas) {
        try {
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(atlas);
            if (texture != null) {
                texture.setFilter(true, false);
            }
        } catch (RuntimeException ignored) {
        }
    }

    public static void draw(GuiGraphics graphics, Icon icon, float x, float y, float size, int color) {
        int side = Math.max(8, Math.round(size));
        float alpha = ((color >>> 24) & 0xFF) / 255.0F;
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        ResourceLocation atlas = ATLAS_AQUATECH;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        smooth(atlas);
        RenderSystem.setShaderColor(red, green, blue, alpha);
        graphics.blit(atlas, Math.round(x), Math.round(y), side, side,
                icon.u(), icon.v(), CELL, CELL, ATLAS_WIDTH, ATLAS_HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public static void drawCentered(GuiGraphics graphics, Icon icon, float centerX, float centerY, float size, int color) {
        float side = Math.max(8, size);
        draw(graphics, icon, centerX - side / 2.0F, centerY - side / 2.0F, side, color);
    }
}
