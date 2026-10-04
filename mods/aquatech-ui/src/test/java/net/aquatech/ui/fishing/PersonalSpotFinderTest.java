package net.aquatech.ui.fishing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonalSpotFinderTest {

    @Test
    void ringAroundARaftStartsOutsideItsClaim() {
        assertEquals(90, PersonalSpotFinder.ringMin(true, 70));
        assertEquals(120, PersonalSpotFinder.ringMin(true, 120));
    }

    @Test
    void ringWithoutARaftKeepsTheConfiguredMinimum() {
        assertEquals(70, PersonalSpotFinder.ringMin(false, 70));
    }

    @Test
    void aPlayerFarFromTheirRaftDoesNotGetASpot() {
        int home = PersonalSpotFinder.AT_HOME_BLOCKS;
        assertTrue(PersonalSpotFinder.withinRange(0, 0, home));
        assertTrue(PersonalSpotFinder.withinRange(home, 0, home));
        assertTrue(PersonalSpotFinder.withinRange(120, 120, home));
        assertTrue(!PersonalSpotFinder.withinRange(home, 1, home));
        assertTrue(!PersonalSpotFinder.withinRange(1255, 6668, home), "friend's raft is 6.7 km from ours");
    }

    @Test
    void ringIsNeverEmptyEvenWhenTheConfiguredMaximumIsBelowTheMinimum() {
        int min = PersonalSpotFinder.ringMin(true, 70);
        int max = PersonalSpotFinder.ringMax(min, 80);
        assertTrue(max > min, "max=" + max + " min=" + min);
        assertEquals(140, PersonalSpotFinder.ringMax(90, 140));
    }
}
