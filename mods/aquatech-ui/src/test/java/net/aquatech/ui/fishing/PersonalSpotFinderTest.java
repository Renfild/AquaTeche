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
    void ringIsNeverEmptyEvenWhenTheConfiguredMaximumIsBelowTheMinimum() {
        int min = PersonalSpotFinder.ringMin(true, 70);
        int max = PersonalSpotFinder.ringMax(min, 80);
        assertTrue(max > min, "max=" + max + " min=" + min);
        assertEquals(140, PersonalSpotFinder.ringMax(90, 140));
    }
}
