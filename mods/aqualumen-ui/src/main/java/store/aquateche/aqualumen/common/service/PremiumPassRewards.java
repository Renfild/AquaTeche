package store.aquateche.aqualumen.common.service;

import store.aquateche.aqualumen.common.service.BoosterLogic.Tier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Premium track of the season pass: pure reward table, no Minecraft types.
 * Coins are twice the free track; on every free case milestone the premium case is a better case than the free one
 * (free: starter, smeltery, applied, superconductor, draconic), extra cases fill levels 2/7/12/17,
 * the rest are boosters and pearl multipliers.
 * The F4 page mirrors this table in tools/hub_assets/pass_view.js (PASS_PREMIUM) - keep both in sync.
 */
public final class PremiumPassRewards {

    public static final int MAX_TIER = 25;

    /** @param caseId case key to hand out (null for none); itemId registry id of a stackable item (null for none);
     *               booster/boosterCount fish-shop booster (null for none) */
    public record Reward(long coins, String caseId, String itemId, Tier booster, int boosterCount) {
    }

    private static final long[] COINS = {
            5000, 6000, 7000, 8000, 10000, 12000, 14000,
            16000, 18000, 24000, 28000, 32000, 36000, 40000,
            50000, 60000, 70000, 80000, 90000, 100000, 110000,
            120000, 140000, 160000, 200000
    };

    private PremiumPassRewards() {
    }

    public static Reward forTier(int tier) {
        if (tier < 1 || tier > MAX_TIER) {
            return null;
        }
        long coins = COINS[tier - 1];
        return switch (tier) {
            case 2 -> new Reward(coins, "smeltery", null, null, 0);
            case 3, 6 -> new Reward(coins, null, null, Tier.SMALL, 1);
            case 5 -> new Reward(coins, "steam", null, null, 0);
            case 7 -> new Reward(coins, "steam", null, null, 0);
            case 10 -> new Reward(coins, "flora", null, null, 0);
            case 12 -> new Reward(coins, "applied", null, null, 0);
            case 13 -> new Reward(coins, null, "aquatech_ui:rate_x8", null, 0);
            case 14, 18 -> new Reward(coins, null, null, Tier.LARGE, 1);
            case 15 -> new Reward(coins, "abyss", null, null, 0);
            case 17 -> new Reward(coins, "abyss", null, null, 0);
            case 20 -> new Reward(coins, "singularity", null, null, 0);
            case 23 -> new Reward(coins, null, "aquatech_ui:rate_x32", null, 0);
            case 25 -> new Reward(coins, "infinity", null, Tier.LARGE, 1);
            default -> new Reward(coins, null, null, null, 0);
        };
    }

    /** The hub draws MAX_TIER levels; the config may only lower that. */
    public static int passMax(int configMax) {
        return Math.max(1, Math.min(MAX_TIER, configMax));
    }

    /** Season level the pass works with: raw player level clamped to 1..passMax. */
    public static int effectiveTier(int rawTier, int configMax) {
        return Math.max(1, Math.min(passMax(configMax), rawTier));
    }

    /** Reached, not yet claimed tiers in ascending order. */
    public static List<Integer> claimable(int reachedTier, Set<Integer> claimed) {
        List<Integer> out = new ArrayList<>();
        for (int t = 1; t <= Math.min(reachedTier, MAX_TIER); t++) {
            if (!claimed.contains(t)) {
                out.add(t);
            }
        }
        return out;
    }
}
