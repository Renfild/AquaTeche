package store.aquateche.aqualumen.client.web;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import store.aquateche.aqualumen.AquaLumenUI;

final class LumenCefHost {

    private LumenCefHost() {
    }

    static boolean ready() {
        try {
            return MCEF.isInitialized() || MCEF.initialize();
        } catch (Throwable error) {
            AquaLumenUI.LOGGER.warn("[AquaLumen CEF] initialization failed: {}", error.toString());
            return false;
        }
    }

    static MCEFBrowser create(String url, int pixelWidth, int pixelHeight) {
        if (!ready()) {
            return null;
        }
        try {
            return MCEF.createBrowser(url, true, Math.max(64, pixelWidth), Math.max(64, pixelHeight));
        } catch (Throwable error) {
            AquaLumenUI.LOGGER.warn("[AquaLumen CEF] browser creation failed: {}", error.toString());
            return null;
        }
    }
}
