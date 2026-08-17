package store.aquateche.aqualumen.client.render;

import net.minecraft.client.Minecraft;
import store.aquateche.aqualumen.config.LumenConfig;

/**
 * Frame rate independent animation helpers.
 *
 * <p>Everything here degrades to "instantly finished" when the player turns animations off in the
 * client config, so no call site needs an if-branch for the reduced motion case.
 */
public final class Anim {

    private Anim() {
    }

    public static boolean enabled() {
        try {
            return LumenConfig.CLIENT.animations.get();
        } catch (IllegalStateException configNotLoaded) {
            return true;
        }
    }

    /** Seconds elapsed since the previous frame. */
    public static float delta() {
        return Minecraft.getInstance().getDeltaFrameTime() / 20.0F;
    }

    /**
     * Exponential easing toward a target that behaves the same at 30 and 240 fps.
     * Speed 14 feels snappy, 8 feels soft, 4 feels lazy.
     */
    public static float approach(float current, float target, float speed, float delta) {
        if (!enabled()) {
            return target;
        }
        float factor = 1.0F - (float) Math.exp(-Math.max(0.0F, speed) * Math.max(0.0F, delta));
        return current + (target - current) * factor;
    }

    public static float clamp01(float value) {
        return value < 0.0F ? 0.0F : Math.min(value, 1.0F);
    }

    public static float easeOutCubic(float t) {
        float x = 1.0F - clamp01(t);
        return 1.0F - x * x * x;
    }

    public static float easeInOutCubic(float t) {
        float x = clamp01(t);
        return x < 0.5F ? 4.0F * x * x * x : 1.0F - (float) Math.pow(-2.0F * x + 2.0F, 3.0) / 2.0F;
    }

    /** Slight overshoot, used for badges and the case reveal. */
    public static float easeOutBack(float t) {
        float x = clamp01(t) - 1.0F;
        return 1.0F + 2.70158F * x * x * x + 1.70158F * x * x;
    }

    /** Smooth 0..1 breathing value. */
    public static float pulse(float time, float speed) {
        if (!enabled()) {
            return 1.0F;
        }
        return 0.5F + 0.5F * (float) Math.sin(time * speed);
    }

    /**
     * Per-item reveal progress. {@code overlap} is how much of the total timeline a single item
     * takes, so 1.0 means all items animate together and 0.35 gives a clear cascade.
     */
    public static float stagger(float progress, int index, int count, float overlap) {
        if (!enabled()) {
            return 1.0F;
        }
        if (count <= 1) {
            return clamp01(progress);
        }
        float window = Math.max(0.05F, Math.min(1.0F, overlap));
        float step = (1.0F - window) / (count - 1);
        return clamp01((progress - index * step) / window);
    }

    /** Multiplies the alpha already present in an ARGB color. */
    public static int fade(int color, float alpha) {
        int base = color >>> 24;
        int faded = Math.round(base * clamp01(alpha));
        return (faded << 24) | (color & 0x00FFFFFF);
    }

    /** Moving highlight position used by the shimmer on locked rewards. */
    public static float shimmer(float time, float column, float width) {
        if (!enabled()) {
            return 0.0F;
        }
        float head = (time * 0.6F) % (width + 24.0F) - 12.0F;
        float distance = Math.abs(column - head);
        return distance > 10.0F ? 0.0F : 1.0F - distance / 10.0F;
    }
}
