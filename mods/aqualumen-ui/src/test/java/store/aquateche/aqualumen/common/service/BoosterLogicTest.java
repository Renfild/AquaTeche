package store.aquateche.aqualumen.common.service;

import org.junit.jupiter.api.Test;
import store.aquateche.aqualumen.common.service.BoosterLogic.Activation;
import store.aquateche.aqualumen.common.service.BoosterLogic.Active;
import store.aquateche.aqualumen.common.service.BoosterLogic.Result;
import store.aquateche.aqualumen.common.service.BoosterLogic.Stock;
import store.aquateche.aqualumen.common.service.BoosterLogic.Tier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoosterLogicTest {

    private static final long NOW = 1_000_000L;

    @Test
    void tiersMatchTheDesign() {
        assertEquals(1.5, Tier.SMALL.mult());
        assertEquals(30L * 60_000L, Tier.SMALL.durationMs());
        assertEquals(2.0, Tier.LARGE.mult());
        assertEquals(15L * 60_000L, Tier.LARGE.durationMs());
    }

    @Test
    void parseAcceptsRussianAndEnglishNames() {
        assertSame(Tier.SMALL, BoosterLogic.parse("small"));
        assertSame(Tier.SMALL, BoosterLogic.parse("малый"));
        assertSame(Tier.SMALL, BoosterLogic.parse("  Малый "));
        assertSame(Tier.LARGE, BoosterLogic.parse("large"));
        assertSame(Tier.LARGE, BoosterLogic.parse("БОЛЬШОЙ"));
    }

    @Test
    void parseRejectsUnknownAndEmptyInput() {
        assertNull(BoosterLogic.parse("huge"));
        assertNull(BoosterLogic.parse(""));
        assertNull(BoosterLogic.parse(null));
    }

    @Test
    void activationSpendsOneBoosterAndStartsTheTimer() {
        Activation a = BoosterLogic.activate(new Stock(2, 1), Active.NONE, Tier.SMALL, NOW);
        assertEquals(Result.OK, a.result());
        assertEquals(new Stock(1, 1), a.stock());
        assertEquals(1.5, a.active().mult());
        assertEquals(NOW + 30L * 60_000L, a.active().until());
    }

    @Test
    void activatingWithEmptyStockChangesNothing() {
        Activation a = BoosterLogic.activate(new Stock(0, 3), Active.NONE, Tier.SMALL, NOW);
        assertEquals(Result.NO_STOCK, a.result());
        assertEquals(new Stock(0, 3), a.stock());
        assertEquals(Active.NONE, a.active());
    }

    @Test
    void secondBoosterIsRefusedWhileOneRunsAndNothingIsSpent() {
        Active running = new Active(1.5, NOW + 10 * 60_000L);
        Activation a = BoosterLogic.activate(new Stock(1, 1), running, Tier.LARGE, NOW);
        assertEquals(Result.ALREADY_ACTIVE, a.result());
        assertEquals(new Stock(1, 1), a.stock());
        assertEquals(running, a.active());
    }

    @Test
    void expiredBoosterDoesNotBlockTheNextOne() {
        Active expired = new Active(1.5, NOW - 1L);
        Activation a = BoosterLogic.activate(new Stock(0, 1), expired, Tier.LARGE, NOW);
        assertEquals(Result.OK, a.result());
        assertEquals(2.0, a.active().mult());
    }

    @Test
    void multiplierIsOneWhenNothingRuns() {
        assertEquals(1.0, BoosterLogic.multiplierAt(Active.NONE, NOW));
        assertEquals(1.0, BoosterLogic.multiplierAt(new Active(2.0, NOW), NOW));
        assertEquals(2.0, BoosterLogic.multiplierAt(new Active(2.0, NOW + 1L), NOW));
    }

    @Test
    void bonusIsTheExtraPartRoundedToACoin() {
        assertEquals(1067L, BoosterLogic.bonus(2133L, 1.5));
        assertEquals(500L, BoosterLogic.bonus(500L, 2.0));
        assertEquals(0L, BoosterLogic.bonus(500L, 1.0));
    }

    @Test
    void bonusNeverGoesNegativeOrOnNonPositiveSales() {
        assertEquals(0L, BoosterLogic.bonus(0L, 2.0));
        assertEquals(0L, BoosterLogic.bonus(-50L, 2.0));
        assertEquals(0L, BoosterLogic.bonus(1000L, 0.5));
    }

    @Test
    void remainingTimeIsShownAsMinutesAndSecondsRoundedUp() {
        assertEquals("30:00", BoosterLogic.formatRemaining(30L * 60_000L));
        assertEquals("29:48", BoosterLogic.formatRemaining(29L * 60_000L + 47_200L));
        assertEquals("0:01", BoosterLogic.formatRemaining(1L));
        assertEquals("0:00", BoosterLogic.formatRemaining(0L));
        assertEquals("0:00", BoosterLogic.formatRemaining(-5L));
    }

    @Test
    void activeReportsRemainingTime() {
        Active a = new Active(1.5, NOW + 5_000L);
        assertTrue(a.isActive(NOW));
        assertEquals(5_000L, a.remainingMs(NOW));
        assertFalse(a.isActive(NOW + 5_000L));
        assertEquals(0L, a.remainingMs(NOW + 9_000L));
    }

    @Test
    void stockCountsAndAddsPerTier() {
        Stock s = new Stock(1, 0).plus(Tier.LARGE, 2).plus(Tier.SMALL, 1);
        assertEquals(2, s.of(Tier.SMALL));
        assertEquals(2, s.of(Tier.LARGE));
        assertEquals(4, s.total());
    }
}
