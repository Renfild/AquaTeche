package net.aquatech.ui.fishing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Пер-тировой ростер рыбы: каждая удочка ловит свой набор видов по редкости.
 * Ростер кумулятивный — старшие тира ловят всё, что младшие, плюс новых рыб;
 * внутри ростера редкая рыба выпадает реже. Источник видов: config/aqualumen/fish_shop.json.
 */
public final class FishRosterService {

    public static final int MAX_TIER = 13;

    private record Entry(String id, int requiredTier, int weight) {}

    private static final Map<String, Integer> ROD_TIER = new HashMap<>();
    static {
        ROD_TIER.put("bamboo_rod", 1);
        ROD_TIER.put("humble_rod", 2);
        ROD_TIER.put("good_old_rod", 3);
        ROD_TIER.put("boner_rod", 3);
        ROD_TIER.put("naturalist_rod", 4);
        ROD_TIER.put("slimed_rod", 5);
        ROD_TIER.put("iceborn_rod", 6);
        ROD_TIER.put("starcatcher_rod", 7);
        ROD_TIER.put("azure_crystal_rod", 8);
        ROD_TIER.put("sharktooth_rod", 9);
        ROD_TIER.put("obsidian_rod", 10);
        ROD_TIER.put("lush_glowberry_rod", 11);
        ROD_TIER.put("magmaforged_rod", 12);
        ROD_TIER.put("alpha_rod", 13);
    }

    private static volatile List<Entry> roster = List.of();
    private static volatile long mtime = -1L;

    private FishRosterService() {
    }

    public static int tierOf(String rodId) {
        return ROD_TIER.getOrDefault(rodId, 1);
    }

    /** Ролл рыбы под тир удочки; null — ростер пуст (тогда останется базовый лут). */
    public static ItemStack roll(int tier, RandomSource random) {
        ensure();
        List<Entry> pool = new ArrayList<>();
        for (Entry e : roster) {
            if (e.requiredTier() <= tier) {
                pool.add(e);
            }
        }
        if (pool.isEmpty()) return null;
        int total = 0;
        for (Entry e : pool) total += e.weight();
        int roll = random.nextInt(total);
        for (Entry e : pool) {
            roll -= e.weight();
            if (roll < 0) {
                ResourceLocation id = ResourceLocation.tryParse(e.id());
                if (id == null) return null;
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item == Items.AIR) return null;
                return new ItemStack(item);
            }
        }
        return null;
    }

    private static void ensure() {
        try {
            Path file = FMLPaths.CONFIGDIR.get().resolve("aqualumen/fish_shop.json");
            if (!Files.exists(file)) return;
            long m = Files.getLastModifiedTime(file).toMillis();
            if (m == mtime) return;
            mtime = m;
            var root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            List<FishDef> fishes = new ArrayList<>();
            for (var e : root.getAsJsonArray("fishes")) {
                JsonObject f = e.getAsJsonObject();
                fishes.add(new FishDef(
                        f.get("id").getAsString(),
                        f.has("priceCoins") ? f.get("priceCoins").getAsLong() : 30L,
                        f.has("rarity") ? f.get("rarity").getAsString() : "Обычный"));
            }
            fishes.sort((a, b) -> Long.compare(a.price, b.price));
            List<Entry> built = new ArrayList<>();
            int prev = 0;
            for (FishDef f : fishes) {
                int bandStart = bandStartByRarity(f.rarity);
                int bandSpan = bandSpanByRarity(f.rarity);
                int idxInBand = countSame(built, f.rarity);
                int required = bandStart + (bandSpan == 0 ? 0 : idxInBand * bandSpan / Math.max(1, countInSource(fishes, f.rarity)));
                required = Math.max(prev, Math.min(MAX_TIER, required));
                prev = required;
                built.add(new Entry(f.id, required, weightByRarity(f.rarity)));
            }
            roster = List.copyOf(built);
        } catch (Exception ignored) {
        }
    }

    private record FishDef(String id, long price, String rarity) {}

    private static int countSame(List<Entry> built, String rarity) {
        int n = 0;
        for (Entry e : built) if (rarityOf(e.requiredTier()).equals(rarity)) n++;
        return n;
    }

    private static int countInSource(List<FishDef> fishes, String rarity) {
        int n = 0;
        for (FishDef f : fishes) if (f.rarity.equals(rarity)) n++;
        return Math.max(1, n);
    }

    private static String rarityOf(int tier) {
        if (tier >= 8) return "Легенда";
        if (tier >= 6) return "Эпический";
        if (tier >= 3) return "Редкий";
        return "Обычный";
    }

    private static int bandStartByRarity(String rarity) {
        return switch (rarity.toLowerCase(java.util.Locale.ROOT)) {
            case "легенда", "legend" -> 9;
            case "эпический", "epic", "эпичекский" -> 6;
            case "необычный", "uncommon" -> 2;
            case "редкий", "rare" -> 3;
            default -> 1;
        };
    }

    private static int bandSpanByRarity(String rarity) {
        return switch (rarity.toLowerCase(java.util.Locale.ROOT)) {
            case "легенда", "legend" -> 4;
            case "эпический", "epic", "эпичекский" -> 6;
            case "необычный", "uncommon" -> 4;
            case "редкий", "rare" -> 7;
            default -> 7;
        };
    }

    private static int weightByRarity(String rarity) {
        return switch (rarity.toLowerCase(java.util.Locale.ROOT)) {
            case "легенда", "legend" -> 3;
            case "эпический", "epic", "эпичекский" -> 10;
            case "необычный", "uncommon" -> 50;
            case "редкий", "rare" -> 30;
            default -> 70;
        };
    }

    static JsonArray debugRoster(int tier) {
        ensure();
        JsonArray arr = new JsonArray();
        for (Entry e : roster) {
            if (e.requiredTier() <= tier) {
                JsonObject o = new JsonObject();
                o.addProperty("id", e.id());
                o.addProperty("fromTier", e.requiredTier());
                arr.add(o);
            }
        }
        return arr;
    }
}
