package store.aquateche.aqualumen.client.render;

import net.minecraft.client.gui.GuiGraphics;

/** Small drawing kit: rounded panels, gradients, glow, progress bars and rings. No textures needed. */
public final class Gfx {

    private Gfx() {
    }

    public static void rect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    public static void roundedRect(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        gradientRounded(graphics, x, y, width, height, radius, color, color);
    }

    /** Vertical gradient with rounded corners, drawn scanline by scanline. */
    public static void gradientRounded(GuiGraphics graphics, int x, int y, int width, int height, int radius,
                                       int topColor, int bottomColor) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        for (int row = 0; row < height; row++) {
            int inset = cornerInset(row, height, r);
            float t = height <= 1 ? 0.0F : row / (float) (height - 1);
            graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, lerpColor(topColor, bottomColor, t));
        }
    }

    /** Horizontal gradient with rounded corners. */
    public static void gradientRoundedH(GuiGraphics graphics, int x, int y, int width, int height, int radius,
                                        int leftColor, int rightColor) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        for (int row = 0; row < height; row++) {
            int inset = cornerInset(row, height, r);
            for (int column = inset; column < width - inset; column++) {
                float t = width <= 1 ? 0.0F : column / (float) (width - 1);
                graphics.fill(x + column, y + row, x + column + 1, y + row + 1, lerpColor(leftColor, rightColor, t));
            }
        }
    }

    public static void outline(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        for (int row = 0; row < height; row++) {
            int inset = cornerInset(row, height, r);
            boolean edgeRow = row == 0 || row == height - 1 || row < r || row >= height - r;
            if (edgeRow) {
                int previous = cornerInset(Math.max(0, row - 1), height, r);
                int span = Math.max(1, Math.abs(previous - inset));
                graphics.fill(x + inset, y + row, x + inset + span, y + row + 1, color);
                graphics.fill(x + width - inset - span, y + row, x + width - inset, y + row + 1, color);
                if (row == 0 || row == height - 1) {
                    graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
                }
            } else {
                graphics.fill(x + inset, y + row, x + inset + 1, y + row + 1, color);
                graphics.fill(x + width - inset - 1, y + row, x + width - inset, y + row + 1, color);
            }
        }
    }

    /** Soft outer glow used for the active navigation item and primary buttons. */
    public static void glow(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color, int layers) {
        for (int layer = layers; layer >= 1; layer--) {
            float alpha = 0.06F * layer / layers;
            int tinted = withAlpha(color, alpha);
            roundedRect(graphics, x - layer, y - layer, width + layer * 2, height + layer * 2, radius + layer, tinted);
        }
    }

    public static void progressBar(GuiGraphics graphics, int x, int y, int width, int height, float progress,
                                   int trackColor, int fromColor, int toColor) {
        int radius = height / 2;
        roundedRect(graphics, x, y, width, height, radius, trackColor);
        int filled = Math.round(Math.max(0.0F, Math.min(1.0F, progress)) * width);
        if (filled > 2) {
            gradientRoundedH(graphics, x, y, filled, height, radius, fromColor, toColor);
        }
    }

    /** Circular progress ring, used for the level indicator on the profile card. */
    public static void ring(GuiGraphics graphics, int centerX, int centerY, int radius, int thickness,
                            float progress, int trackColor, int fromColor, int toColor) {
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        int inner = radius - thickness;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance > radius || distance < inner) {
                    continue;
                }
                double angle = (Math.atan2(dy, dx) + Math.PI / 2.0 + Math.PI * 2.0) % (Math.PI * 2.0);
                float t = (float) (angle / (Math.PI * 2.0));
                int color = t <= clamped ? lerpColor(fromColor, toColor, t / Math.max(0.001F, clamped)) : trackColor;
                graphics.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color);
            }
        }
    }

    public static int cornerInset(int row, int height, int radius) {
        if (radius <= 0) {
            return 0;
        }
        double dy;
        if (row < radius) {
            dy = radius - row - 0.5;
        } else if (row >= height - radius) {
            dy = row - (height - radius) + 0.5;
        } else {
            return 0;
        }
        double squared = (double) radius * radius - dy * dy;
        return radius - (int) Math.round(Math.sqrt(Math.max(0.0, squared)));
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

    private static int lerpChannel(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }
}
