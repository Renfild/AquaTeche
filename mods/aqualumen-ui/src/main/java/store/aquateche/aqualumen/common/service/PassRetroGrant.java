package store.aquateche.aqualumen.common.service;

import java.util.List;
import java.util.Set;

/**
 * Free-track tiers 3, 8 and 13 got extra rewards after some players had already claimed them.
 * Pure rules: which of those tiers still owe their extra.
 */
public final class PassRetroGrant {

    public static final List<Integer> TIERS = List.of(3, 8, 13);

    public record Extra(boolean smallBooster, String caseId) {
    }

    private PassRetroGrant() {
    }

    /** The extra a tier carries on top of its coins, or null for a tier without one. */
    public static Extra extraFor(int tier) {
        return switch (tier) {
            case 3, 8 -> new Extra(true, null);
            case 13 -> new Extra(false, "starter");
            default -> null;
        };
    }

    /** Claimed tiers whose extra has not been handed out yet, in ascending order. */
    public static List<Integer> due(Set<Integer> claimedTiers, Set<Integer> extrasGiven) {
        return TIERS.stream().filter(t -> claimedTiers.contains(t) && !extrasGiven.contains(t)).toList();
    }
}
