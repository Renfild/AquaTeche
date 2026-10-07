package net.aquatech.ui.fishing;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LeyFightTest {

    private static final int MAX_TICKS = 20 * 60 * 15;

    /** Аккуратный игрок: мотает до 70, отпускает до 45, на рывке только тянет в обратную сторону. */
    private static LeyFight.State play(long seed, double reelCeiling, boolean counterSurges) {
        Random rng = new Random(seed);
        LeyFight.State s = LeyFight.start(rng);
        boolean cooling = false;
        int ticks = 0;
        while (s.result == LeyFight.Result.NONE && ticks++ < MAX_TICKS) {
            if (s.tension > reelCeiling) {
                cooling = true;
            } else if (s.tension < 45.0) {
                cooling = false;
            }
            boolean surge = s.phase != LeyFight.Phase.CALM;
            boolean reel = !surge && !cooling;
            int steer = surge && counterSurges ? -s.surgeDir : 0;
            LeyFight.step(s, reel, steer, rng);
        }
        return s;
    }

    private static int ticksToWin(long seed) {
        Random rng = new Random(seed);
        LeyFight.State s = LeyFight.start(rng);
        boolean cooling = false;
        int ticks = 0;
        while (s.result == LeyFight.Result.NONE && ticks < MAX_TICKS) {
            ticks++;
            if (s.tension > 70.0) {
                cooling = true;
            } else if (s.tension < 45.0) {
                cooling = false;
            }
            boolean surge = s.phase != LeyFight.Phase.CALM;
            LeyFight.step(s, !surge && !cooling, surge ? -s.surgeDir : 0, rng);
        }
        return ticks;
    }

    @Test
    void carefulPlayerWinsInAboutTwoToFourMinutes() {
        for (long seed = 1; seed <= 40; seed++) {
            LeyFight.State s = play(seed, 70.0, true);
            assertEquals(LeyFight.Result.WON, s.result, "seed " + seed);
            int seconds = ticksToWin(seed) / 20;
            assertTrue(seconds >= 90 && seconds <= 240, "seed " + seed + ": " + seconds + " с");
        }
    }

    @Test
    void ignoringSurgesSnapsTheLine() {
        for (long seed = 1; seed <= 20; seed++) {
            assertEquals(LeyFight.Result.SNAPPED, play(seed, 70.0, false).result, "seed " + seed);
        }
    }

    @Test
    void holdingTheReelWithoutPausesSnapsTheLine() {
        Random rng = new Random(3);
        LeyFight.State s = LeyFight.start(rng);
        for (int i = 0; i < 2000 && s.result == LeyFight.Result.NONE; i++) {
            LeyFight.step(s, true, s.phase == LeyFight.Phase.SURGE ? -s.surgeDir : 0, rng);
        }
        assertEquals(LeyFight.Result.SNAPPED, s.result);
    }

    @Test
    void doingNothingLetsTheFishDrift() {
        Random rng = new Random(5);
        LeyFight.State s = LeyFight.start(rng);
        int ticks = 0;
        while (s.result == LeyFight.Result.NONE && ticks++ < MAX_TICKS) {
            LeyFight.step(s, false, 0, rng);
        }
        assertNotEquals(LeyFight.Result.WON, s.result);
        assertEquals(100.0, s.stamina, 1e-9);
    }

    @Test
    void surgeIsAnnouncedBeforeItStarts() {
        Random rng = new Random(9);
        LeyFight.State s = LeyFight.start(rng);
        int warnTicks = 0;
        while (s.phase != LeyFight.Phase.SURGE) {
            LeyFight.step(s, false, 0, rng);
            if (s.phase == LeyFight.Phase.WARN) {
                warnTicks++;
                assertTrue(s.surgeDir == -1 || s.surgeDir == 1);
            }
        }
        assertTrue(warnTicks >= 20, "предупреждение " + warnTicks + " тиков");
    }

    @Test
    void finishedFightIgnoresFurtherSteps() {
        Random rng = new Random(1);
        LeyFight.State s = LeyFight.start(rng);
        s.result = LeyFight.Result.WON;
        double tension = s.tension;
        LeyFight.step(s, true, 1, rng);
        assertEquals(tension, s.tension, 0.0);
    }
}
