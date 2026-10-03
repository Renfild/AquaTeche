package store.aquateche.aqualumen.common.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopRulesTest {

    @Test
    void ranksAreRecognisedByOfferIdAndByPlainName() {
        assertTrue(ShopRules.isRankId("rank.sailor"));
        assertTrue(ShopRules.isRankId("RANK.VIP"));
        assertTrue(ShopRules.isRankId("  legend "));
        assertTrue(ShopRules.isRankId("admiral"));
    }

    @Test
    void otherProductsAreNotRanks() {
        assertFalse(ShopRules.isRankId("gems.5"));
        assertFalse(ShopRules.isRankId("pass.premium"));
        assertFalse(ShopRules.isRankId("ae.silicon_press"));
        assertFalse(ShopRules.isRankId(""));
        assertFalse(ShopRules.isRankId(null));
    }

    @Test
    void premiumPassCostsAThousandGems() {
        assertEquals(1000L, ShopRules.PASS_PREMIUM_GEMS);
    }
}
