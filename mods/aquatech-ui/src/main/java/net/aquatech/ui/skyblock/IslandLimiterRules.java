package net.aquatech.ui.skyblock;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Caps on /is WorldGuard regions (island_*) and personal rafts.
 * Sized for a full factory, not a 4-machine starter hut.
 */
public final class IslandLimiterRules {

    private static final Map<String, Integer> MAX = new LinkedHashMap<>();
    private static final Map<String, String> TITLE = new LinkedHashMap<>();
    private static final Map<String, String> NS_CAT = new LinkedHashMap<>();

    static {
        put("aquatech_ui:auto_fisher", 8, "Авто-рыболов");
        put("aquatech_ui:ocean_filter", 8, "Ботанический экстрактор");
        put("aquatech_ui:seabed_dredger", 4, "Дноуглубитель");
        put("aquatech_ui:ocean_altar", 1, "Алтарь океана");
        put("aquatech_ui:abyssal_portal", 1, "Бездонный портал");
        put("minecraft:hopper", 64, "Воронка");

        put("cat:iu", 96, "Industrial Upgrade");
        put("cat:ae2", 48, "AE2");
        put("cat:de", 12, "Draconic Evolution");
        put("cat:botania", 24, "Botania");

        NS_CAT.put("industrialupgrade", "cat:iu");
        NS_CAT.put("ae2", "cat:ae2");
        NS_CAT.put("draconicevolution", "cat:de");
        NS_CAT.put("botania", "cat:botania");
        NS_CAT.put("mythicbotany", "cat:botania");
        NS_CAT.put("botanicalmachinery", "cat:botania");
    }

    private IslandLimiterRules() {
    }

    private static void put(String id, int max, String title) {
        MAX.put(id, max);
        TITLE.put(id, title);
    }

    public static String keyFor(BlockState state) {
        if (state == null) {
            return null;
        }
        return keyFor(state.getBlock(), state.hasBlockEntity());
    }

    public static String keyFor(Block block, boolean hasBlockEntity) {
        if (block == null) {
            return null;
        }
        ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(block);
        if (loc == null) {
            return null;
        }
        String id = loc.toString();
        if (MAX.containsKey(id)) {
            return id;
        }
        if (!hasBlockEntity) {
            return null;
        }
        return NS_CAT.get(loc.getNamespace());
    }

    public static boolean isLimited(String key) {
        return key != null && MAX.containsKey(key);
    }

    public static int max(String key) {
        return MAX.getOrDefault(key, 0);
    }

    public static String title(String key) {
        return TITLE.getOrDefault(key, key == null ? "" : key);
    }

    public static Map<String, Integer> allMax() {
        return Map.copyOf(MAX);
    }
}
