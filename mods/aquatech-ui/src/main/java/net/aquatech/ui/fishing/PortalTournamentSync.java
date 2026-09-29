package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.server.auth.PortalSessionVerifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Отправляет снимки турнира на портал в отдельном потоке; игровой поток никогда не ждёт сеть. */
public final class PortalTournamentSync {

    private static final long DEBOUNCE_MS = 3_000L;
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "aquatech-tournament-sync");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean SCHEDULED = new AtomicBoolean(false);
    private static final AtomicReference<Pending> LATEST = new AtomicReference<>();
    private static volatile int lastFinalizedWeek = -1;

    private record Pending(int week, String status, String json, String baseUrl) {
    }

    private PortalTournamentSync() {
    }

    public static String resolveBase() {
        String base = System.getProperty("aquatech.tournament.portalUrl");
        if (base == null || base.isBlank()) {
            base = ModConfig.AUTH_API_BASE.get();
        }
        if (base == null) {
            return "";
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    public static int lastFinalizedWeek() {
        return lastFinalizedWeek;
    }

    public static void push(int week, String status, String json, String baseUrl) {
        LATEST.set(new Pending(week, status, json, baseUrl));
        if (SCHEDULED.compareAndSet(false, true)) {
            EXEC.schedule(PortalTournamentSync::flush, DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        }
    }

    private static void flush() {
        SCHEDULED.set(false);
        Pending p = LATEST.getAndSet(null);
        if (p == null) {
            return;
        }
        String key = PortalSessionVerifier.readSyncKey();
        if (key == null || key.isBlank() || p.baseUrl().isBlank()) {
            AquaTechUI.LOGGER.debug("[tournament] portal sync skipped: no sync key or base url");
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(p.baseUrl() + "/api/sync/tournament"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("X-AquaTech-Server-Key", key)
                    .POST(HttpRequest.BodyPublishers.ofString(p.json()))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                if ("finalized".equals(p.status())) {
                    lastFinalizedWeek = p.week();
                }
            } else {
                AquaTechUI.LOGGER.warn("[tournament] portal rejected snapshot: HTTP {}", response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            AquaTechUI.LOGGER.warn("[tournament] portal sync failed: {}", e.toString());
        }
    }
}
