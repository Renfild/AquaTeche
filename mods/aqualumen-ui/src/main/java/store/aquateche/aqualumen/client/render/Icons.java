package store.aquateche.aqualumen.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Vector icon set. Shapes are authored as smooth strokes in tools/gen_assets.py and baked into a
 * single anti-aliased atlas (7 x 3 cells, 64 px each). The atlas holds a white mask only, so every
 * icon is tinted with the current theme colour at draw time: no pixel-art staircase, no per-theme
 * copies, one texture bind for the whole interface.
 */
public final class Icons {

    private static final ResourceLocation ATLAS =
            new ResourceLocation("aqualumen", "textures/gui/icons.png");
    private static final int CELL = 64;
    private static final int COLUMNS = 7;
    private static final int ROWS = 4;
    private static final int ATLAS_WIDTH = CELL * COLUMNS;
    private static final int ATLAS_HEIGHT = CELL * ROWS;

    private static boolean smoothed;

    /** Order defines the atlas cell, so it must match the order used by the generator. */
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

    private Icons() {
    }

    /**
     * Bilinear filtering is what keeps a 64 px cell clean when it is drawn at 8-20 px. The texture
     * manager only knows the atlas after the first bind, so this runs right after the first blit.
     */
    private static void smooth() {
        if (smoothed) {
            return;
        }
        try {
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(ATLAS);
            texture.setFilter(true, false);
            smoothed = true;
        } catch (RuntimeException ignored) {
            // A missing atlas must never take the screen down; the icon just renders unfiltered.
        }
    }

    /** Draws an icon with its top left corner at x, y, tinted with an ARGB colour. */
    public static void draw(GuiGraphics graphics, Icon icon, int x, int y, int size, int color) {
        int side = Math.max(10, size);
        float alpha = ((color >>> 24) & 0xFF) / 255.0F;
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(red, green, blue, alpha);
        graphics.blit(ATLAS, x, y, side, side,
                icon.u(), icon.v(), CELL, CELL, ATLAS_WIDTH, ATLAS_HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        smooth();
    }

    public static void drawCentered(GuiGraphics graphics, Icon icon, int centerX, int centerY,
                                    int size, int color) {
        int side = Math.max(10, size);
        draw(graphics, icon, centerX - side / 2, centerY - side / 2, side, color);
    }

    /** Icon inside a rounded tile, used by offer cards and case rows. */
    public static void badge(GuiGraphics graphics, Icon icon, int x, int y, int box,
                            int background, int color) {
        Gfx.roundedRect(graphics, x, y, box, box, Math.max(3, box / 3), background);
        drawCentered(graphics, icon, x + box / 2, y + box / 2, Math.max(8, box - 6), color);
    }
}
