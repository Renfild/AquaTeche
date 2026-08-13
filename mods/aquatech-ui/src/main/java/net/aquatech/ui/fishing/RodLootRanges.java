package net.aquatech.ui.fishing;

/**
 * Base catch totals before rate mods (always low — player scales with ×2…×64 in rod).
 */
public final class RodLootRanges {
    private RodLootRanges() {
    }

    public static int min(AquaTechFishingRodItem.RodType type) {
        return 2;
    }

    public static int max(AquaTechFishingRodItem.RodType type) {
        return 4;
    }

    /** Random target total in [min, max] inclusive. */
    public static int rollTotal(AquaTechFishingRodItem.RodType type, net.minecraft.util.RandomSource random) {
        int lo = min(type);
        int hi = max(type);
        if (hi <= lo) return lo;
        return lo + random.nextInt(hi - lo + 1);
    }
}
