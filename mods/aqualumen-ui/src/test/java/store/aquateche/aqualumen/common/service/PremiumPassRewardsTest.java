package store.aquateche.aqualumen.common.service;

import org.junit.jupiter.api.Test;
import store.aquateche.aqualumen.common.service.BoosterLogic.Tier;
import store.aquateche.aqualumen.common.service.PremiumPassRewards.Reward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PremiumPassRewardsTest {

    @Test
    void everyTierInRangeHasCoins() {
        for (int tier = 1; tier <= PremiumPassRewards.MAX_TIER; tier++) {
            Reward r = PremiumPassRewards.forTier(tier);
            assertNotNull(r, "tier " + tier);
            assertTrue(r.coins() > 0, "tier " + tier);
        }
    }

    @Test
    void outOfRangeTiersHaveNoReward() {
        assertNull(PremiumPassRewards.forTier(0));
        assertNull(PremiumPassRewards.forTier(-3));
        assertNull(PremiumPassRewards.forTier(PremiumPassRewards.MAX_TIER + 1));
    }

    @Test
    void premiumCoinsAreDoubleTheFreeTrack() {
        assertEquals(5000L, PremiumPassRewards.forTier(1).coins());
        assertEquals(24000L, PremiumPassRewards.forTier(10).coins());
        assertEquals(200000L, PremiumPassRewards.forTier(25).coins());
    }

    @Test
    void premiumFillsTheEarlyAndMiddleLevelsWithCases() {
        assertEquals("smeltery", PremiumPassRewards.forTier(2).caseId());
        assertEquals("steam", PremiumPassRewards.forTier(7).caseId());
        assertEquals("applied", PremiumPassRewards.forTier(12).caseId());
        assertEquals("abyss", PremiumPassRewards.forTier(17).caseId());
    }

    private static final java.util.List<String> CASE_LADDER = java.util.List.of(
            "starter", "smeltery", "steam", "flora", "applied", "abyss", "superconductor", "singularity", "draconic", "infinity");
    private static final java.util.Map<Integer, String> FREE_CASES = java.util.Map.of(
            5, "starter", 10, "smeltery", 15, "applied", 20, "superconductor", 25, "draconic");

    @Test
    void premiumCaseIsAlwaysABetterCaseThanTheFreeOneOnTheSameTier() {
        for (var e : FREE_CASES.entrySet()) {
            String premium = PremiumPassRewards.forTier(e.getKey()).caseId();
            assertNotNull(premium, "tier " + e.getKey());
            assertTrue(CASE_LADDER.indexOf(premium) > CASE_LADDER.indexOf(e.getValue()),
                    "tier " + e.getKey() + ": premium " + premium + " must beat free " + e.getValue());
        }
    }

    @Test
    void premiumCasesSitOnTheFreeMilestones() {
        assertEquals("steam", PremiumPassRewards.forTier(5).caseId());
        assertEquals("flora", PremiumPassRewards.forTier(10).caseId());
        assertEquals("abyss", PremiumPassRewards.forTier(15).caseId());
        assertEquals("singularity", PremiumPassRewards.forTier(20).caseId());
        assertEquals("infinity", PremiumPassRewards.forTier(25).caseId());
        assertNull(PremiumPassRewards.forTier(4).caseId());
    }

    @Test
    void boostersAndPearlMultipliersArePlacedBetweenTheCases() {
        assertEquals(Tier.SMALL, PremiumPassRewards.forTier(3).booster());
        assertEquals(Tier.SMALL, PremiumPassRewards.forTier(6).booster());
        assertEquals(1, PremiumPassRewards.forTier(3).boosterCount());
        assertEquals(Tier.LARGE, PremiumPassRewards.forTier(14).booster());
        assertEquals("aquatech_ui:rate_x8", PremiumPassRewards.forTier(13).itemId());
        assertEquals("aquatech_ui:rate_x32", PremiumPassRewards.forTier(23).itemId());
        assertNull(PremiumPassRewards.forTier(2).itemId());
        assertNull(PremiumPassRewards.forTier(2).booster());
        assertNull(PremiumPassRewards.forTier(7).booster());
    }

    @Test
    void theFinalTierGivesTheTopCaseAndALargeBooster() {
        Reward last = PremiumPassRewards.forTier(PremiumPassRewards.MAX_TIER);
        assertEquals("infinity", last.caseId());
        assertEquals(Tier.LARGE, last.booster());
    }

    @Test
    void claimableTiersAreOnlyReachedUnclaimedOnes() {
        assertEquals(java.util.List.of(), PremiumPassRewards.claimable(0, java.util.Set.of()));
        assertEquals(java.util.List.of(1, 2, 3), PremiumPassRewards.claimable(3, java.util.Set.of()));
        assertEquals(java.util.List.of(2, 4), PremiumPassRewards.claimable(4, java.util.Set.of(1, 3)));
        assertEquals(PremiumPassRewards.MAX_TIER, PremiumPassRewards.claimable(99, java.util.Set.of()).size());
    }

    @Test
    void passTierNeverExceedsTheTwentyFiveLevelsTheHubDraws() {
        assertEquals(25, PremiumPassRewards.effectiveTier(40, 50));
        assertEquals(25, PremiumPassRewards.effectiveTier(26, 500));
        assertEquals(12, PremiumPassRewards.effectiveTier(12, 50));
    }

    @Test
    void configCanOnlyLowerThePassNotRaiseIt() {
        assertEquals(10, PremiumPassRewards.effectiveTier(20, 10));
        assertEquals(25, PremiumPassRewards.passMax(50));
        assertEquals(1, PremiumPassRewards.passMax(0));
    }

    @Test
    void tierIsAtLeastOne() {
        assertEquals(1, PremiumPassRewards.effectiveTier(0, 25));
        assertEquals(1, PremiumPassRewards.effectiveTier(-4, 25));
    }
}
