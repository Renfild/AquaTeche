package store.aquateche.aqualumen.common.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GemExchangeTest {

    @Test
    void amountMustBeAWholeNumberInTheAllowedRange() {
        assertEquals(1, GemExchange.parseAmount("1"));
        assertEquals(50, GemExchange.parseAmount(" 50 "));
        assertEquals(GemExchange.MAX_PER_ACTION, GemExchange.parseAmount(String.valueOf(GemExchange.MAX_PER_ACTION)));
        assertEquals(0, GemExchange.parseAmount("0"));
        assertEquals(0, GemExchange.parseAmount("-5"));
        assertEquals(0, GemExchange.parseAmount(String.valueOf(GemExchange.MAX_PER_ACTION + 1)));
        assertEquals(0, GemExchange.parseAmount("много"));
        assertEquals(0, GemExchange.parseAmount(null));
    }

    @Test
    void largeTradesDoNotOverflowInt() {
        long perGem = 100_000_000L;
        assertEquals(1_000L * perGem, GemExchange.buyCost(1_000, perGem));
        assertTrue(GemExchange.sellPayout(1_000, perGem) > Integer.MAX_VALUE);
    }

    @Test
    void buyingThenSellingBackAtTheDefaultRatesAlwaysLosesCoins() {
        long buy = 10_000L;
        long sell = 5_000L;
        for (int gems : new int[]{1, 7, 100, 1000}) {
            assertTrue(GemExchange.sellPayout(gems, sell) < GemExchange.buyCost(gems, buy));
        }
    }
}
