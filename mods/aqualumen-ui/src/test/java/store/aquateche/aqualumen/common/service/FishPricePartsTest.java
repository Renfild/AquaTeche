package store.aquateche.aqualumen.common.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FishPricePartsTest {

    @Test
    void unitPriceIsTheProductOfAllFactors() {
        FishShopConfig.PriceParts parts = new FishShopConfig.PriceParts(100, 4.0, 2.0, 1.0, 1.5, 1.2, 1.25, 1.0);
        assertEquals(100 * 4.0 * 2.0 * 1.0 * 1.5 * 1.2 * 1.25 * 1.0, parts.unitPrice(), 1e-9);
        assertEquals(1800L, parts.roundedUnitPrice());
    }

    @Test
    void aFishIsNeverWorthLessThanOneCoin() {
        assertEquals(1L, new FishShopConfig.PriceParts(0, 1, 1, 1, 1, 1, 1, 1).roundedUnitPrice());
    }

    @Test
    void descriptionListsOnlyFactorsThatChangeThePrice() {
        FishShopConfig.PriceParts plain = new FishShopConfig.PriceParts(10, 1, 1, 1, 1, 1, 1, 1);
        assertEquals("", plain.describe());
        String text = new FishShopConfig.PriceParts(10, 4.0, 1, 1, 1.5, 1, 1.6, 1).describe();
        assertTrue(text.contains("редкость ×4.00"), text);
        assertTrue(text.contains("спрос ×1.50"), text);
        assertTrue(text.contains("грейд ×1.60"), text);
        assertTrue(!text.contains("вес"), text);
    }
}
