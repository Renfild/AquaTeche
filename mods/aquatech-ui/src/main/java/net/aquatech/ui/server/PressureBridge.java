package net.aquatech.ui.server;

import net.aquatech.ui.capability.SkillEffects;
import net.aquatech.ui.item.SonarGogglesItem;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Depth / pressure used by HUD and ocean mechanics.
 * Sea level = Y 190. Build floor = 50 m below sea (Y 140).
 */
public final class PressureBridge {
    public static final int SEA_LEVEL_Y = 190;
    /** How many blocks below sea level players may place blocks. */
    public static final int MAX_BUILD_DEPTH_BELOW_SEA = 50;
    public static final int MIN_BUILD_Y = SEA_LEVEL_Y - MAX_BUILD_DEPTH_BELOW_SEA; // 140

    private PressureBridge() {
    }

    public record PressureInfo(boolean inWater, int depth, int tolerance, int effective) {
    }

    public static PressureInfo fromPlayer(Player player) {
        boolean inWater = player.isEyeInFluid(FluidTags.WATER) || player.isInWater();
        if (!inWater) {
            return new PressureInfo(false, 0, 0, 0);
        }
        int depth = Math.max(0, SEA_LEVEL_Y - player.blockPosition().getY());
        int tolerance = 10 + armorPieceBonus(player) + skillAndGearTolerance(player);
        int effective = Math.max(0, depth - tolerance);
        return new PressureInfo(true, depth, tolerance, effective);
    }

    public static int depthBelowSeaLevel(Entity entity) {
        if (entity instanceof Player player) {
            return fromPlayer(player).depth();
        }
        if (!entity.isEyeInFluid(FluidTags.WATER)) {
            return 0;
        }
        return Math.max(0, SEA_LEVEL_Y - entity.blockPosition().getY());
    }

    public static boolean isBelowBuildLimit(int blockY) {
        return blockY < MIN_BUILD_Y;
    }

    private static int armorPieceBonus(Player player) {
        int pieces = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty()) pieces++;
        }
        return pieces * 2;
    }

    private static int skillAndGearTolerance(Player player) {
        int t = SkillEffects.pressureDepthTolerance(player);
        ItemStack helmet = player.getInventory().getArmor(3);
        if (helmet.getItem() instanceof SonarGogglesItem) {
            t += 4;
        }
        return t;
    }
}
