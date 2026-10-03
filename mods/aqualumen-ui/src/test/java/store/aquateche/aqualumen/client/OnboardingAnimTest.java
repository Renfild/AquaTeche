package store.aquateche.aqualumen.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnboardingAnimTest {

    @Test
    void easingsStartAtZeroAndEndAtOne() {
        assertEquals(0.0, OnboardingAnim.easeOutCubic(0), 1e-9);
        assertEquals(1.0, OnboardingAnim.easeOutCubic(1), 1e-9);
        assertEquals(0.0, OnboardingAnim.easeOutBack(0), 1e-9);
        assertEquals(1.0, OnboardingAnim.easeOutBack(1), 1e-9);
    }

    @Test
    void easingsClampOutsideTheUnitRange() {
        assertEquals(0.0, OnboardingAnim.easeOutCubic(-3), 1e-9);
        assertEquals(1.0, OnboardingAnim.easeOutCubic(5), 1e-9);
    }

    @Test
    void cubicNeverGoesBackwards() {
        double prev = -1;
        for (int i = 0; i <= 100; i++) {
            double v = OnboardingAnim.easeOutCubic(i / 100.0);
            assertTrue(v >= prev, "step " + i);
            prev = v;
        }
    }

    @Test
    void backEasingOvershootsOnceBeforeSettling() {
        double peak = 0;
        for (int i = 0; i <= 100; i++) {
            peak = Math.max(peak, OnboardingAnim.easeOutBack(i / 100.0));
        }
        assertTrue(peak > 1.0 && peak < 1.2, "peak " + peak);
    }

    @Test
    void approachMovesTowardTheTargetWithoutOvershooting() {
        double v = 0;
        for (int i = 0; i < 20; i++) {
            double next = OnboardingAnim.approach(v, 1.0, 6.0, 0.05);
            assertTrue(next >= v && next <= 1.0);
            v = next;
        }
        assertTrue(v > 0.9);
        assertEquals(1.0, OnboardingAnim.approach(0.999, 1.0, 6.0, 1.0), 1e-9);
        assertEquals(0.0, OnboardingAnim.approach(0.3, 0.0, 6.0, 5.0), 1e-9);
    }

    @Test
    void toastIsVisibleOnlyWhileItLivesAndFadesAtTheEnd() {
        assertEquals(0.0, OnboardingAnim.toastAlpha(-10, 3500), 1e-9);
        assertEquals(1.0, OnboardingAnim.toastAlpha(1000, 3500), 1e-9);
        assertTrue(OnboardingAnim.toastAlpha(3300, 3500) < 1.0);
        assertEquals(0.0, OnboardingAnim.toastAlpha(3600, 3500), 1e-9);
    }
}
