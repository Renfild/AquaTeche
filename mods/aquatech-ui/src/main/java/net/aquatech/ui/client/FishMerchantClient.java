package net.aquatech.ui.client;

import net.aquatech.ui.AquaTechUI;

import java.lang.reflect.Method;

/** Клиентская часть скупщика. С aqualumen нет compile-time связи, поэтому вызов идёт через reflection. */
public final class FishMerchantClient {

    private static final String LUMEN_CLIENT = "store.aquateche.aqualumen.client.LumenClient";
    private static final String FISH_TAB = "fishing";

    private FishMerchantClient() {
    }

    public static void openFishMenu() {
        try {
            Method openScreen = Class.forName(LUMEN_CLIENT).getMethod("openScreen", String.class);
            openScreen.invoke(null, FISH_TAB);
        } catch (ReflectiveOperationException | LinkageError e) {
            AquaTechUI.LOGGER.warn("Fish merchant: aqualumen hub is unavailable, menu not opened: {}", e.toString());
        }
    }
}
