package com.casesmod.client.gui.widget;

import com.casesmod.CasesMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Кастомные GUI-текстуры мода (сгенерированы процедурно: свечение, блёстки, фоновый грейн). */
public class ModTextures {
    public static final ResourceLocation GLOW_BURST = new ResourceLocation(CasesMod.MOD_ID, "textures/gui/glow_burst.png");
    public static final ResourceLocation SPARKLE = new ResourceLocation(CasesMod.MOD_ID, "textures/gui/sparkle.png");
    public static final ResourceLocation NOISE_OVERLAY = new ResourceLocation(CasesMod.MOD_ID, "textures/gui/noise_overlay.png");
    public static final ResourceLocation FISH_MARKET = new ResourceLocation(CasesMod.MOD_ID, "textures/gui/fish_market.png");

    private static final int GLOW_BURST_SIZE = 128;
    private static final int SPARKLE_SIZE = 32;
    private static final int NOISE_SIZE = 128;

    /** Растягивает квадратную текстуру на произвольный размер экрана (не 1:1 с файлом). */
    public static void blitStretched(GuiGraphics gfx, ResourceLocation tex, int x, int y, int w, int h, int texFileSize) {
        gfx.blit(tex, x, y, w, h, 0f, 0f, texFileSize, texFileSize, texFileSize, texFileSize);
    }

    public static void blitGlowBurst(GuiGraphics gfx, int centerX, int centerY, int radius, int argbTint) {
        tintedBlit(gfx, GLOW_BURST, centerX - radius, centerY - radius, radius * 2, radius * 2, GLOW_BURST_SIZE, argbTint);
    }

    public static void blitSparkle(GuiGraphics gfx, int centerX, int centerY, int size, int argbTint) {
        tintedBlit(gfx, SPARKLE, centerX - size / 2, centerY - size / 2, size, size, SPARKLE_SIZE, argbTint);
    }

    /** Тайлит шумовую текстуру по всей заданной области — тонкий "материальный" грейн поверх фона. */
    public static void blitNoiseOverlayTiled(GuiGraphics gfx, int x1, int y1, int x2, int y2) {
        for (int x = x1; x < x2; x += NOISE_SIZE) {
            for (int y = y1; y < y2; y += NOISE_SIZE) {
                int w = Math.min(NOISE_SIZE, x2 - x);
                int h = Math.min(NOISE_SIZE, y2 - y);
                gfx.blit(NOISE_OVERLAY, x, y, w, h, 0f, 0f, w, h, NOISE_SIZE, NOISE_SIZE);
            }
        }
    }

    /** Блит с цветовым тонированием через альфа/RGB множитель шейдера (сбрасывается сразу после). */
    private static void tintedBlit(GuiGraphics gfx, ResourceLocation tex, int x, int y, int w, int h, int fileSize, int argb) {
        float a = ((argb >>> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(r, g, b, a);
        blitStretched(gfx, tex, x, y, w, h, fileSize);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
