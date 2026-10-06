package net.aquatech.ui.client;

import net.aquatech.ui.AquaTechUI;

import java.lang.reflect.Method;

/** Клиентская часть пчеловода. С aqualumen нет compile-time связи, поэтому вызов идёт через reflection. */
public final class BeeKeeperClient {

    private static final String LUMEN_CLIENT = "store.aquateche.aqualumen.client.LumenClient";
    private static final String BEE_TAB = "bees";

    private BeeKeeperClient() {
    }

    public static void openBeeMenu() {
        try {
            Method openStandalone = Class.forName(LUMEN_CLIENT).getMethod("openStandalone", String.class);
            openStandalone.invoke(null, BEE_TAB);
        } catch (ReflectiveOperationException | LinkageError e) {
            AquaTechUI.LOGGER.warn("Bee keeper: aqualumen hub is unavailable, menu not opened: {}", e.toString());
        }
    }
}
