package com.casesmod.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Deep-ocean backdrop: smooth multi-stop gradients, drifting teal wash, soft vignette.
 * No stripe bands or hard seams.
 */
public final class OceanParallax {

    private OceanParallax() {
    }

    public static void render(GuiGraphics gfx, int width, int height, float timeSec, float partialTicks) {
        gfx.fillGradient(0, 0, width, height, 0xFF050A14, 0xFF0C1A2E);

        int midY = height * 2 / 3;
        gfx.fillGradient(0, height / 4, width, midY, 0x00000000, 0x142A7088);
        gfx.fillGradient(0, midY, width, height, 0x08183045, 0x183A90C0);

        float pulse = 0.5f + 0.5f * Mth.sin(timeSec * 0.35f);
        int washA = (int) (14 + 10 * pulse);
        gfx.fill(0, 0, width, height, (washA << 24) | 0x2A8090);

        GlassUI.drawSoftVignette(gfx, width, height, 0x28);
    }
}
