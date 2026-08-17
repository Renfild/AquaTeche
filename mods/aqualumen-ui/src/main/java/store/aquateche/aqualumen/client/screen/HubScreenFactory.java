package store.aquateche.aqualumen.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModList;
import store.aquateche.aqualumen.AquaLumenUI;

public final class HubScreenFactory {

    private static final String WEB_SCREEN = "store.aquateche.aqualumen.client.screen.LumenWebScreen";

    private HubScreenFactory() {
    }

    public static Screen create(String initialTab) {
        if (!ModList.get().isLoaded("mcef")) {
            AquaLumenUI.LOGGER.info("[AquaLumen CEF] MCEF absent, using native hub");
            return new HubScreen();
        }
        try {
            Class<?> type = Class.forName(WEB_SCREEN);
            return (Screen) type.getConstructor(String.class).newInstance(initialTab);
        } catch (Throwable error) {
            AquaLumenUI.LOGGER.warn("[AquaLumen CEF] web screen failed, using native hub: {}", error.toString());
            return new HubScreen();
        }
    }
}
