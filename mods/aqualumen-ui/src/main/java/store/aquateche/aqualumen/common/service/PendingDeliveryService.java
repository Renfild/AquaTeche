package store.aquateche.aqualumen.common.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import store.aquateche.aqualumen.AquaLumenUI;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/** Pulls D1 pending_commands for online players and fulfills them on the main thread. */
public final class PendingDeliveryService {

    private static final AtomicBoolean RUNNING = new AtomicBoolean();
    private static int tick;

    private PendingDeliveryService() {
    }

    public static void onServerTick(MinecraftServer server) {
        tick++;
        if (tick % 100 != 0 || !RUNNING.compareAndSet(false, true)) {
            return;
        }
        String key = HubDataService.resolveSyncKey();
        if (key == null) {
            RUNNING.set(false);
            return;
        }
        List<UUID> online = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            online.add(player.getUUID());
        }
        CompletableFuture.runAsync(() -> {
            try {
                for (UUID id : online) {
                    ServerPlayer player = server.getPlayerList().getPlayer(id);
                    if (player == null) {
                        continue;
                    }
                    pollOne(server, player, player.getGameProfile().getName(), key);
                }
            } finally {
                RUNNING.set(false);
            }
        });
    }

    private static void pollOne(MinecraftServer server, ServerPlayer player, String nick, String key) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    "https://aquateche.store/api/internal/pending-commands?nick="
                            + java.net.URLEncoder.encode(nick, StandardCharsets.UTF_8)
            ).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-AquaTech-Server-Key", key);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(4000);
            if (conn.getResponseCode() != 200) {
                return;
            }
            JsonObject res = JsonParser.parseReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            conn.disconnect();
            if (!res.has("commands")) {
                return;
            }
            JsonArray commands = res.getAsJsonArray("commands");
            for (int i = 0; i < commands.size(); i++) {
                JsonObject row = commands.get(i).getAsJsonObject();
                int id = row.get("id").getAsInt();
                String kind = row.has("kind") ? row.get("kind").getAsString() : "";
                String payload = row.has("payload") ? row.get("payload").getAsString() : "";
                server.execute(() -> deliver(player, key, id, kind, payload));
            }
        } catch (Throwable t) {
            AquaLumenUI.LOGGER.debug("pending-commands poll: {}", t.toString());
        }
    }

    private static void deliver(ServerPlayer player, String key, int id, String kind, String payload) {
        if (player == null || !player.isAlive()) {
            return;
        }
        boolean ok = StoreCatalog.fulfill(player, kind, payload);
        ack(key, id, ok);
        if (ok) {
            player.sendSystemMessage(Component.literal("Доставка с сайта: " + kind)
                    .withStyle(ChatFormatting.GREEN));
            HubDataService.push(player);
        }
    }

    private static void ack(String key, int id, boolean ok) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    "https://aquateche.store/api/internal/pending-commands"
            ).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("X-AquaTech-Server-Key", key);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            JsonObject body = new JsonObject();
            body.addProperty("op", ok ? "ack" : "fail");
            body.addProperty("id", id);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Throwable ignored) {
        }
    }
}
