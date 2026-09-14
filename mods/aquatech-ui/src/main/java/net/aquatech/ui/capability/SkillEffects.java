package net.aquatech.ui.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

/**
 * Древо талантов УДАЛЕНО навсегда (решение владельца, 2026-09-15).
 * Классы оставлены как нейтральные заглушки: все бонусы равны нулю,
 * дерево пусто. Не восстанавливать и не предлагать.
 */
public final class SkillEffects {

    public static final float CAP_FISHING_SPEED = 0.40f;
    public static final float CAP_CATCH_MULT = 0.50f;
    public static final float CAP_RARE_LOOT = 0.50f;
    public static final float CAP_MACHINE_SPEED = 0.50f;
    public static final float CAP_FE_GEN = 0.50f;
    public static final float CAP_FE_EFFICIENCY = 0.40f;
    public static final float CAP_SWIM = 0.40f;
    public static final float CAP_KELP = 2.00f;
    public static final float CAP_WATER_RESIST = 0.30f;

    /** One capability read → common bonuses for tick/hurt hot paths. */
    public record Snapshot(
            float fishingSpeedBonus,
            float catchMultiplier,
            float rareLootBonus,
            float machineSpeedMultiplier,
            float hydroFeBonus,
            float energyCostFactor,
            float swimSpeedBonus,
            int extraAirTicks,
            boolean waterBreathing,
            boolean nightVisionWater,
            float waterDamageReduction,
            int pressureDepthTolerance,
            float kelpHarvestBonus,
            boolean regenInWater,
            int regenAmplifier
    ) {
        public static final Snapshot EMPTY = new Snapshot(
                0f, 1f, 0f, 1f, 1f, 1f, 0f, 0,
                false, false, 0f, 0, 0f, false, -1
        );
    }

    private SkillEffects() {
    }

    public static boolean has(Player player, String skillId) {
        return false;
    }

    public static Player nearestPlayer(Level level, BlockPos pos, double range) {
        if (level == null || pos == null) return null;
        return level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, range, false);
    }

    public static Snapshot snapshot(Player player) {
        return Snapshot.EMPTY;
    }

    public static float fishingSpeedBonus(Player player) {
        return 0f;
    }

    public static float catchMultiplier(Player player) {
        return 1f;
    }

    public static float rareLootBonus(Player player) {
        return 0f;
    }

    public static float machineSpeedMultiplier(Player player) {
        return 1f;
    }

    public static float hydroFeBonus(Player player) {
        return 0f;
    }

    public static float energyCostFactor(Player player) {
        return 1f;
    }

    public static float swimSpeedBonus(Player player) {
        return 0f;
    }

    public static int extraAirTicks(Player player) {
        return 0;
    }

    public static boolean waterBreathing(Player player) {
        return false;
    }

    public static boolean nightVisionWater(Player player) {
        return false;
    }

    public static float waterDamageReduction(Player player) {
        return 0f;
    }

    public static int pressureDepthTolerance(Player player) {
        return 0;
    }

    public static float kelpHarvestBonus(Player player) {
        return 0f;
    }

    public static boolean regenInWater(Player player) {
        return false;
    }

    public static int regenAmplifier(Player player) {
        return -1;
    }

    public static float autoFisherSpeed(Player player) {
        return 1f;
    }
}
