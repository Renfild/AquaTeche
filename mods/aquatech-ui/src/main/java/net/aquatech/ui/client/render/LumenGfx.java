package net.aquatech.ui.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/**
 * GPU-accelerated triangle mesh renderer for AquaLumen design system.
 * Renders anti-aliased rounded rectangles, borders, outer glows, gradients, and progress bars.
 */
public final class LumenGfx {

    private LumenGfx() {
    }

    public static void rect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        roundedRect(graphics, x, y, width, height, 0, color);
    }

    public static void roundedRect(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
        gradientRounded(graphics, x, y, width, height, radius, color, color);
    }

    public static void gradientRounded(GuiGraphics graphics, float x, float y, float width, float height, float radius,
                                       int topColor, int bottomColor) {
        meshRounded(graphics, x, y, width, height, radius, topColor, bottomColor, true);
    }

    public static void gradientRoundedH(GuiGraphics graphics, float x, float y, float width, float height, float radius,
                                        int leftColor, int rightColor) {
        meshRounded(graphics, x, y, width, height, radius, leftColor, rightColor, false);
    }

    public static void outline(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
        if (width < 2 || height < 2) {
            return;
        }
        graphics.flush();
        float r = clampRadius(radius, width, height);
        float stroke = 1.0F;
        beginColor();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = graphics.pose().last().pose();
        int segments = arcSegments(r);
        float[] outer = perimeter(x, y, width, height, r, segments);
        float innerR = Math.max(0.0F, r - stroke);
        float[] inner = perimeter(x + stroke, y + stroke, width - stroke * 2.0F, height - stroke * 2.0F,
                innerR, segments);
        int points = outer.length / 2;
        for (int i = 0; i <= points; i++) {
            int index = i % points;
            put(buffer, matrix, outer[index * 2], outer[index * 2 + 1], color);
            put(buffer, matrix, inner[index * 2], inner[index * 2 + 1], color);
        }
        Tesselator.getInstance().end();
        restore();
    }

    public static void glow(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color, int layers) {
        for (int layer = layers; layer >= 1; layer--) {
            float alpha = 0.05F * layer / layers;
            roundedRect(graphics, x - layer, y - layer, width + layer * 2, height + layer * 2,
                    radius + layer, withAlpha(color, alpha));
        }
    }

    public static void progressBar(GuiGraphics graphics, float x, float y, float width, float height, float progress,
                                   int trackColor, int fromColor, int toColor) {
        float radius = height / 2.0F;
        roundedRect(graphics, x, y, width, height, radius, trackColor);
        float filled = Math.max(0.0F, Math.min(1.0F, progress)) * width;
        if (filled > 2) {
            gradientRoundedH(graphics, x, y, filled, height, radius, fromColor, toColor);
        }
    }

    public static int lerpColor(int from, int to, float t) {
        float clamped = Math.max(0.0F, Math.min(1.0F, t));
        int a = lerpChannel(from >>> 24, to >>> 24, clamped);
        int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, clamped);
        int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, clamped);
        int b = lerpChannel(from & 0xFF, to & 0xFF, clamped);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int withAlpha(int color, float alpha) {
        int a = (int) (Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private static void meshRounded(GuiGraphics graphics, float x, float y, float width, float height, float radius,
                                    int fromColor, int toColor, boolean vertical) {
        if (width <= 0 || height <= 0) {
            return;
        }
        graphics.flush();
        float r = clampRadius(radius, width, height);
        int segments = arcSegments(r);
        float[] rim = perimeter(x, y, width, height, r, segments);
        int points = rim.length / 2;
        float midX = x + width * 0.5F;
        float midY = y + height * 0.5F;
        int centerColor = lerpColor(fromColor, toColor, 0.5F);

        beginColor();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = graphics.pose().last().pose();
        put(buffer, matrix, midX, midY, centerColor);
        for (int i = 0; i <= points; i++) {
            int index = i % points;
            float px = rim[index * 2];
            float py = rim[index * 2 + 1];
            float t = vertical
                    ? (height <= 1 ? 0.0F : (py - y) / height)
                    : (width <= 1 ? 0.0F : (px - x) / width);
            put(buffer, matrix, px, py, lerpColor(fromColor, toColor, t));
        }
        Tesselator.getInstance().end();
        restore();
    }

    private static float[] perimeter(float x, float y, float width, float height, float radius, int segments) {
        int steps = Math.max(1, segments);
        float[] out = new float[4 * steps * 2];
        int n = 0;
        n = arc(out, n, x + radius, y + radius, radius, (float) Math.PI, (float) (Math.PI * 1.5), steps);
        n = arc(out, n, x + width - radius, y + radius, radius, (float) (Math.PI * 1.5), (float) (Math.PI * 2.0), steps);
        n = arc(out, n, x + width - radius, y + height - radius, radius, 0.0F, (float) (Math.PI * 0.5), steps);
        arc(out, n, x + radius, y + height - radius, radius, (float) (Math.PI * 0.5), (float) Math.PI, steps);
        return out;
    }

    private static int arc(float[] out, int n, float cx, float cy, float radius, float from, float to, int steps) {
        float rad = Math.max(0.0F, radius);
        for (int i = 0; i < steps; i++) {
            float t = steps == 1 ? 0.0F : i / (float) (steps - 1);
            float angle = from + (to - from) * t;
            out[n++] = cx + (float) Math.cos(angle) * rad;
            out[n++] = cy + (float) Math.sin(angle) * rad;
        }
        return n;
    }

    private static int arcSegments(float radius) {
        if (radius < 1.0F) {
            return 1;
        }
        return Math.max(10, Math.min(36, Math.round(radius * 2.5F)));
    }

    private static float clampRadius(float radius, float width, float height) {
        return Math.max(0.0F, Math.min(radius, Math.min(width, height) * 0.5F));
    }

    private static void put(BufferBuilder buffer, Matrix4f matrix, float x, float y, int color) {
        buffer.vertex(matrix, x, y, 0.0F)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF)
                .endVertex();
    }

    private static void beginColor() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    private static void restore() {
        RenderSystem.enableCull();
    }

    private static int lerpChannel(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }
}
