package net.aquatech.ui.skyblock;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-island caps for AquaTech machines. Ids must match Forge registry names.
 */
public final class IslandLimiterRules {

    private static final Map<String, Integer> MAX = new LinkedHashMap<>();
    private static final Map<String, String> TITLE = new LinkedHashMap<>();

    static {
        put("aquatech_ui:auto_fisher", 4, "Авто-рыболов");
        put("aquatech_ui:ocean_filter", 4, "Ботанический экстрактор");
        put("aquatech_ui:seabed_dredger", 2, "Дноуглубитель");
        put("aquatech_ui:ocean_altar", 1, "Алтарь океана");
        put("aquatech_ui:abyssal_portal", 1, "Бездонный портал");
    }

    private IslandLimiterRules() {
    }

    private static void put(String id, int max, String title) {
        MAX.put(id, max);
        TITLE.put(id, title);
    }

    public static boolean isLimited(String blockId) {
        return blockId != null && MAX.containsKey(blockId);
    }

    public static int max(String blockId) {
        return MAX.getOrDefault(blockId, 0);
    }

    public static String title(String blockId) {
        return TITLE.getOrDefault(blockId, blockId == null ? "" : blockId);
    }

    public static Map<String, Integer> allMax() {
        return Map.copyOf(MAX);
    }
}
