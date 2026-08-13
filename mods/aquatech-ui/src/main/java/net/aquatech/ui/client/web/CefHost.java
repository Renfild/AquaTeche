package net.aquatech.ui.client.web;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import net.aquatech.ui.AquaTechUI;

/** Direct CinemaMod MCEF 2.1.6 calls. Loaded only after {@code Class.forName("com.cinemamod.mcef.MCEF")}. */
public final class CefHost {

    private CefHost() {
    }

    public static boolean ready() {
        if (MCEF.isInitialized()) {
            return true;
        }
        try {
            return MCEF.initialize();
        } catch (Exception e) {
            AquaTechUI.LOGGER.warn("[cef] initialize failed: {}", e.toString());
            return false;
        }
    }

    public static MCEFBrowser create(String url, int pixelW, int pixelH) {
        if (!ready()) {
            return null;
        }
        return MCEF.createBrowser(url, true, Math.max(64, pixelW), Math.max(64, pixelH));
    }
}
