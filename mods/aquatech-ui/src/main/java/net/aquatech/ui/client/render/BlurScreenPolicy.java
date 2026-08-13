package net.aquatech.ui.client.render;

/**
 * Pure policy for which screens get world gaussian blur.
 * Kept free of Minecraft types so it can be asserted without a client runtime.
 */
public final class BlurScreenPolicy {

    public static final String CASESMOD_GUI_PREFIX = "com.casesmod.client.gui.";

    private BlurScreenPolicy() {
    }

    public static boolean shouldBlur(boolean aquaBlurredScreen, String className) {
        if (aquaBlurredScreen) {
            return true;
        }
        return className != null && className.startsWith(CASESMOD_GUI_PREFIX);
    }
}
