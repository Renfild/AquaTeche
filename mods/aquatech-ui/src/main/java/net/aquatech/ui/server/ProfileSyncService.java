package net.aquatech.ui.server;

import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.common.ServerStats;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.ChatBubblePacket;
import net.aquatech.ui.network.packet.SyncAllProfilesPacket;
import net.aquatech.ui.network.packet.SyncServerStatsPacket;
import net.aquatech.ui.server.bukkit.LuckPermsBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Periodic TAB/HUD profile sync + chat bubbles. */
@Mod.EventBusSubscriber(modid = "aquatech_ui")
public final class ProfileSyncService {
    private static ScheduledExecutorService scheduler;
    private static MinecraftServer server;
    private static long lastTickCount = -1L;
    private static long lastMeasureTime = -1L;
    private static float smoothedTps = 20.0f;
    private static int lastProfilesHash;
    private static int lastStatsHash;

    private ProfileSyncService() {
    }

    public static void start(MinecraftServer minecraftServer) {
        server = minecraftServer;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "aquatech-ui-sync");
            t.setDaemon(true);
            return t;
        });
        int periodMs = Math.max(5, ModConfig.SYNC_INTERVAL_TICKS.get()) * 50;
        // First run after login-ready window so we never hit a joining player mid-handshake.
        scheduler.scheduleAtFixedRate(ProfileSyncService::broadcastSafe,
                NetworkHandler.LOGIN_READY_DELAY_TICKS * 50L + 1000L,
                periodMs, TimeUnit.MILLISECONDS);
        lastProfilesHash = 0;
        lastStatsHash = 0;
    }

    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        server = null;
        lastProfilesHash = 0;
        lastStatsHash = 0;
    }

    public static void requestSync() {
        LuckPermsBridge.invalidateAllPlayers();
        lastProfilesHash = 0;
        lastStatsHash = 0;
        broadcastSafe();
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        LuckPermsBridge.invalidatePlayer(sp.getUUID());
        NetworkHandler.markJoined(sp);
        MinecraftServer s = sp.getServer();
        if (s != null) {
            s.tell(new TickTask(s.getTickCount() + NetworkHandler.LOGIN_READY_DELAY_TICKS, () -> {
                ProfileSyncService.requestSync();
                if (sp.level() instanceof net.minecraft.server.level.ServerLevel level) {
                    net.aquatech.ui.skyblock.IslandLimiterTracker.get(level).syncTo(sp);
                }
            }));
        }
    }

    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            NetworkHandler.markLeft(sp.getUUID());
            LuckPermsBridge.invalidatePlayer(sp.getUUID());
            requestSync();
        }
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        String text = event.getRawText();
        if (text == null || text.isBlank()) return;
        var server = event.getPlayer().getServer();
        if (server == null) return;
        int duration = ModConfig.BUBBLE_DURATION_TICKS.get();
        NetworkHandler.sendToPlayReady(
                new ChatBubblePacket(event.getPlayer().getUUID(), text, duration),
                server.getPlayerList().getPlayers());
    }

    private static void broadcastSafe() {
        MinecraftServer s = server != null ? server : ServerLifecycleHooks.getCurrentServer();
        if (s == null || !s.isRunning()) return;
        s.execute(ProfileSyncService::broadcastNow);
    }

    private static void broadcastNow() {
        MinecraftServer s = server != null ? server : ServerLifecycleHooks.getCurrentServer();
        if (s == null) return;

        try {
            List<PlayerProfile> profiles = new ArrayList<>();
            int staffOnline = 0;
            for (ServerPlayer player : s.getPlayerList().getPlayers()) {
                try {
                    PlayerProfile profile = LuckPermsBridge.fromServerPlayer(player);
                    profiles.add(profile);
                    if (profile.staff()) staffOnline++;
                } catch (Exception e) {
                    // Never kick a player because TAB/rank sync failed.
                }
            }
            profiles.sort(Comparator
                    .comparingInt(PlayerProfile::rankWeight).reversed()
                    .thenComparing(p -> p.name().toLowerCase(Locale.ROOT)));

            SyncAllProfilesPacket packet = new SyncAllProfilesPacket(profiles);
            NetworkHandler.sendToPlayReady(packet, s.getPlayerList().getPlayers());

            long tickCount = s.getTickCount();
            long now = System.currentTimeMillis();
            if (lastTickCount >= 0 && lastMeasureTime >= 0) {
                long dtMs = now - lastMeasureTime;
                long dTicks = tickCount - lastTickCount;
                if (dtMs > 0) {
                    smoothedTps = (float) Math.min(20.0, dTicks * 1000.0 / dtMs);
                }
            }
            lastTickCount = tickCount;
            lastMeasureTime = now;

            ServerStats stats = new ServerStats(
                    s.getPlayerCount(),
                    s.getMaxPlayers(),
                    staffOnline,
                    smoothedTps,
                    ModConfig.SERVER_NAME.get(),
                    ModConfig.SERVER_DOMAIN.get()
            );
            SyncServerStatsPacket statsPacket = new SyncServerStatsPacket(stats);
            NetworkHandler.sendToPlayReady(statsPacket, s.getPlayerList().getPlayers());
        } catch (Exception ignored) {
            // Profile sync must never take down login.
        }
    }
}
