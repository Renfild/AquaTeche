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
}
