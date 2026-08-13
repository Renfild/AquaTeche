package net.aquatech.ui.client.render;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * StarCatcher-style hit VFX on the fishing bar.
 */
public final class HitParticle {
    public float x;
    public float y;
    public float vx;
    public float vy;
    public int life;
    public int maxLife;
    public int frame;
    public int color;

    public HitParticle(float x, float y, int color, RandomSource rng) {
        this.x = x;
        this.y = y;
        float ang = rng.nextFloat() * (float) (Math.PI * 2);
        float spd = 0.6f + rng.nextFloat() * 1.4f;
        this.vx = Mth.cos(ang) * spd;
        this.vy = Mth.sin(ang) * spd - 0.5f;
        this.maxLife = 12 + rng.nextInt(8);
        this.life = maxLife;
        this.frame = rng.nextInt(6);
        this.color = color;
    }

    public boolean tick() {
        x += vx;
        y += vy;
        vy += 0.06f;
        life--;
        return life > 0;
    }

    public float alpha() {
        return Mth.clamp(life / (float) maxLife, 0f, 1f);
    }
}
