package net.aquatech.ui.client.render;

/**
 * Pure policy for which screens get world gaussian blur.
 * Kept free of Minecraft types so it can be asserted without a client runtime.
 */
public final class BlurScreenPolicy {

    public static final String AQUALUMEN_GUI_PREFIX = "store.aquateche.aqualumen.client.screen.";

    private BlurScreenPolicy() {
    }

    public static boolean shouldBlur(boolean aquaBlurredScreen, String className) {
        if (aquaBlurredScreen) {
            return true;
        }
        return className != null && className.startsWith(AQUALUMEN_GUI_PREFIX);
    }
}
