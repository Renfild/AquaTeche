package net.aquatech.ui.fishing;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepChestLogicTest {

    @Test
    void rewardCoinsStayInTheFiveToTenThousandRangeInHundreds() {
        Random random = new Random(7);
        for (int i = 0; i < 2000; i++) {
            DeepChestLogic.Reward reward = DeepChestLogic.rollReward(random);
            assertTrue(reward.coins() >= 5000 && reward.coins() <= 10000, "coins " + reward.coins());
            assertEquals(0, reward.coins() % 100);
        }
    }

    @Test
    void caseKeyDropsAboutThirtyPercentOfTheTimeAndIsAlwaysAKnownCase() {
        Random random = new Random(11);
        int keys = 0;
        int rolls = 10000;
        for (int i = 0; i < rolls; i++) {
            String id = DeepChestLogic.rollReward(random).caseId();
            if (id != null) {
                keys++;
                assertTrue(id.equals("flora") || id.equals("steam") || id.equals("smeltery"), id);
            }
        }
        double share = keys / (double) rolls;
        assertTrue(share > 0.27 && share < 0.33, "share " + share);
    }

    @Test
    void nextDelayFallsInsideTheConfiguredWindow() {
        Random random = new Random(3);
        for (int i = 0; i < 500; i++) {
            long ms = DeepChestLogic.nextDelayMs(random, 120, 180);
            assertTrue(ms >= 120 * 60_000L && ms <= 180 * 60_000L, "ms " + ms);
        }
        assertEquals(60 * 60_000L, DeepChestLogic.nextDelayMs(random, 60, 60));
    }

    @Test
    void nextDelayToleratesAnInvertedWindow() {
        assertEquals(90 * 60_000L, DeepChestLogic.nextDelayMs(new Random(1), 90, 30));
    }

    @Test
    void compassUsesMinecraftAxesEastIsPlusXSouthIsPlusZ() {
        assertEquals("на восток", DeepChestLogic.compass(10, 0));
        assertEquals("на юг", DeepChestLogic.compass(0, 10));
        assertEquals("на запад", DeepChestLogic.compass(-10, 0));
        assertEquals("на север", DeepChestLogic.compass(0, -10));
        assertEquals("на юго-запад", DeepChestLogic.compass(-10, 10));
    }

    @Test
    void distanceIsRoundedHorizontalLength() {
        assertEquals(5, DeepChestLogic.distance(3, 4));
        assertEquals(0, DeepChestLogic.distance(0, 0));
    }

    @Test
    void timeLeftShowsMinutesAndPaddedSecondsAndNeverGoesNegative() {
        assertEquals("5:00", DeepChestLogic.timeLeft(300_000));
        assertEquals("0:07", DeepChestLogic.timeLeft(6_100));
        assertEquals("0:00", DeepChestLogic.timeLeft(-5));
    }

    @Test
    void lastWinnerMayNotClaimAgainWhileOthersAreOnline() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        assertFalse(DeepChestLogic.mayClaim(a, a, 2));
        assertTrue(DeepChestLogic.mayClaim(a, b, 2));
        assertTrue(DeepChestLogic.mayClaim(a, a, 1), "alone on the server is fine");
        assertTrue(DeepChestLogic.mayClaim(null, a, 3));
    }

    @Test
    void progressIsShareOfTimeLeftClampedToUnit() {
        assertEquals(1.0f, DeepChestLogic.progress(300_000, 300_000));
        assertEquals(0.5f, DeepChestLogic.progress(150_000, 300_000));
        assertEquals(0.0f, DeepChestLogic.progress(-1, 300_000));
        assertEquals(0.0f, DeepChestLogic.progress(10, 0));
    }

    @Test
    void noCaseIdWhenTheKeyRollMisses() {
        Random random = new Random(0) {
            @Override
            public double nextDouble() {
                return 0.99;
            }
        };
        assertNull(DeepChestLogic.rollReward(random).caseId());
    }

    @Test
    void smallBoosterDropsAboutAQuarterOfTheTime() {
        Random random = new Random(5);
        int hits = 0;
        int rolls = 10000;
        for (int i = 0; i < rolls; i++) {
            if (DeepChestLogic.rollBooster(random)) hits++;
        }
        double share = hits / (double) rolls;
        assertTrue(share > 0.22 && share < 0.28, "share " + share);
    }
}
