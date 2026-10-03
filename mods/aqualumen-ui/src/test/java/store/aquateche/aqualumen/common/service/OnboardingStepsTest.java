package store.aquateche.aqualumen.common.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OnboardingStepsTest {

    @Test
    void thereAreFiveStepsNumberedFromOne() {
        assertEquals(5, OnboardingSteps.COUNT);
        for (int id = 1; id <= OnboardingSteps.COUNT; id++) {
            OnboardingSteps.Step step = OnboardingSteps.get(id);
            assertNotNull(step, "step " + id);
            assertEquals(id, step.id());
            assertFalse(step.title().isBlank());
            assertFalse(step.hint().isBlank());
            assertFalse(step.goal() < 1);
        }
    }

    @Test
    void theKitComesBeforeFishingAndFishingCountsThreeFish() {
        assertEquals("Забери стартовый набор", OnboardingSteps.get(1).title());
        assertEquals(1, OnboardingSteps.get(1).goal());
        assertEquals("Поймай 3 рыбы", OnboardingSteps.get(2).title());
        assertEquals(3, OnboardingSteps.get(2).goal());
    }

    @Test
    void stepsOutsideTheRangeDoNotExist() {
        assertNull(OnboardingSteps.get(0));
        assertNull(OnboardingSteps.get(OnboardingSteps.COUNT + 1));
        assertNull(OnboardingSteps.get(-4));
    }

    @Test
    void progressFractionStaysInsideZeroAndOne() {
        assertEquals(0.0, OnboardingSteps.fraction(0, 3), 1e-9);
        assertEquals(2.0 / 3.0, OnboardingSteps.fraction(2, 3), 1e-9);
        assertEquals(1.0, OnboardingSteps.fraction(7, 3), 1e-9);
        assertEquals(0.0, OnboardingSteps.fraction(-2, 3), 1e-9);
        assertEquals(1.0, OnboardingSteps.fraction(0, 0), 1e-9);
    }

    @Test
    void colourCodesAreStrippedFromRewardText() {
        assertEquals("+500 монет", OnboardingSteps.plain("§6+500 монет"));
        assertEquals("малый бустер (включить: /booster)", OnboardingSteps.plain("§6малый бустер §7(включить: /booster)"));
        assertEquals("", OnboardingSteps.plain(null));
    }

    @Test
    void keyLabelIsTheLatinPhysicalKeyNotTheLayoutLetter() {
        assertEquals("Y", OnboardingSteps.keyLabel("key.keyboard.y", "Н"));
        assertEquals("H", OnboardingSteps.keyLabel("key.keyboard.h", "Р"));
        assertEquals("F6", OnboardingSteps.keyLabel("key.keyboard.f6", "F6"));
        assertEquals("4", OnboardingSteps.keyLabel("key.mouse.4", "Button 4"));
    }

    @Test
    void longKeyNamesFallBackToTheTranslatedName() {
        assertEquals("Левый Shift", OnboardingSteps.keyLabel("key.keyboard.left.shift", "Левый Shift"));
        assertEquals("Пробел", OnboardingSteps.keyLabel("key.keyboard.space", "Пробел"));
        assertEquals("?", OnboardingSteps.keyLabel(null, "?"));
    }
}
