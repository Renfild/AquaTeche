package store.aquateche.aqualumen.client;

/** Easing helpers for the guide panel. Pure math, no Minecraft types. */
public final class OnboardingAnim {

    private static final double BACK = 1.70158;

    private OnboardingAnim() {
    }

    private static double clamp01(double t) {
        return Math.max(0.0, Math.min(1.0, t));
    }

    public static double easeOutCubic(double t) {
        double x = 1.0 - clamp01(t);
        return 1.0 - x * x * x;
    }

    /** Overshoots a little past 1 and settles: the slide-in feels springy. */
    public static double easeOutBack(double t) {
        double x = clamp01(t) - 1.0;
        return 1.0 + (BACK + 1.0) * x * x * x + BACK * x * x;
    }

    /** Exponential approach, frame-rate independent, never overshoots the target. */
    public static double approach(double current, double target, double rate, double dtSeconds) {
        double k = 1.0 - Math.exp(-rate * Math.max(0.0, dtSeconds));
        double next = current + (target - current) * k;
        return Math.abs(target - next) < 0.002 ? target : next;
    }

    /** 0 before the toast starts and after it ends, 1 in the middle, fading out over the last 400 ms. */
    public static double toastAlpha(long ageMs, long lifeMs) {
        if (ageMs < 0 || ageMs >= lifeMs) {
            return 0.0;
        }
        long fade = 400;
        return ageMs > lifeMs - fade ? (double) (lifeMs - ageMs) / fade : 1.0;
    }
}
