package store.aquateche.aqualumen.common.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import store.aquateche.aqualumen.AquaLumenUI;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configurable fish selling shop with dynamic rarity and weight-based pricing (config/aqualumen/fish_shop.json).
 */
public final class FishShopConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/fish_shop.json");

    private static Data cached;
    private static long cachedMtime;

    private static final Path DEMAND_FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/fish_demand.json");
    private static final Object DEMAND_LOCK = new Object();
    private static volatile Map<String, Double> demandMult = Map.of();
    private static volatile int demandDay = -1;

    /**
     * Daily demand: 3 trending fish from the whole catalog, multipliers x2.0 / x1.75 / x1.5.
     * Deterministic index math shared with the KubeJS cat shop so both stay in sync.
     * Persists to config/aqualumen/fish_demand.json for announcements and debugging.
     */
    public static void ensureDemand() {
        int today = (int) LocalDate.now().toEpochDay();
        if (demandDay == today) {
            return;
        }
        synchronized (DEMAND_LOCK) {
            if (demandDay == today) {
                return;
            }
            try {
                if (Files.exists(DEMAND_FILE)) {
                    JsonObject j = JsonParser.parseString(Files.readString(DEMAND_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (j.has("day") && j.get("day").getAsInt() == today && j.has("trends")) {
                        Map<String, Double> m = new HashMap<>();
                        for (var t : j.getAsJsonArray("trends")) {
                            JsonObject o = t.getAsJsonObject();
                            m.put(o.get("id").getAsString(), o.get("mult").getAsDouble());
                        }
                        demandMult = m;
                        demandDay = today;
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
            List<FishDef> pool = get().fishes;
            int n = pool.size();
            Map<String, Double> m = new HashMap<>();
            JsonArray trends = new JsonArray();
            if (n > 0) {
                int i1 = ((today * 7 + 3) % n + n) % n;
                int i2 = ((today * 13 + 5) % n + n) % n;
                if (i2 == i1) i2 = (i2 + 1) % n;
                int i3 = ((today * 29 + 11) % n + n) % n;
                if (i3 == i1 || i3 == i2) i3 = (i3 + 1) % n;
                if (i3 == i1) i3 = (i3 + 1) % n;
                double[] mults = {2.0, 1.75, 1.5};
                int[] idx = {i1, i2, i3};
                for (int i = 0; i < 3; i++) {
                    FishDef f = pool.get(idx[i]);
                    m.put(f.id, mults[i]);
                    JsonObject o = new JsonObject();
                    o.addProperty("id", f.id);
                    o.addProperty("name", f.name);
                    o.addProperty("mult", mults[i]);
                    trends.add(o);
                }
            }
            demandMult = m;
            demandDay = today;
            try {
                Files.createDirectories(DEMAND_FILE.getParent());
                JsonObject root = new JsonObject();
                root.addProperty("day", today);
                root.add("trends", trends);
                Files.writeString(DEMAND_FILE, GSON.toJson(root), StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
        }
    }

    public static double demandFor(String itemId) {
        ensureDemand();
        Double v = demandMult.get(itemId);
        return v == null ? 1.0 : v;
    }

    private FishShopConfig() {
    }

    public static synchronized Data get() {
        try {
            if (Files.exists(FILE)) {
                long mtime = Files.getLastModifiedTime(FILE).toMillis();
                if (cached == null || mtime != cachedMtime) {
                    cached = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), Data.class);
                    cachedMtime = mtime;
                }
            } else {
                if (cached == null) {
                    Files.createDirectories(FILE.getParent());
                    Data def = defaults();
                    Files.writeString(FILE, GSON.toJson(def), StandardCharsets.UTF_8);
                    cached = def;
                    cachedMtime = Files.getLastModifiedTime(FILE).toMillis();
                }
            }
        } catch (IOException | RuntimeException error) {
            AquaLumenUI.LOGGER.warn("Fish shop config unreadable, using defaults: {}", error.toString());
            cached = defaults();
            cachedMtime = 0L;
        }
        return cached;
    }

    public static synchronized boolean save(Data data) {
        if (data == null) return false;
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(data), StandardCharsets.UTF_8);
            cached = data;
            cachedMtime = Files.getLastModifiedTime(FILE).toMillis();
            return true;
        } catch (IOException e) {
            AquaLumenUI.LOGGER.error("Failed to save fish_shop.json", e);
            return false;
        }
    }

    public static synchronized boolean addOrUpdate(FishDef def) {
        if (def == null || def.id == null || def.id.isBlank()) return false;
        Data data = get();
        data.fishes.removeIf(f -> f.id.equalsIgnoreCase(def.id));
        data.fishes.add(def);
        return save(data);
    }

    public static synchronized boolean remove(String id) {
        if (id == null || id.isBlank()) return false;
        Data data = get();
        boolean removed = data.fishes.removeIf(f -> f.id.equalsIgnoreCase(id));
        if (removed) {
            save(data);
        }
        return removed;
    }

    public static synchronized void reload() {
        cached = null;
        cachedMtime = 0L;
        get();
    }

    public static FishDef find(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (FishDef def : get().fishes) {
            if (id.equalsIgnoreCase(def.id)) {
                return def;
            }
        }
        return null;
    }

    /**
     * Calculates the dynamic price of a single fish item stack based on base price, rarity, weight, and golden flag.
     */
    public static long calculateFishPrice(ItemStack stack, FishDef baseDef) {
        if (stack == null || stack.isEmpty()) return 0;
        long basePrice = baseDef != null ? baseDef.priceCoins : 5L;

        double rarityMultiplier = 1.0;
        double weightMultiplier = 1.0;
        double goldenMultiplier = 1.0;

        String itemRarity = baseDef != null ? baseDef.rarity : "Базовый";

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            CompoundTag fishInfo = tag.contains("caught_fish_info") ? tag.getCompound("caught_fish_info") : tag;

            // 1. Rarity from NBT if present
            String rarityStr = "";
            if (fishInfo.contains("rarity")) {
                rarityStr = fishInfo.getString("rarity");
            } else if (tag.contains("Rarity")) {
                rarityStr = tag.getString("Rarity");
            }
            if (!rarityStr.isBlank()) {
                itemRarity = rarityStr;
            }

            // 2. Weight & Size
            int weightGrams = 0;
            if (fishInfo.contains("weight")) {
                weightGrams = fishInfo.getInt("weight");
            } else if (fishInfo.contains("weightInGrams")) {
                weightGrams = fishInfo.getInt("weightInGrams");
            } else if (tag.contains("Weight")) {
                weightGrams = tag.getInt("Weight");
            }

            float percentile = 0.0F;
            if (fishInfo.contains("percentile")) {
                percentile = fishInfo.getFloat("percentile");
            }

            if (weightGrams > 0) {
                weightMultiplier = 1.0 + Math.min(5.0, ((double) weightGrams / 800.0) * 0.4 + (percentile * 0.6));
            } else if (percentile > 0.0F) {
                weightMultiplier = 1.0 + (percentile * 1.5);
            }

            // 3. Golden / Trophy
            boolean golden = false;
            if (fishInfo.contains("golden")) {
                golden = fishInfo.getBoolean("golden");
            } else if (tag.contains("Golden")) {
                golden = tag.getBoolean("Golden");
            }
            if (golden) {
                goldenMultiplier = 2.5;
            }
        }

        // Rarity multiplier mapping
        String rLower = itemRarity.toLowerCase();
        if (rLower.contains("mythic") || rLower.contains("\u043c\u0438\u0444\u0438\u043a") || rLower.contains("\u043c\u0438\u0444\u0438\u0447\u0435\u0441\u043a")) {
            rarityMultiplier = 15.0;
        } else if (rLower.contains("legend") || rLower.contains("\u043b\u0435\u0433\u0435\u043d\u0434")) {
            rarityMultiplier = 8.0;
        } else if (rLower.contains("epic") || rLower.contains("\u044d\u043f\u0438\u043a") || rLower.contains("\u044d\u043f\u0438\u0447\u0435\u0441\u043a")) {
            rarityMultiplier = 4.0;
        } else if (rLower.contains("rare") || rLower.contains("\u0440\u0435\u0434\u043a")) {
            rarityMultiplier = 2.2;
        } else if (rLower.contains("uncommon") || rLower.contains("\u043d\u0435\u043e\u0431\u044b\u0447\u043d")) {
            rarityMultiplier = 1.4;
        }

        double unitPrice = basePrice * rarityMultiplier * weightMultiplier * goldenMultiplier;
        if (baseDef != null) {
            unitPrice *= demandFor(baseDef.id);
        }
        long finalUnitPrice = Math.max(1L, Math.round(unitPrice));
        return finalUnitPrice * stack.getCount();
    }

    public static int countInInventory(ServerPlayer player, String itemId) {
        if (player == null || itemId == null) return 0;
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
        if (item == null || BuiltInRegistries.ITEM.getKey(item) == null) return 0;
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /** Only raw vanilla fish outside the config list — never namespace-wide (rods/hats live there too). */
    private static boolean isVanillaFish(String itemId) {
        return itemId.equals("minecraft:cod") || itemId.equals("minecraft:salmon")
                || itemId.equals("minecraft:tropical_fish") || itemId.equals("minecraft:pufferfish");
    }

    public static void sellAll(ServerPlayer player) {
        if (player == null) return;
        long totalCoins = 0;
        int totalFishCount = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key == null) continue;
            String itemId = key.toString();

            FishDef def = find(itemId);
            if (def == null && !isVanillaFish(itemId)) {
                continue;
            }

            int count = stack.getCount();
            long price = calculateFishPrice(stack, def);
            totalCoins += price;
            totalFishCount += count;
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }

        player.inventoryMenu.broadcastChanges();

        if (totalFishCount <= 0) {
            player.sendSystemMessage(Component.literal("\u00a7e[AquaTech] \u0423 \u0432\u0430\u0441 \u043d\u0435\u0442 \u0440\u044b\u0431\u044b \u0434\u043b\u044f \u043f\u0440\u043e\u0434\u0430\u0436\u0438!"));
            return;
        }

        HubEconomy.grantCoins(player, totalCoins);
        player.sendSystemMessage(Component.literal("\u00a7a[AquaTech] \u00a7f\u0423\u043b\u043e\u0432 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u0440\u043e\u0434\u0430\u043d: \u00a7e" + totalFishCount + " \u0448\u0442. \u00a7f\u043d\u0430 \u0441\u0443\u043c\u043c\u0443 \u00a76+" + totalCoins + " \u043c\u043e\u043d\u0435\u0442\u00a7f (\u0441 \u0443\u0447\u0451\u0442\u043e\u043c \u0432\u0435\u0441\u0430 \u0438 \u0440\u0435\u0434\u043a\u043e\u0441\u0442\u0438)!"));
        HubDataService.push(player);
    }

    public static void sellSingle(ServerPlayer player, String fishId) {
        if (player == null || fishId == null) return;
        FishDef def = find(fishId);
        if (def == null && !isVanillaFish(fishId)) {
            player.sendSystemMessage(Component.literal("\u00a7c[AquaTech] \u042d\u0442\u043e\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u043d\u0435\u043b\u044c\u0437\u044f \u043f\u0440\u043e\u0434\u0430\u0442\u044c: " + fishId));
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(fishId));
        if (item == null) {
            player.sendSystemMessage(Component.literal("\u00a7c[AquaTech] \u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0440\u044b\u0431\u044b: " + fishId));
            return;
        }
        long totalCoins = 0;
        int totalCount = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                int count = stack.getCount();
                long price = calculateFishPrice(stack, def);
                totalCoins += price;
                totalCount += count;
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        player.inventoryMenu.broadcastChanges();

        if (totalCount <= 0) {
            player.sendSystemMessage(Component.literal("\u00a7e[AquaTech] \u0412 \u0432\u0430\u0448\u0435\u043c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435 \u043d\u0435\u0442 \u044d\u0442\u043e\u0439 \u0440\u044b\u0431\u044b!"));
            return;
        }

        HubEconomy.grantCoins(player, totalCoins);
        String fishName = def != null ? def.name : item.getDescription().getString();
        player.sendSystemMessage(Component.literal("\u00a7a[AquaTech] \u00a7f\u041f\u0440\u043e\u0434\u0430\u043d\u043e \u00a7e" + totalCount + " \u0448\u0442. " + fishName + " \u00a7f\u043d\u0430 \u00a76+" + totalCoins + " \u043c\u043e\u043d\u0435\u0442\u00a7f (\u0441 \u0443\u0447\u0451\u0442\u043e\u043c \u0432\u0435\u0441\u0430 \u0438 \u0440\u0435\u0434\u043a\u043e\u0441\u0442\u0438)!"));
        HubDataService.push(player);
    }

    public static final class Data {
        public List<FishDef> fishes = new ArrayList<>();
    }

    public static final class FishDef {
        public String id;
        public String name;
        public long priceCoins;
        public String rarity;
        public String tag;

        public FishDef() {
        }

        public FishDef(String id, String name, long priceCoins, String rarity, String tag) {
            this.id = id;
            this.name = name;
            this.priceCoins = priceCoins;
            this.rarity = rarity;
            this.tag = tag;
        }
    }

    private static Data defaults() {
        Data d = new Data();
        d.fishes.add(new FishDef("minecraft:cod", "\u0421\u044b\u0440\u0430\u044f \u0442\u0440\u0435\u0441\u043a\u0430", 5, "\u0411\u0430\u0437\u043e\u0432\u044b\u0439", "minecraft"));
        d.fishes.add(new FishDef("minecraft:salmon", "\u0421\u044b\u0440\u043e\u0439 \u043b\u043e\u0441\u043e\u0441\u044c", 5, "\u0411\u0430\u0437\u043e\u0432\u044b\u0439", "minecraft"));
        d.fishes.add(new FishDef("starcatcher:driftfin", "Driftfin (\u0414\u0440\u0438\u0444\u0442\u0444\u0438\u043d)", 10, "\u041e\u0431\u044b\u0447\u043d\u044b\u0439", "starcatcher"));
        d.fishes.add(new FishDef("starcatcher:rockgill", "Rockgill (\u0420\u043e\u043a\u0433\u0438\u043b\u043b)", 15, "\u0420\u0435\u0434\u043a\u0438\u0439", "starcatcher"));
        d.fishes.add(new FishDef("starcatcher:sunny_sturgeon", "Sunny Sturgeon (\u041e\u0441\u0451\u0442\u0440)", 20, "\u0420\u0435\u0434\u043a\u0438\u0439", "starcatcher"));
        d.fishes.add(new FishDef("starcatcher:silverfin_pike", "Silverfin Pike (\u0429\u0443\u043a\u0430)", 25, "\u042d\u043f\u0438\u0447\u0435\u0441\u043a\u0438\u0439", "starcatcher"));
        d.fishes.add(new FishDef("starcatcher:carpenjoe", "Carpenjoe (\u041a\u0430\u0440\u043f\u0435\u043d\u0434\u0436\u043e)", 30, "\u042d\u043f\u0438\u0447\u0435\u0441\u043a\u0438\u0439", "starcatcher"));
        d.fishes.add(new FishDef("starcatcher:hollowbelly_darter", "Hollowbelly Darter (\u0422\u0435\u043c\u043d\u043e\u0431\u0440\u044e\u0448\u043a\u0430)", 40, "\u042d\u043f\u0438\u0447\u0435\u0441\u043a\u0438\u0439", "starcatcher"));
        d.fishes.add(new FishDef("starcatcher:silverveil_perch", "Silverveil Perch (\u041e\u043a\u0443\u043d\u044c)", 50, "\u042d\u043f\u0438\u0447\u0435\u0441\u043a\u0438\u0439", "starcatcher"));
        d.fishes.add(new FishDef("starcatcher:elderscale", "Elderscale (\u0414\u0440\u0435\u0432\u043d\u044f\u044f \u0440\u044b\u0431\u0430)", 100, "\u041b\u0435\u0433\u0435\u043d\u0434\u0430", "starcatcher"));
        return d;
    }
}
