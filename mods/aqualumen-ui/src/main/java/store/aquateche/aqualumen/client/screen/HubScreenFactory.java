package store.aquateche.aqualumen.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModList;
import store.aquateche.aqualumen.AquaLumenUI;

public final class HubScreenFactory {

    private static final String WEB_SCREEN = "store.aquateche.aqualumen.client.screen.LumenWebScreen";

    private HubScreenFactory() {
    }

    public static Screen create(String initialTab) {
        return create(initialTab, false);
    }

    public static Screen create(String initialTab, boolean standalone) {
        if (!ModList.get().isLoaded("mcef")) {
            AquaLumenUI.LOGGER.error("[AquaLumen CEF] MCEF absent, hub unavailable");
            return null;
        }
        try {
            Class<?> type = Class.forName(WEB_SCREEN);
            return (Screen) type.getConstructor(String.class, boolean.class).newInstance(initialTab, standalone);
        } catch (Throwable error) {
            AquaLumenUI.LOGGER.error("[AquaLumen CEF] web hub failed: {}", error.toString());
            return null;
        }
    }
}
