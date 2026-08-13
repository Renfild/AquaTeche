package net.aquatech.ui.server.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Calls {@code POST /api/launcher/verify-token} on aquateche.store (D1 sessions). */
public final class PortalSessionVerifier {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(base + "/api/launcher/verify-token"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("x-aquatech-launcher", "1")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
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
            return new Result(true, verifiedNick, balance, rank == null || rank.isBlank() ? "player" : rank, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.fail("interrupted");
        } catch (Exception e) {
            AquaTechUI.LOGGER.warn("[auth] verify-token failed: {}", e.toString());
            return Result.fail(e.getClass().getSimpleName());
        }
    }
}
