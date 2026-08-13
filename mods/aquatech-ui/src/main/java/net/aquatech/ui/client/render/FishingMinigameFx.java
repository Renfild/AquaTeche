package net.aquatech.ui.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aquatech.ui.AquaTechUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * StarCatcher-inspired vertical-bar fishing minigame renderer.
 */
public final class FishingMinigameFx {
    private static final String BASE = "textures/gui/minigame/fishing/";

    public static final ResourceLocation TEX_TANK = tex("tank_surface.png");
    public static final ResourceLocation TEX_BAR_OUT = tex("bar_outline.png");
    public static final ResourceLocation TEX_BAR_FILL = tex("bar_fill.png");
    public static final ResourceLocation TEX_TREASURE = tex("treasure_bar.png");
    public static final ResourceLocation TEX_POINTER = tex("pointer.png");
    public static final ResourceLocation TEX_ROD = tex("rod.png");
    public static final ResourceLocation TEX_SPOT_NORMAL = tex("spots/normal.png");
    public static final ResourceLocation TEX_SPOT_TREASURE = tex("spots/treasure.png");
    public static final ResourceLocation TEX_KEY_RMB = tex("keys/rmb.png");
    public static final ResourceLocation TEX_KEY_RMB_HOT = tex("keys/rmb_hot.png");
    public static final ResourceLocation TEX_KEY_A = tex("keys/a.png");
    public static final ResourceLocation TEX_KEY_A_HOT = tex("keys/a_hot.png");
    public static final ResourceLocation TEX_KEY_D = tex("keys/d.png");
    public static final ResourceLocation TEX_KEY_D_HOT = tex("keys/d_hot.png");

    public static final ResourceLocation[] SPARKS = new ResourceLocation[6];
    static {
        for (int i = 0; i < 6; i++) {
            SPARKS[i] = tex("spark_f" + i + ".png");
        }
    }

    public static final int PX = 2;
    public static final int TANK_W = 64;
    public static final int TANK_H = 192;
    public static final int BAR_W = 16;
    public static final int BAR_H = 100;

    public static final int SAFE = 0xFF6EB86E;
    public static final int GOLD = 0xFFE8B040;
    public static final int DANGER = 0xFFC85048;
    public static final int WATER = 0xFF5AB0C8;
    public static final int INK = 0xFF3A2818;
    public static final int CREAM = 0xFFF0E6C8;

    private FishingMinigameFx() {
    }

    private static ResourceLocation tex(String file) {
        return new ResourceLocation(AquaTechUI.MOD_ID, BASE + file);
    }

    public static int gui(int logical) {
        return logical * PX;
    }

    public static float time(float partialTick) {
        return (System.currentTimeMillis() % 1_000_000L) / 1000.0F + partialTick * 0.05F;
    }

    public static int withAlpha(int rgb, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    public static void dimScreen(GuiGraphics g, int sw, int sh, float intensity) {
        int a = Mth.clamp((int) (intensity * 100f), 15, 100);
        g.fill(0, 0, sw, sh, (a << 24) | 0x182028);
    }

    private static void blit(GuiGraphics g, ResourceLocation tex, int x, int y,
                             int srcW, int srcH, int dstW, int dstH) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.blit(tex, x, y, dstW, dstH, 0, 0, srcW, srcH, srcW, srcH);
    }

    public static void drawMinigame(GuiGraphics g, int ox, int oy, float scale,
                                    float progress, float progressSmooth, int fishHp,
                                    float pointerPos, float pointerPrev, float partialTick,
                                    float spotCenter, float spotSize, float yellowPad,
                                    boolean treasureActive, int treasurePct,
                                    boolean inSpot, boolean holding, float kimbeAlpha,
                                    float kimbePos, List<HitParticle> particles,
                                    float animT, boolean grace) {
        int tankW = Math.round(gui(TANK_W) * scale);
        int tankH = Math.round(gui(TANK_H) * scale);
        int barW = Math.round(gui(BAR_W) * scale);
        int barH = Math.round(gui(BAR_H) * scale);

        int tankX = ox - tankW / 2;
        int tankY = oy - tankH / 2;

        blit(g, TEX_TANK, tankX, tankY, TANK_W * PX, TANK_H * PX, tankW, tankH);

        // Rod decoration
        int rodW = Math.round(gui(48) * scale);
        int rodH = Math.round(gui(64) * scale);
        blit(g, TEX_ROD, tankX + tankW - rodW / 2, tankY - rodH / 4, 96, 128, rodW, rodH);

        int barX = tankX + tankW / 2 - barW / 2 + Math.round(gui(8) * scale);
        int barY = tankY + Math.round(gui(32) * scale);
        blit(g, TEX_BAR_OUT, barX, barY, BAR_W * PX, BAR_H * PX, barW, barH);

        // Catch progress fill (top = full tension)
        float pct = Mth.clamp(progressSmooth / Math.max(1f, fishHp), 0f, 1.2f);
        int fillH = Math.round(barH * pct);
        if (fillH > 0) {
            int fillY = barY + barH - fillH;
            int fillInnerW = barW - Math.round(gui(4) * scale);
            int fillInnerX = barX + (barW - fillInnerW) / 2;
            blit(g, TEX_BAR_FILL, fillInnerX, fillY, 24, 192,
                    fillInnerW, fillH);
        }

        // Sweet spot zone
        float spotHalf = spotSize * 0.5f + yellowPad;
        float spotTop = spotCenter - spotHalf;
        float spotBot = spotCenter + spotHalf;
        int spotTexH = Math.round(Math.max(gui(8), (spotBot - spotTop) / 100f * barH));
        int spotTexY = barY + Math.round((100f - spotBot) / 100f * barH);
        int spotTexW = Math.round(gui(12) * scale);
        int spotTexX = barX + (barW - spotTexW) / 2;
        ResourceLocation spotTex = treasureActive ? TEX_SPOT_TREASURE : TEX_SPOT_NORMAL;
        RenderSystem.setShaderColor(1f, 1f, 1f, grace ? 0.45f : 0.88f);
        blit(g, spotTex, spotTexX, spotTexY, 32, 96, spotTexW, spotTexH);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Kimbe marker flash
        if (kimbeAlpha > 0.02f && !grace) {
            int ky = barY + Math.round((100f - kimbePos) / 100f * barH);
            int ka = Mth.clamp((int) (kimbeAlpha * 180), 0, 255);
            g.fill(barX - gui(2), ky - gui(1), barX + barW + gui(2), ky + gui(2),
                    (ka << 24) | (SAFE & 0x00FFFFFF));
        }

        // Pointer
        float drawPtr = Mth.lerp(partialTick, pointerPrev, pointerPos);
        int ptrY = barY + Math.round((100f - drawPtr) / 100f * barH) - Math.round(gui(8) * scale);
        int ptrW = Math.round(gui(32) * scale);
        int ptrH = Math.round(gui(16) * scale);
        blit(g, TEX_POINTER, barX + barW / 2 - ptrW / 2, ptrY, 64, 32, ptrW, ptrH);

        // Treasure bar
        if (treasureActive) {
            int trW = Math.round(gui(8) * scale);
            int trH = barH;
            int trX = barX - trW - Math.round(gui(6) * scale);
            blit(g, TEX_TREASURE, trX, barY, 16, 200, trW, trH);
            int tFill = Math.round(trH * treasurePct / 100f);
            if (tFill > 0) {
                g.fill(trX + 2, barY + trH - tFill, trX + trW - 2, barY + trH,
                        withAlpha(GOLD, 0.85f));
            }
        }

        // Hit particles
        for (HitParticle p : particles) {
            float a = p.alpha();
            int fi = (p.frame + (int) (animT * 20)) % 6;
            RenderSystem.setShaderColor(1f, 1f, 1f, a);
            int ps = Math.round(gui(8) * scale);
            blit(g, SPARKS[fi], (int) p.x - ps / 2, (int) p.y - ps / 2, 32, 32, ps, ps);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Key hints
        int keyY = barY + barH + Math.round(gui(10) * scale);
        int keyW = Math.round(gui(24) * scale);
        int keyH = Math.round(gui(16) * scale);
        int keyGap = Math.round(gui(6) * scale);
        int keysX = barX + barW / 2 - (keyW * 3 + keyGap * 2) / 2;
        boolean hot = holding && inSpot && !grace;
        blit(g, hot ? TEX_KEY_A_HOT : TEX_KEY_A, keysX, keyY, 48, 32, keyW, keyH);
        blit(g, hot ? TEX_KEY_RMB_HOT : TEX_KEY_RMB, keysX + keyW + keyGap, keyY, 48, 32, keyW, keyH);
        blit(g, hot ? TEX_KEY_D_HOT : TEX_KEY_D, keysX + (keyW + keyGap) * 2, keyY, 48, 32, keyW, keyH);
    }

    public static void drawHint(GuiGraphics g, Font font, int cx, int y, String text, int color) {
        g.drawCenteredString(font, text, cx, y, color);
    }
}
