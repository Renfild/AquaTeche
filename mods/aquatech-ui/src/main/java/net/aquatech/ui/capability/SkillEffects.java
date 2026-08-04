package net.aquatech.ui.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Aggregates unlocked skill bonuses. Caps prevent runaway stacking.
 * Values must match descriptions in {@link SkillDefinitions}.
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
        if (player == null) return false;
        return player.getCapability(AquaSkillCapability.INSTANCE)
                .map(cap -> cap.hasSkill(skillId) || "origin".equals(skillId))
                .orElse(false);
    }

    public static Player nearestPlayer(Level level, BlockPos pos, double range) {
        if (level == null || pos == null) return null;
        return level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, range, false);
    }

    /** Single capability lookup for passives / pressure / similar multi-bonus paths. */
    public static Snapshot snapshot(Player player) {
        if (player == null) return Snapshot.EMPTY;
        return player.getCapability(AquaSkillCapability.INSTANCE).map(SkillEffects::fromCap).orElse(Snapshot.EMPTY);
    }

    private static Snapshot fromCap(AquaSkillCapability cap) {
        float fishing = 0f, catchMult = 0f, rare = 0f, machine = 0f, feGen = 0f, feEff = 0f;
        float swim = 0f, air = 0f, resist = 0f, kelp = 0f;
        for (SkillDefinitions.SkillDef def : SkillDefinitions.all()) {
            if ("origin".equals(def.id()) || !cap.hasSkill(def.id())) continue;
            switch (def.category()) {
                case FISHING_SPEED -> fishing += def.value();
                case CATCH_MULT -> catchMult += def.value();
                case RARE_LOOT -> rare += def.value();
                case MACHINE_SPEED -> machine += def.value();
                case FE_GEN -> feGen += def.value();
                case FE_EFFICIENCY -> feEff += def.value();
                case SWIM_SPEED -> swim += def.value();
                case AIR -> air += def.value();
                case WATER_RESIST -> resist += def.value();
                case KELP_HARVEST -> kelp += def.value();
                default -> {
                }
            }
        }
        boolean master = cap.hasSkill("master_angler");
        boolean poseidon = cap.hasSkill("poseidon_blessing");
        boolean overdrive = cap.hasSkill("overdrive_machine");
        boolean immortal = cap.hasSkill("immortal_diver");
        boolean harmony = cap.hasSkill("ocean_harmony");
        boolean deep = cap.hasSkill("deep_resonance");
        boolean waterBreath = cap.hasSkill("water_breathing") || immortal;
        boolean nightVision = cap.hasSkill("night_vision") || immortal;
        boolean livingKelp = cap.hasSkill("living_kelp");
        boolean immortalOrg = cap.hasSkill("immortal_organism");

        if (master) {
            fishing += 0.20f;
            catchMult += 0.20f;
        }
        if (poseidon) rare += 0.20f;
        if (overdrive) {
            machine += 0.20f;
            feGen += 0.20f;
        }
        if (immortal) {
            swim += 0.15f;
            resist += 0.10f;
        }
        if (harmony) {
            fishing += 0.03f;
            catchMult += 0.03f;
            rare += 0.03f;
            machine += 0.03f;
            feGen += 0.03f;
            feEff += 0.03f;
            swim += 0.03f;
            kelp += 0.03f;
        }
        if (deep) {
            fishing += 0.05f;
            catchMult += 0.05f;
            rare += 0.05f;
            machine += 0.05f;
            feGen += 0.05f;
            feEff += 0.05f;
            swim += 0.05f;
            kelp += 0.05f;
        }
        if (immortalOrg) kelp += 0.25f;

        int depth = 0;
        if (cap.hasSkill("depth_armor")) depth += 6;
        if (cap.hasSkill("pressure_resist")) depth += 6;
        if (cap.hasSkill("water_breathing")) depth += 4;
        if (immortal) depth += 12;

        int regenAmp = immortalOrg ? 1 : livingKelp ? 0 : -1;
        float feEffCapped = Math.min(CAP_FE_EFFICIENCY, feEff);

        return new Snapshot(
                Math.min(CAP_FISHING_SPEED, fishing),
                Math.min(1.0f + CAP_CATCH_MULT, 1.0f + catchMult),
                Math.min(CAP_RARE_LOOT, rare),
                1.0f + Math.min(CAP_MACHINE_SPEED, machine),
                1.0f + Math.min(CAP_FE_GEN, feGen),
                1.0f - feEffCapped,
                Math.min(CAP_SWIM, swim),
                Math.round(air),
                waterBreath,
                nightVision,
                Math.min(CAP_WATER_RESIST, resist),
                depth,
                Math.min(CAP_KELP, kelp),
                livingKelp || immortalOrg,
                regenAmp
        );
    }

    private static float sumCategory(Player player, SkillDefinitions.SkillCategory category) {
        if (player == null) return 0f;
        return player.getCapability(AquaSkillCapability.INSTANCE).map(cap -> {
            float sum = 0f;
            for (SkillDefinitions.SkillDef def : SkillDefinitions.all()) {
                if (def.category() != category) continue;
                if (!cap.hasSkill(def.id()) && !"origin".equals(def.id())) continue;
                if ("origin".equals(def.id())) continue;
                sum += def.value();
            }
            return sum;
        }).orElse(0f);
    }

    /** Additive fishing progress / bite speed. Cap 40%. */
    public static float fishingSpeedBonus(Player player) {
        float b = sumCategory(player, SkillDefinitions.SkillCategory.FISHING_SPEED);
        if (has(player, "master_angler")) b += 0.20f;
        if (has(player, "ocean_harmony")) b += 0.03f;
        if (has(player, "deep_resonance")) b += 0.05f;
        // avoid double-counting ALL_BONUS nodes that are only for fishing keystone
        // master_angler is ALL_BONUS — already added above once
        return Math.min(CAP_FISHING_SPEED, b);
    }

    /**
     * Chance to duplicate catch once: returns multiplier such that (mult-1) is the chance.
     * Cap +50% chance.
     */
    public static float catchMultiplier(Player player) {
        float m = 1.0f + sumCategory(player, SkillDefinitions.SkillCategory.CATCH_MULT);
        if (has(player, "master_angler")) m += 0.20f;
        if (has(player, "ocean_harmony")) m += 0.03f;
        if (has(player, "deep_resonance")) m += 0.05f;
        return Math.min(1.0f + CAP_CATCH_MULT, m);
    }

    /** Extra rare loot chance. Cap 50%. */
    public static float rareLootBonus(Player player) {
        float b = sumCategory(player, SkillDefinitions.SkillCategory.RARE_LOOT);
        if (has(player, "poseidon_blessing")) b += 0.20f;
        if (has(player, "ocean_harmony")) b += 0.03f;
        if (has(player, "deep_resonance")) b += 0.05f;
        return Math.min(CAP_RARE_LOOT, b);
    }

    /** Machine progress multiplier (1.0 = base). Cap +50%. */
    public static float machineSpeedMultiplier(Player player) {
        float b = sumCategory(player, SkillDefinitions.SkillCategory.MACHINE_SPEED);
        if (has(player, "overdrive_machine")) b += 0.20f;
        if (has(player, "ocean_harmony")) b += 0.03f;
        if (has(player, "deep_resonance")) b += 0.05f;
        return 1.0f + Math.min(CAP_MACHINE_SPEED, b);
    }

    /** Hydro FE generation multiplier. Cap +50%. */
    public static float hydroFeBonus(Player player) {
        float b = sumCategory(player, SkillDefinitions.SkillCategory.FE_GEN);
        if (has(player, "overdrive_machine")) b += 0.20f;
        if (has(player, "ocean_harmony")) b += 0.03f;
        if (has(player, "deep_resonance")) b += 0.05f;
        return 1.0f + Math.min(CAP_FE_GEN, b);
    }

    /** Energy cost factor (&lt;1 = cheaper). Cap −40%. */
    public static float energyCostFactor(Player player) {
        float reduce = sumCategory(player, SkillDefinitions.SkillCategory.FE_EFFICIENCY);
        if (has(player, "ocean_harmony")) reduce += 0.03f;
        if (has(player, "deep_resonance")) reduce += 0.05f;
        reduce = Math.min(CAP_FE_EFFICIENCY, reduce);
        return 1.0f - reduce;
    }

    public static float swimSpeedBonus(Player player) {
        float b = sumCategory(player, SkillDefinitions.SkillCategory.SWIM_SPEED);
        if (has(player, "immortal_diver")) b += 0.15f;
        if (has(player, "ocean_harmony")) b += 0.03f;
        if (has(player, "deep_resonance")) b += 0.05f;
        return Math.min(CAP_SWIM, b);
    }

    /** Extra air supply ticks (20 ticks = 1 sec). */
    public static int extraAirTicks(Player player) {
        return Math.round(sumCategory(player, SkillDefinitions.SkillCategory.AIR));
    }

    public static boolean waterBreathing(Player player) {
        return has(player, "water_breathing") || has(player, "immortal_diver");
    }

    public static boolean nightVisionWater(Player player) {
        return has(player, "night_vision") || has(player, "immortal_diver");
    }

    public static float waterDamageReduction(Player player) {
        float b = sumCategory(player, SkillDefinitions.SkillCategory.WATER_RESIST);
        if (has(player, "immortal_diver")) b += 0.10f;
        return Math.min(CAP_WATER_RESIST, b);
    }

    /** Extra safe depth in meters from diving skills (stacked with armor tolerance). */
    public static int pressureDepthTolerance(Player player) {
        if (player == null) return 0;
        int meters = 0;
        if (has(player, "depth_armor")) meters += 6;
        if (has(player, "pressure_resist")) meters += 6;
        if (has(player, "water_breathing")) meters += 4;
        if (has(player, "immortal_diver")) meters += 12;
        return meters;
    }

    /** Extra kelp drop multiplier (0.25 = +25%). Cap +200%. */
    public static float kelpHarvestBonus(Player player) {
        float b = sumCategory(player, SkillDefinitions.SkillCategory.KELP_HARVEST);
        if (has(player, "immortal_organism")) b += 0.25f;
        if (has(player, "ocean_harmony")) b += 0.03f;
        if (has(player, "deep_resonance")) b += 0.05f;
        return Math.min(CAP_KELP, b);
    }

    public static boolean regenInWater(Player player) {
        return has(player, "living_kelp") || has(player, "immortal_organism");
    }

    public static int regenAmplifier(Player player) {
        if (has(player, "immortal_organism")) return 1; // Regen II
        if (has(player, "living_kelp")) return 0; // Regen I
        return -1;
    }

    /** Convenience for AutoFisher progress: base progress * (1 + fishingSpeed). */
    public static float autoFisherSpeed(Player player) {
        return 1.0f + fishingSpeedBonus(player);
    }
}
