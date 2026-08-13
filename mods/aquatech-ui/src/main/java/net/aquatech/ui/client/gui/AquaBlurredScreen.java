package net.aquatech.ui.client.gui;

import net.aquatech.ui.client.render.UiDraw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * LoliLand {@code IBlurredGui} analogue for Forge 1.20.1.
 * World gaussian blur is applied by {@link net.aquatech.ui.client.render.OceanBlurEngine}
 * before this screen draws. Subclasses only paint glass widgets on top.
 */
public abstract class AquaBlurredScreen extends Screen {

    /** Translucent navy wash — blur of the world must remain visible. */
    protected static final int COLOR_BG_VIGNETTE = 0x66050A14;
    protected static final int COLOR_GLASS_PANEL = 0x99101828;
    protected static final int COLOR_GLASS_CARD = 0xAA0D2136;
    protected static final int COLOR_CYAN_ACCENT = 0xFF5CE1FF;
    protected static final int COLOR_BORDER_MUTED = 0x665CE1FF;
    protected static final int COLOR_TEXT_MUTED = 0xB394A3B8;

    protected final List<BubbleParticle> bubbles = new ArrayList<>();
    protected final Random random = new Random();
    protected final long openedAtMs = System.currentTimeMillis();

    private boolean enableAtmosphericParticles = true;

    protected static class BubbleParticle {
        public float x, y, speed, size;
        public int alpha;
    }

    protected AquaBlurredScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        if (enableAtmosphericParticles) {
            bubbles.clear();
            for (int i = 0; i < 22; i++) {
                BubbleParticle p = new BubbleParticle();
                p.x = random.nextFloat() * width;
                p.y = random.nextFloat() * height;
                p.speed = 0.12f + random.nextFloat() * 0.28f;
                p.size = 1.0f + random.nextFloat() * 2.0f;
                p.alpha = 20 + random.nextInt(70);
                bubbles.add(p);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!enableAtmosphericParticles) {
            return;
        }
        for (BubbleParticle p : bubbles) {
            p.y -= p.speed;
            if (p.y < 0) {
                p.y = height;
                p.x = random.nextFloat() * width;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderAtmosphere(g);
        renderScreenContent(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
    }

    protected void renderAtmosphere(GuiGraphics g) {
        g.fill(0, 0, width, height, COLOR_BG_VIGNETTE);
        if (!enableAtmosphericParticles) {
            return;
        }
        for (BubbleParticle p : bubbles) {
            int color = (p.alpha << 24) | 0x5CE1FF;
            g.fill((int) p.x, (int) p.y, (int) (p.x + p.size), (int) (p.y + p.size), color);
        }
    }

    protected abstract void renderScreenContent(GuiGraphics g, int mouseX, int mouseY, float partialTick);

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void drawGlassContainer(GuiGraphics g, int x, int y, int w, int h, int fillARGB, int borderARGB) {
        g.fill(x, y, x + w, y + h, fillARGB);
        UiDraw.border(g, x, y, w, h, borderARGB);
    }

    protected void drawHeaderTitle(GuiGraphics g, String titleText, String subtitleText, int x, int y, int w) {
        net.aquatech.ui.client.render.AquaFontRenderer.drawCenteredHeader(g, font, titleText, x + w / 2, y, COLOR_CYAN_ACCENT);
        if (subtitleText != null && !subtitleText.isEmpty()) {
            net.aquatech.ui.client.render.AquaFontRenderer.drawCentered(g, font, subtitleText, x + w / 2, y + 14, COLOR_TEXT_MUTED);
        }
    }

    protected void setEnableAtmosphericParticles(boolean enable) {
        this.enableAtmosphericParticles = enable;
    }
}
