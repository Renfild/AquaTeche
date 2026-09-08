package net.aquatech.ui.client.fishing;

import net.aquatech.ui.fishing.FishingRodCompat;
import net.aquatech.ui.fishing.StarCatcherAttachments;
import net.aquatech.ui.item.RateModItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.List;

/** Цветная пыль цвета тира вокруг поплавка, пока активен рейт-мод (x2..x64). */
public final class RateParticles {
    private static final int CADENCE_TICKS = 4;
    private static Entity cachedBob;
    private static long cachedBobAt;

    private RateParticles() {
    }

    /** Вызывается из клиентского тика. */
    public static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        if (mc.level.getGameTime() % CADENCE_TICKS != 0) return;

        ItemStack rod = heldRod(player);
        if (rod.isEmpty()) {
            cachedBob = null;
            return;
        }
        int mult = StarCatcherAttachments.readRateMultiplier(rod);
        if (mult <= 1) return;

        float[] rgb = tierColor(mult);
        if (rgb == null) return;

        Entity bob = findBob(player);
        if (bob == null) return;
        cachedBob = bob;

        DustParticleOptions dust = new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2]), 1.25F);
        double t = (mc.level.getGameTime() % 40) / 40.0 * Math.PI * 2;
        double ox = Math.cos(t) * 0.34;
        double oz = Math.sin(t) * 0.34;
        double oy = bob.getY() + 0.22 + Math.sin(t * 2) * 0.07;
        mc.level.addParticle(dust, bob.getX() + ox, oy, bob.getZ() + oz, 0, 0.012, 0);
        double t2 = t + Math.PI;
        mc.level.addParticle(dust, bob.getX() + Math.cos(t2) * 0.3, oy + 0.1, bob.getZ() + Math.sin(t2) * 0.3, 0, 0.008, 0);
    }

    private static ItemStack heldRod(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (FishingRodCompat.isSupportedRod(main)) return main;
        ItemStack off = player.getOffhandItem();
        return FishingRodCompat.isSupportedRod(off) ? off : ItemStack.EMPTY;
    }

    /** Ванильный поплавок или SC FishingBobEntity в радиусе 48. */
    private static Entity findBob(LocalPlayer player) {
        if (player.fishing != null) return player.fishing;
        long now = player.level().getGameTime();
        if (cachedBob != null && cachedBob.isAlive() && now - cachedBobAt < 100) {
            return cachedBob;
        }
        AABB box = player.getBoundingBox().inflate(48.0D, 32.0D, 48.0D);
        List<Entity> nearby = player.level().getEntities(player, box,
                e -> e.getClass().getName().contains("FishingBobEntity"));
        Entity best = null;
        double bestD = Double.MAX_VALUE;
        for (Entity e : nearby) {
            double d = e.distanceToSqr(player);
            if (d < bestD) {
                bestD = d;
                best = e;
            }
        }
        cachedBob = best;
        cachedBobAt = now;
        return best;
    }

    /** Цвет тира = палитра магазинных медальонов (x2 зелёный -> x64 красный). */
    public static float[] tierColor(int mult) {
        return switch (mult) {
            case 2 -> new float[]{0.30F, 0.82F, 0.54F};
            case 4 -> new float[]{0.23F, 0.62F, 1.00F};
            case 8 -> new float[]{0.96F, 0.76F, 0.36F};
            case 16 -> new float[]{1.00F, 0.55F, 0.26F};
            case 32 -> new float[]{0.76F, 0.39F, 1.00F};
            case 64 -> new float[]{1.00F, 0.35F, 0.35F};
            default -> null;
        };
    }
}
