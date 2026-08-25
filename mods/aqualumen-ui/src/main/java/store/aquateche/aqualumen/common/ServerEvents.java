package store.aquateche.aqualumen.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.common.command.LumenCommands;
import store.aquateche.aqualumen.common.service.HubActionHandler;
import store.aquateche.aqualumen.common.service.HubDataService;
import store.aquateche.aqualumen.common.service.HubEconomy;
import store.aquateche.aqualumen.config.LumenConfig;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server side glue. Tracks which players actually run the client mod, so the server can decide
 * between the rich hub screen and the chest fallback used by vanilla clients on Mohist.
 */
@Mod.EventBusSubscriber(modid = AquaLumenUI.MODID)
public final class ServerEvents {

    private static final Set<UUID> MODDED_CLIENTS = ConcurrentHashMap.newKeySet();
    private static int tickCounter;

    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LumenCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        MODDED_CLIENTS.remove(id);
        HubActionHandler.forget(id);
        HubDataService.closeFor(id);
    }

    /** Periodic refresh for players with an open hub + portal stat sync. */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        int interval = LumenConfig.COMMON.snapshotIntervalTicks.get();
        if (tickCounter % interval == 0) {
            HubDataService.refreshOpenHubs(event.getServer());
        }
        // Portal sync lives here: the KubeJS sandbox blocks java.lang.Thread, so HTTP must come from the mod.
        if (tickCounter > 0 && tickCounter % 3000 == 0) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                HubEconomy.coins(player); // main-thread wallet warm-up (legacy import) before the async read
                HubDataService.syncPlayerToWebAsync(player);
            }
        }
        tickCounter++;
    }

    public static void markModded(ServerPlayer player) {
        MODDED_CLIENTS.add(player.getUUID());
    }

    public static boolean hasClientMod(ServerPlayer player) {
        if (MODDED_CLIENTS.contains(player.getUUID())) {
            return true;
        }
        if (player.connection != null && player.connection.connection != null) {
            try {
                if (store.aquateche.aqualumen.network.LumenNetwork.CHANNEL.isRemotePresent(player.connection.connection)) {
                    MODDED_CLIENTS.add(player.getUUID());
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }
}
