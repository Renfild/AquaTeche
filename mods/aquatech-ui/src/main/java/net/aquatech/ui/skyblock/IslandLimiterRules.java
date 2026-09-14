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

    static {
        // Лимитируются ТОЛЬКО машины AquaTech: Механизмы — по 10 шт каждого на регион.
        // Остальное (воронки, IU, AE2, Botania, Draconic) не ограничено.
        put("aquatech_machines:fisher", 10, "Рыбак MK-2");
        put("aquatech_machines:excavator", 10, "Экскаватор");
        put("aquatech_machines:extractor", 10, "Экстрактор");
        put("aquatech_machines:synthesizer", 10, "Синтезатор");
        put("aquatech_machines:centrifuge", 10, "Центрифуга");
        put("aquatech_machines:flower_collector", 10, "Цветолов");
        put("aquatech_machines:mana_fabricator", 10, "Мана-Фабрикатор");
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
        return MAX.containsKey(id) ? id : null;
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
