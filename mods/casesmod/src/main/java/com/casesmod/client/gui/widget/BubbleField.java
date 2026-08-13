package com.casesmod.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Random;

/**
 * Pooled bubble particles for the ocean menu. Hard cap, no per-frame allocations.
 */
public final class BubbleField {

    private static final int MAX = 20;

    private final float[] x = new float[MAX];
    private final float[] y = new float[MAX];
    private final float[] speed = new float[MAX];
    private final float[] wobble = new float[MAX];
    private final float[] phase = new float[MAX];
    private final int[] size = new int[MAX];
    private final boolean[] alive = new boolean[MAX];
    private final Random rng = new Random();

    private int width;
    private int height;
    private float spawnAcc;
    private boolean seeded;

    public void resize(int w, int h) {
        this.width = Math.max(1, w);
        this.height = Math.max(1, h);
        if (!seeded) {
            seeded = true;
            for (int i = 0; i < MAX / 2; i++) {
                spawnAt(i, true);
            }
        }
    }

    /** Call once per client tick (or from render with dt). */
    public void tick(float dt) {
        if (width <= 0 || height <= 0) return;
        spawnAcc += dt;
        // ~2 bubbles / second
        while (spawnAcc >= 0.5f) {
            spawnAcc -= 0.5f;
            int slot = findFree();
            if (slot >= 0) spawnAt(slot, false);
        }

        for (int i = 0; i < MAX; i++) {
            if (!alive[i]) continue;
            y[i] -= speed[i] * dt * 60f;
            x[i] += Mth.sin(phase[i] + y[i] * 0.04f) * wobble[i] * dt * 60f;
            phase[i] += dt * 1.5f;
            if (y[i] < -12f || x[i] < -16f || x[i] > width + 16f) {
                alive[i] = false;
            }
        }
    }

    public void render(GuiGraphics gfx) {
        for (int i = 0; i < MAX; i++) {
            if (!alive[i]) continue;
            int px = (int) x[i];
            int py = (int) y[i];
            int s = size[i];
            // Outer soft + inner highlight — two fills, no shaders
            gfx.fill(px, py, px + s, py + s, 0x335CE1FF);
            if (s >= 3) {
                gfx.fill(px + 1, py + 1, px + s - 1, py + s - 1, 0x55A8F0FF);
                gfx.fill(px + 1, py + 1, px + 2, py + 2, 0x88FFFFFF);
            }
        }
    }

    private int findFree() {
        for (int i = 0; i < MAX; i++) {
            if (!alive[i]) return i;
        }
        return -1;
    }

    private void spawnAt(int i, boolean anywhere) {
        alive[i] = true;
        x[i] = rng.nextFloat() * width;
        y[i] = anywhere ? rng.nextFloat() * height : height + 4f + rng.nextFloat() * 20f;
        speed[i] = 0.35f + rng.nextFloat() * 0.55f;
        wobble[i] = 0.08f + rng.nextFloat() * 0.18f;
        phase[i] = rng.nextFloat() * 6.28f;
        size[i] = 2 + rng.nextInt(4);
    }
}
