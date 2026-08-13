package net.aquatech.ui.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Soft AquaTech motion for machine screens — pulse, scan, bubbles, turbine.
 * Keep effects subtle; never fight slot readability.
 */
public final class MachineGuiFx {
    private MachineGuiFx() {
    }

    public static float time(float partialTick) {
        return (System.currentTimeMillis() % 1_000_000L) / 1000.0F + partialTick * 0.05F;
    }

    public static int withAlpha(int rgb, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    /** Soft breathing glow on the top accent line. */
    public static void accentPulse(GuiGraphics g, int x, int y, int w, float t) {
        float pulse = 0.35F + 0.25F * (0.5F + 0.5F * Mth.sin(t * 2.2F));
        g.fill(x + 8, y + 2, x + w - 8, y + 3, withAlpha(0x5AC8FA, pulse));
    }

    /** Slow horizontal scan shimmer across the machine band. */
    public static void scanShimmer(GuiGraphics g, int x, int y, int w, float t, boolean active) {
        float speed = active ? 1.6F : 0.55F;
        int band = 14;
        int pos = (int) ((t * speed * 40.0F) % (w + band)) - band;
        int left = x + 6 + pos;
        int right = left + band;
        int clipL = x + 6;
        int clipR = x + w - 6;
        if (right <= clipL || left >= clipR) return;
        int drawL = Math.max(left, clipL);
        int drawR = Math.min(right, clipR);
        float a = active ? 0.18F : 0.08F;
        g.fill(drawL, y + 16, drawR, y + 78, withAlpha(0x38BDF8, a));
    }

    /** Rising bubble motes in the machine area. */
    public static void bubbles(GuiGraphics g, int x, int y, float t, boolean active, int seed) {
        if (!active) return;
        for (int i = 0; i < 6; i++) {
            float phase = t * (0.7F + (i % 3) * 0.15F) + i * 1.7F + seed * 0.3F;
            float rise = (phase % 1.0F);
            int bx = x + 20 + ((i * 23 + seed * 7) % 130);
            int by = y + 72 - (int) (rise * 52);
            float fade = 1.0F - rise;
            int size = 1 + (i % 2);
            g.fill(bx, by, bx + size, by + size, withAlpha(0x7DD3FC, 0.15F + 0.35F * fade));
        }
    }

    /** Soft vertical shimmer on filled energy column. */
    public static void energyPulse(GuiGraphics g, int x, int y, int w, int h, int filled, float t) {
        if (filled <= 0) return;
        int top = y + (h - filled);
        float pulse = 0.25F + 0.20F * (0.5F + 0.5F * Mth.sin(t * 3.5F));
        g.fill(x, top, x + w, y + h, withAlpha(0xFFFFFF, pulse * 0.12F));
        // moving highlight speck
        int speck = top + (int) ((t * 18.0F) % Math.max(1, filled));
        g.fill(x, speck, x + w, speck + 2, withAlpha(0xE0F2FE, 0.35F));
    }

    /** Progress arrow glow trail while crafting. */
    public static void progressGlow(GuiGraphics g, int x, int y, int scaled, float t) {
        if (scaled <= 0) return;
        float pulse = 0.30F + 0.25F * (0.5F + 0.5F * Mth.sin(t * 5.0F));
        g.fill(x, y + 5, x + scaled, y + 12, withAlpha(0x4ADE80, pulse * 0.25F));
        int tip = x + scaled - 1;
        g.fill(tip, y + 2, tip + 2, y + 15, withAlpha(0x86EFAC, pulse));
    }

    /** Tiny spinning turbine for Ocean Filter. */
    public static void turbine(GuiGraphics g, int cx, int cy, float t, boolean active) {
        float speed = active ? 6.5F : 0.8F;
        float ang = t * speed;
        int color = withAlpha(0x38BDF8, active ? 0.85F : 0.35F);
        int hub = withAlpha(0x0EA5E9, active ? 0.95F : 0.45F);
        for (int blade = 0; blade < 4; blade++) {
            float a = ang + blade * (float) (Math.PI * 0.5);
            int x2 = cx + Mth.floor(Mth.cos(a) * 10);
            int y2 = cy + Mth.floor(Mth.sin(a) * 10);
            drawLine(g, cx, cy, x2, y2, color);
        }
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, hub);
        // ring
        ring(g, cx, cy, 12, withAlpha(0x5AC8FA, active ? 0.55F : 0.22F));
    }

    /** Soft ritual pulse around altar center slot. */
    public static void altarPulse(GuiGraphics g, int slotX, int slotY, float t, boolean active) {
        float wave = 0.5F + 0.5F * Mth.sin(t * (active ? 3.2F : 1.2F));
        int r = 10 + (int) (wave * (active ? 4 : 2));
        ring(g, slotX + 8, slotY + 8, r, withAlpha(0xFBBF24, 0.12F + wave * 0.18F));
        if (active) {
            for (int i = 0; i < 4; i++) {
                float a = t * 2.0F + i * (float) (Math.PI * 0.5);
                int px = slotX + 8 + Mth.floor(Mth.cos(a) * 22);
                int py = slotY + 8 + Mth.floor(Mth.sin(a) * 18);
                g.fill(px, py, px + 2, py + 2, withAlpha(0xA78BFA, 0.45F));
            }
        }
    }

    /** Flame flicker overlay above burn well. */
    public static void flameFlicker(GuiGraphics g, int x, int y, int h, float t) {
        if (h <= 0) return;
        float flick = 0.55F + 0.45F * (0.5F + 0.5F * Mth.sin(t * 11.0F));
        g.fill(x + 3, y + (14 - h), x + 11, y + 14, withAlpha(0xFDE68A, 0.15F * flick));
        g.fill(x + 5, y + (14 - Math.max(1, h - 2)), x + 9, y + 12, withAlpha(0xF97316, 0.25F * flick));
    }

    /** Idle sparkle on tackle slots. */
    public static void slotTwinkle(GuiGraphics g, int slotX, int slotY, float t, int index) {
        float phase = t * 2.4F + index * 1.3F;
        float a = 0.10F + 0.18F * Math.max(0.0F, Mth.sin(phase));
        g.fill(slotX - 1, slotY - 1, slotX + 17, slotY, withAlpha(0x67E8F9, a));
        g.fill(slotX - 1, slotY - 1, slotX, slotY + 17, withAlpha(0x67E8F9, a));
    }

    public static void workingDots(GuiGraphics g, int x, int y, float t, boolean active) {
        if (!active) return;
        int step = ((int) (t * 3.0F)) % 4;
        for (int i = 0; i < 3; i++) {
            float a = i <= step ? 0.85F : 0.20F;
            g.fill(x + i * 4, y, x + i * 4 + 2, y + 2, withAlpha(0x5AC8FA, a));
        }
    }

    private static void ring(GuiGraphics g, int cx, int cy, int radius, int color) {
        for (int i = 0; i < 24; i++) {
            float a = i * (float) (Math.PI * 2.0 / 24.0);
            int px = cx + Mth.floor(Mth.cos(a) * radius);
            int py = cy + Mth.floor(Mth.sin(a) * radius);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    private static void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1;
        int y = y1;
        while (true) {
            g.fill(x, y, x + 1, y + 1, color);
            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }
}
