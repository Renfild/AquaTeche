package net.aquatech.ui.fishing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeyLogicTest {

    private static final long WEEK_START = 1_000_000_000_000L;
    private static final long WEEK = 7L * 24 * 3600_000L;
    private static final String DIM = "minecraft:overworld";

    private static LeyLogic.State week() {
        LeyLogic.State s = new LeyLogic.State();
        LeyLogic.startWeek(s, 202641, WEEK_START, WEEK_START + WEEK, 0.5);
        return s;
    }

    @Test
    void appearanceIsScheduledBetweenTuesdayAndSundayEvening() {
        LeyLogic.State early = new LeyLogic.State();
        LeyLogic.startWeek(early, 1, WEEK_START, WEEK_START + WEEK, 0.0);
        assertEquals(WEEK_START + 24L * 3600_000L, early.appearAtMs, "never on Monday");
        LeyLogic.State late = new LeyLogic.State();
        LeyLogic.startWeek(late, 1, WEEK_START, WEEK_START + WEEK, 1.0);
        assertTrue(late.appearAtMs + LeyLogic.WINDOW_MS <= WEEK_START + WEEK - 6L * 3600_000L,
                "the window must close before the week ends");
        LeyLogic.State clamped = new LeyLogic.State();
        LeyLogic.startWeek(clamped, 1, WEEK_START, WEEK_START + WEEK, 7.0);
        assertEquals(late.appearAtMs, clamped.appearAtMs);
    }

    @Test
    void appearsOnlyWhenDueAndSomeoneIsOnline() {
        LeyLogic.State s = week();
        assertFalse(LeyLogic.due(s, s.appearAtMs - 1, 5));
        assertFalse(LeyLogic.due(s, s.appearAtMs, 0), "nobody online: wait");
        assertTrue(LeyLogic.due(s, s.appearAtMs, 1));
        LeyLogic.appear(s, s.appearAtMs, DIM, 10, 20);
        assertFalse(LeyLogic.due(s, s.appearAtMs + 1, 1), "only once per week");
    }

    @Test
    void windowClosesAfterThirtyMinutes() {
        LeyLogic.State s = week();
        long t = s.appearAtMs;
        LeyLogic.appear(s, t, DIM, 0, 0);
        assertTrue(LeyLogic.active(s, t + LeyLogic.WINDOW_MS - 1));
        assertFalse(LeyLogic.active(s, t + LeyLogic.WINDOW_MS));
        assertTrue(LeyLogic.expired(s, t + LeyLogic.WINDOW_MS));
        assertFalse(LeyLogic.expired(s, t + 1000));
        LeyLogic.close(s);
        assertFalse(LeyLogic.expired(s, t + LeyLogic.WINDOW_MS + 1), "announced once");
        assertFalse(LeyLogic.due(s, t + LeyLogic.WINDOW_MS + 1, 3), "no second appearance this week");
    }

    @Test
    void biteNeedsZoneCleanRhythmAndLuck() {
        LeyLogic.State s = week();
        long t = s.appearAtMs;
        LeyLogic.appear(s, t, DIM, 100, 100);
        assertTrue(LeyLogic.canBite(s, t, DIM, 100, 100, 85, 0.19));
        assertFalse(LeyLogic.canBite(s, t, DIM, 100, 100, 84, 0.0), "sloppy rhythm");
        assertFalse(LeyLogic.canBite(s, t, DIM, 100, 100, 100, 0.20), "luck roll must be below the chance");
        assertFalse(LeyLogic.canBite(s, t, DIM, 100 + LeyLogic.RADIUS + 1, 100, 100, 0.0), "outside the zone");
        assertTrue(LeyLogic.canBite(s, t, DIM, 100 + LeyLogic.RADIUS, 100, 100, 0.0), "the border counts");
        assertFalse(LeyLogic.canBite(s, t, "minecraft:the_nether", 100, 100, 100, 0.0), "wrong dimension");
        assertFalse(LeyLogic.canBite(s, t + LeyLogic.WINDOW_MS, DIM, 100, 100, 100, 0.0), "window is over");
    }

    @Test
    void onlyOnePlayerGetsHimPerWeek() {
        LeyLogic.State s = week();
        long t = s.appearAtMs;
        LeyLogic.appear(s, t, DIM, 0, 0);
        LeyLogic.markCaught(s, "Ann", "uuid-a", 250_000);
        assertFalse(LeyLogic.canBite(s, t + 1, DIM, 0, 0, 100, 0.0));
        assertFalse(LeyLogic.active(s, t + 1));
        assertFalse(LeyLogic.expired(s, t + LeyLogic.WINDOW_MS + 1), "a caught Ley is not 'expired'");
        assertEquals("Ann", s.caughtBy);
    }

    @Test
    void newWeekResetsEverything() {
        LeyLogic.State s = week();
        LeyLogic.appear(s, s.appearAtMs, DIM, 5, 5);
        LeyLogic.markCaught(s, "Ann", "uuid-a", 250_000);
        LeyLogic.startWeek(s, 202642, WEEK_START + WEEK, WEEK_START + 2 * WEEK, 0.3);
        assertFalse(s.appeared);
        assertFalse(s.caught);
        assertEquals("", s.caughtBy);
        assertEquals(202642, s.week);
    }

    @Test
    void trophyWeightAndLengthStayInTheirRanges() {
        assertEquals(200_000, LeyLogic.rollGrams(0.0));
        assertEquals(320_000, LeyLogic.rollGrams(1.0));
        assertEquals(200_000, LeyLogic.rollGrams(-3));
        assertEquals(520, LeyLogic.rollCm(0.0));
        assertEquals(640, LeyLogic.rollCm(1.0));
    }
}
