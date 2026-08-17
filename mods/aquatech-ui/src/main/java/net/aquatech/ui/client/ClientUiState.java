package net.aquatech.ui.client;

import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.common.ServerStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientUiState {
    private static final Map<UUID, PlayerProfile> PROFILES = new LinkedHashMap<>();
    private static ServerStats stats = new ServerStats(0, 0, 0, 20.0F, "AquaTech Ocean Horizon", "bk.aquatech.net");
    private static int titleFrame;
    private static boolean tabOpen;
    /** Bumped when skill/horizon data is synced to client (HUD string cache). */
    private static int skillSyncGeneration;
    /** Session token relayed from server after backend auth validation. */
    private static String sessionToken = "";
    private static int sessionBalance;
    private static String sessionRankId = "player";
    /** Block limiter counts: blockRegistryId -> placed count */
    private static final Map<String, Integer> LIMITER_PLACED = new java.util.concurrent.ConcurrentHashMap<>();
    /** Block limiter maxima: blockRegistryId -> max allowed */
    private static final Map<String, Integer> LIMITER_MAX = new java.util.concurrent.ConcurrentHashMap<>();

    private ClientUiState() {
    }

    public static void setTabOpen(boolean open) {
        tabOpen = open;
    }

    public static boolean tabOpen() {
        return tabOpen;
    }

    public static void updateProfiles(List<PlayerProfile> profiles) {
        PROFILES.clear();
        for (PlayerProfile profile : profiles) {
            PROFILES.put(profile.uuid(), profile);
        }
    }

    public static void updateStats(ServerStats newStats) {
        stats = newStats;
    }

    public static List<PlayerProfile> profiles() {
        return Collections.unmodifiableList(new ArrayList<>(PROFILES.values()));
    }

    public static PlayerProfile profile(UUID uuid) {
        return PROFILES.get(uuid);
    }

    public static ServerStats stats() {
        return stats;
    }

    public static void tick() {
        titleFrame++;
    }

    public static int titleFrame() {
        return titleFrame;
    }

    public static void bumpSkillSyncGeneration() {
        skillSyncGeneration++;
    }

    public static int skillSyncGeneration() {
        return skillSyncGeneration;
    }

    public static void setSession(String token, int balance, String rankId) {
        sessionToken = token != null ? token : "";
        sessionBalance = Math.max(0, balance);
        sessionRankId = rankId != null && !rankId.isBlank() ? rankId : "player";
    }

    public static void setSessionToken(String token) {
        setSession(token, sessionBalance, sessionRankId);
    }

    public static String sessionToken() {
        return sessionToken;
    }

    public static int sessionBalance() {
        return sessionBalance;
    }

    public static String sessionRankId() {
        return sessionRankId;
    }

    public static void updateLimiters(java.util.Map<String, Integer> placed, java.util.Map<String, Integer> max) {
        LIMITER_PLACED.clear();
        LIMITER_PLACED.putAll(placed);
        LIMITER_MAX.clear();
        LIMITER_MAX.putAll(max);
    }

    public static int limiterPlaced(String blockId) {
        return LIMITER_PLACED.getOrDefault(blockId, 0);
    }

    public static int limiterMax(String blockId) {
        return LIMITER_MAX.getOrDefault(blockId, 0);
    }

    private static long sessionStartMillis = System.currentTimeMillis();

    public static void resetSessionTimer() {
        sessionStartMillis = System.currentTimeMillis();
    }

    public static String getPlaytimeFormatted() {
        long elapsedSec = Math.max(0, (System.currentTimeMillis() - sessionStartMillis) / 1000L);
        long hours = elapsedSec / 3600L;
        long mins = (elapsedSec % 3600L) / 60L;
        if (hours > 0) {
            return hours + " ч " + mins + " мин";
        } else {
            return Math.max(1, mins) + " мин";
        }
    }
}
