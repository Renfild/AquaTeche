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
    void premiumCoinsMatchTheFreeTrackSoPremiumDoublesTheIncome() {
        assertEquals(2500L, PremiumPassRewards.forTier(1).coins());
        assertEquals(12000L, PremiumPassRewards.forTier(10).coins());
        assertEquals(100000L, PremiumPassRewards.forTier(25).coins());
    }

    @Test
    void caseRewardsSitOnTheSameMilestonesAsTheFreeTrackButOneStepUp() {
        assertEquals("smeltery", PremiumPassRewards.forTier(5).caseId());
        assertEquals("steam", PremiumPassRewards.forTier(10).caseId());
        assertEquals("flora", PremiumPassRewards.forTier(15).caseId());
        assertEquals("abyss", PremiumPassRewards.forTier(20).caseId());
        assertEquals("singularity", PremiumPassRewards.forTier(25).caseId());
        assertNull(PremiumPassRewards.forTier(4).caseId());
    }

    @Test
    void boostersAndPearlMultipliersArePlacedBetweenTheCases() {
        assertEquals(Tier.SMALL, PremiumPassRewards.forTier(3).booster());
        assertEquals(1, PremiumPassRewards.forTier(3).boosterCount());
        assertEquals(Tier.LARGE, PremiumPassRewards.forTier(14).booster());
        assertEquals("aquatech_ui:rate_x8", PremiumPassRewards.forTier(13).itemId());
        assertEquals("aquatech_ui:rate_x32", PremiumPassRewards.forTier(23).itemId());
        assertNull(PremiumPassRewards.forTier(2).itemId());
        assertNull(PremiumPassRewards.forTier(2).booster());
    }

    @Test
    void theFinalTierGivesTheTopCaseAndALargeBooster() {
        Reward last = PremiumPassRewards.forTier(PremiumPassRewards.MAX_TIER);
        assertEquals("singularity", last.caseId());
        assertEquals(Tier.LARGE, last.booster());
    }

    @Test
    void claimableTiersAreOnlyReachedUnclaimedOnes() {
        assertEquals(java.util.List.of(), PremiumPassRewards.claimable(0, java.util.Set.of()));
        assertEquals(java.util.List.of(1, 2, 3), PremiumPassRewards.claimable(3, java.util.Set.of()));
        assertEquals(java.util.List.of(2, 4), PremiumPassRewards.claimable(4, java.util.Set.of(1, 3)));
        assertEquals(PremiumPassRewards.MAX_TIER, PremiumPassRewards.claimable(99, java.util.Set.of()).size());
    }
}
