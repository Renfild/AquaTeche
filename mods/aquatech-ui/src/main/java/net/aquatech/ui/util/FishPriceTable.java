package net.aquatech.ui.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Server-side view of config/aqualumen/fish_shop.json (owned by aqualumen-ui).
 * The Fish Smoker uses it to split junk fish (cheap) into fish meal instead of smoking.
 */
public final class FishPriceTable {

    public static final long JUNK_MAX_PRICE = 55L;

    private static volatile Map<String, Long> prices = Map.of();
    private static volatile long mtime = -1L;

    private FishPriceTable() {
    }

    public static boolean isJunkFish(String itemId) {
        refresh();
        Long price = prices.get(itemId);
        return price != null && price <= JUNK_MAX_PRICE;
    }

    private static void refresh() {
        try {
            Path file = FMLPaths.CONFIGDIR.get().resolve("aqualumen/fish_shop.json");
            if (!Files.exists(file)) return;
            long m = Files.getLastModifiedTime(file).toMillis();
            if (m == mtime) return;
            mtime = m;
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, Long> map = new HashMap<>();
            if (root.has("fishes")) {
                for (var entry : root.getAsJsonArray("fishes")) {
                    JsonObject f = entry.getAsJsonObject();
                    if (f.has("id") && f.has("priceCoins")) {
                        map.put(f.get("id").getAsString(), f.get("priceCoins").getAsLong());
                    }
                }
            }
            prices = Map.copyOf(map);
        } catch (Exception ignored) {
        }
    }
}
