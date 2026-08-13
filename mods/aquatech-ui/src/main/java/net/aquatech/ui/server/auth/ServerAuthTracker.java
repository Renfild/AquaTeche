package net.aquatech.ui.server.auth;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.S2CSessionSyncPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = "aquatech_ui", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerAuthTracker {

    private static final int AUTH_TIMEOUT_TICKS = 200;
    private static final Map<UUID, Long> PENDING_AUTH = new ConcurrentHashMap<>();
    private static final Map<UUID, String> AUTHENTICATED_SESSIONS = new ConcurrentHashMap<>();
    private static final ExecutorService VERIFY_POOL = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "aquatech-auth");
        t.setDaemon(true);
        return t;
    });

    private ServerAuthTracker() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.getServer() != null && player.getServer().isSingleplayer()) {
            acceptLocal(player, "local_singleplayer");
            return;
        }

        PENDING_AUTH.put(player.getUUID(), System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            UUID uuid = event.getEntity().getUUID();
            PENDING_AUTH.remove(uuid);
            AUTHENTICATED_SESSIONS.remove(uuid);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_AUTH.isEmpty()) return;

        long now = System.currentTimeMillis();
        PENDING_AUTH.forEach((uuid, startTime) -> {
            if (now - startTime > (AUTH_TIMEOUT_TICKS * 50L)) {
                PENDING_AUTH.remove(uuid);
                if (!AUTHENTICATED_SESSIONS.containsKey(uuid)) {
                    kickUnauthenticatedPlayer(uuid);
                }
            }
        });
    }

    public static void onAuthPacketReceived(ServerPlayer player, String nick, String sessionToken) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        String token = sessionToken != null ? sessionToken.trim() : "";
        String packetNick = nick != null ? nick.trim() : player.getGameProfile().getName();

        if (player.getServer() != null && player.getServer().isSingleplayer()) {
            acceptLocal(player, token.isEmpty() ? "local_singleplayer" : token);
            return;
        }

        if (!ModConfig.REQUIRE_PORTAL_SESSION.get()) {
            acceptLocal(player, token.isEmpty() ? "local_unverified" : token);
            return;
        }

        if (token.length() < 8 || token.startsWith("aq_session_")) {
            PENDING_AUTH.remove(uuid);
            player.connection.disconnect(Component.literal(
                    "§c[AquaTech] Нет сессии портала.\n§7Войдите на aquateche.store и запустите игру через лаунчер."));
            return;
        }

        CompletableFuture.supplyAsync(() -> PortalSessionVerifier.verify(packetNick, token), VERIFY_POOL)
                .orTimeout(6, TimeUnit.SECONDS)
                .whenComplete((result, err) -> {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    if (server == null) return;
                    server.execute(() -> applyHttpResult(uuid, packetNick, token, result, err));
                });
    }

    private static void applyHttpResult(UUID uuid, String nick, String token, PortalSessionVerifier.Result result, Throwable err) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player == null || player.hasDisconnected()) {
            PENDING_AUTH.remove(uuid);
            return;
        }

        if (err != null || result == null || !result.ok()) {
            String why = err != null ? err.getClass().getSimpleName() : (result != null ? result.error() : "null");
            AquaTechUI.LOGGER.warn("Portal auth failed for {} ({}): {}", player.getGameProfile().getName(), uuid, why);
            PENDING_AUTH.remove(uuid);
            player.connection.disconnect(Component.literal(
                    "§c[AquaTech] Сессия недействительна.\n§7Войдите на aquateche.store и перезапустите лаунчер."));
            return;
        }

        PENDING_AUTH.remove(uuid);
        AUTHENTICATED_SESSIONS.put(uuid, token);
        AquaTechUI.LOGGER.info("Portal session ok for {} rank={} balance={}",
                player.getGameProfile().getName(), result.rankId(), result.balance());
        sendSessionSync(player, result.nick().isBlank() ? nick : result.nick(), token, result.balance(), result.rankId());
    }

    private static void acceptLocal(ServerPlayer player, String token) {
        UUID uuid = player.getUUID();
        PENDING_AUTH.remove(uuid);
        AUTHENTICATED_SESSIONS.put(uuid, token);
        sendSessionSync(player, player.getGameProfile().getName(), token, 0, "player");
    }

    private static void sendSessionSync(ServerPlayer player, String nick, String token, int balance, String rankId) {
        NetworkHandler.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new S2CSessionSyncPacket(nick, token, balance, rankId)
        );
    }

    private static void kickUnauthenticatedPlayer(UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null && !AUTHENTICATED_SESSIONS.containsKey(uuid)) {
                player.connection.disconnect(Component.literal(
                        "§c[AquaTech] Ошибка авторизации.\n§7Запустите игру через лаунчер AquaTech."));
            }
        });
    }

    public static boolean isAuthenticated(UUID uuid) {
        return AUTHENTICATED_SESSIONS.containsKey(uuid);
    }
}
