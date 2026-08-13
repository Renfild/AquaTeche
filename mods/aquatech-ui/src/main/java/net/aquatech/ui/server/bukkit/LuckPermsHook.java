package net.aquatech.ui.server.bukkit;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.server.ProfileSyncService;

import java.util.function.Consumer;

/**
 * Subscribes to LuckPerms user/group events so TAB ranks refresh immediately
 * when an admin runs /lp user ... parent set / promote / etc.
 *
 * On Mohist, LuckPerms lives in the Bukkit plugin classloader — plain
 * Class.forName from Forge cannot see {@code LuckPermsProvider}.
 */
public final class LuckPermsHook {
    private static boolean registered;

    private LuckPermsHook() {
    }

    public static void register() {
        if (registered) return;
        try {
            ClassLoader lpLoader = luckPermsClassLoader();
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider", true, lpLoader);
            Object api = provider.getMethod("get").invoke(null);
            Object bus = api.getClass().getMethod("getEventBus").invoke(api);

            subscribe(bus, lpLoader, "net.luckperms.api.event.user.UserDataRecalculateEvent");
            subscribeIfPresent(bus, lpLoader, "net.luckperms.api.event.user.track.UserPromoteEvent");
            subscribeIfPresent(bus, lpLoader, "net.luckperms.api.event.user.track.UserDemoteEvent");
            subscribeIfPresent(bus, lpLoader, "net.luckperms.api.event.node.NodeAddEvent");
            subscribeIfPresent(bus, lpLoader, "net.luckperms.api.event.node.NodeRemoveEvent");

            registered = true;
            AquaTechUI.LOGGER.info("[AquaTech] LuckPerms hook registered — TAB ranks refresh on group changes");
        } catch (Throwable t) {
            AquaTechUI.LOGGER.warn("[AquaTech] LuckPerms hook not available (TAB will poll every sync tick): {}",
                    t.toString());
        }
    }

    /** Prefer LuckPerms plugin ClassLoader (Bukkit bridge on Mohist). */
    private static ClassLoader luckPermsClassLoader() throws Exception {
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            Object pm = bukkit.getMethod("getPluginManager").invoke(null);
            Object plugin = pm.getClass().getMethod("getPlugin", String.class).invoke(pm, "LuckPerms");
            if (plugin != null) {
                return plugin.getClass().getClassLoader();
            }
        } catch (ClassNotFoundException ignored) {
            // pure Forge / no Bukkit bridge
        }
        return Thread.currentThread().getContextClassLoader();
    }

    private static void subscribeIfPresent(Object bus, ClassLoader loader, String eventClass) {
        try {
            subscribe(bus, loader, eventClass);
        } catch (Throwable ignored) {
        }
    }

    private static void subscribe(Object bus, ClassLoader loader, String eventClass) throws Exception {
        Class<?> clazz = Class.forName(eventClass, true, loader);
        Consumer<Object> handler = event -> ProfileSyncService.requestSync();
        bus.getClass()
                .getMethod("subscribe", Class.class, Consumer.class)
                .invoke(bus, clazz, handler);
    }
}
