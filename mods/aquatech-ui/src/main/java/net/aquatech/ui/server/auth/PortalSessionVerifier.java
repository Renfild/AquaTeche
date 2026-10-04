package net.aquatech.ui.server.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;

import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Calls {@code POST /api/launcher/verify-token} on aquateche.store (D1 sessions). */
public final class PortalSessionVerifier {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** Сколько после успешной проверки сессия принимается, если портал недоступен (сетевой сбой, 5xx). */
    private static final long RECENT_OK_TTL_MS = 30L * 60_000L;
    private static final Map<String, Cached> RECENT_OK = new ConcurrentHashMap<>();

    private record Cached(Result result, long at) {
    }

    private PortalSessionVerifier() {
    }

    public record Result(boolean ok, String nick, int balance, String rankId, String error) {
        static Result fail(String error) {
            return new Result(false, "", 0, "player", error);
        }
    }

    public static Result verify(String nick, String sessionToken) {
        String base = ModConfig.AUTH_API_BASE.get();
        if (base == null || base.isBlank()) {
            return Result.fail("auth.apiBase empty");
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        JsonObject body = new JsonObject();
        body.addProperty("session", sessionToken);
        body.addProperty("nick", nick);

        String syncKey = readSyncKey();
        if (syncKey == null || syncKey.isBlank()) {
            return Result.fail("sync key missing");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(base + "/api/launcher/verify-token"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("X-AquaTech-Server-Key", syncKey)
                .header("x-aquatech-launcher", "1")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500 || response.statusCode() == 429) {
                return recentOrFail(nick, sessionToken, "HTTP " + response.statusCode());
            }
            if (response.statusCode() != 200) {
                return Result.fail("HTTP " + response.statusCode());
            }
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!json.has("ok") || !json.get("ok").getAsBoolean()) {
                return Result.fail("session rejected");
            }
            String verifiedNick = json.has("nick") ? json.get("nick").getAsString() : nick;
            int balance = json.has("balance") ? json.get("balance").getAsInt() : 0;
            String rank = json.has("rank_id") ? json.get("rank_id").getAsString() : "player";
            Result ok = new Result(true, verifiedNick, balance, rank == null || rank.isBlank() ? "player" : rank, null);
            RECENT_OK.put(cacheKey(nick, sessionToken), new Cached(ok, System.currentTimeMillis()));
            return ok;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.fail("interrupted");
        } catch (Exception e) {
            AquaTechUI.LOGGER.warn("[auth] verify-token failed: {}", e.toString());
            return recentOrFail(nick, sessionToken, e.getClass().getSimpleName());
        }
    }

    private static String cacheKey(String nick, String sessionToken) {
        return nick.toLowerCase(java.util.Locale.ROOT) + ":" + sessionToken;
    }

    /**
     * Портал не ответил (DNS, сброс соединения, 5xx), но этот же ник с этим же токеном недавно прошёл проверку:
     * пускаем, чтобы разовый сбой сети не выкидывал игрока при каждом перезаходе. Отказ портала сюда не попадает.
     */
    private static Result recentOrFail(String nick, String sessionToken, String error) {
        Cached cached = RECENT_OK.get(cacheKey(nick, sessionToken));
        if (cached != null && System.currentTimeMillis() - cached.at() <= RECENT_OK_TTL_MS) {
            AquaTechUI.LOGGER.warn("[auth] portal unreachable ({}), accepting a session verified {} s ago for {}",
                    error, (System.currentTimeMillis() - cached.at()) / 1000L, nick);
            return cached.result();
        }
        return Result.fail(error);
    }

    public static String readSyncKey() {
        File file = new File("config/aquatech_sync_key.json");
        if (!file.isFile()) {
            return null;
        }
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            if (!obj.has("key")) {
                return null;
            }
            String key = obj.get("key").getAsString();
            return key == null || key.isBlank() ? null : key;
        } catch (Exception e) {
            AquaTechUI.LOGGER.warn("[auth] cannot read aquatech_sync_key.json: {}", e.toString());
            return null;
        }
    }
}
