package net.aquatech.ui.fishing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages custom user-defined fishing loot per rod.
 * Allows viewing and editing rod catches directly in-game via commands.
 * Persists to config/aquatech_custom_fishing_loot.json
 */
public class CustomFishingLootManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, List<CustomLootEntry>> ROD_LOOT_MAP = new HashMap<>();
    private static boolean loaded = false;

    public static class CustomLootEntry {
        public String itemId;
        public float chance; // 0.00 to 1.00
        public int min;
        public int max;
        public String nbtJson;

        public CustomLootEntry(String itemId, float chance, int min, int max, String nbtJson) {
            this.itemId = itemId;
            this.chance = chance;
            this.min = min;
            this.max = max;
            this.nbtJson = nbtJson;
        }

        public ItemStack createStack(RandomSource random) {
            ResourceLocation loc = ResourceLocation.tryParse(itemId);
            if (loc == null) return ItemStack.EMPTY;
            Item item = BuiltInRegistries.ITEM.get(loc);
            if (item == Items.AIR) return ItemStack.EMPTY;

            int count = min;
            if (max > min) {
                count += random.nextInt(max - min + 1);
            }
            ItemStack stack = new ItemStack(item, count);
            if (nbtJson != null && !nbtJson.isBlank()) {
                try {
                    CompoundTag tag = TagParser.parseTag(nbtJson);
                    stack.setTag(tag);
                } catch (Exception ignored) {
                }
            }
            return stack;
        }
    }

    public static synchronized void ensureLoaded() {
        if (!loaded) {
            load();
            loaded = true;
        }
    }

    public static Path getConfigFile() {
        return FMLPaths.CONFIGDIR.get().resolve("aquatech_custom_fishing_loot.json");
    }

    public static synchronized void load() {
        ROD_LOOT_MAP.clear();
        File file = getConfigFile().toFile();
        if (!file.exists()) {
            save(); // Create empty template
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String rodId = normalizeRodId(entry.getKey());
                JsonArray arr = entry.getValue().getAsJsonArray();
                List<CustomLootEntry> list = new ArrayList<>();
                for (JsonElement elem : arr) {
                    JsonObject obj = elem.getAsJsonObject();
                    String itemId = obj.get("item").getAsString();
                    float chance = obj.get("chance").getAsFloat();
                    int min = obj.has("min") ? obj.get("min").getAsInt() : 1;
                    int max = obj.has("max") ? obj.get("max").getAsInt() : min;
                    String nbt = obj.has("nbt") ? obj.get("nbt").getAsString() : null;
                    list.add(new CustomLootEntry(itemId, chance, min, max, nbt));
                }
                ROD_LOOT_MAP.put(rodId, list);
            }
        } catch (Exception ex) {
            System.err.println("[AquaTech] Error loading custom fishing loot config: " + ex.getMessage());
        }
    }

    public static synchronized void save() {
        try {
            File file = getConfigFile().toFile();
            if (file.getParentFile() != null) file.getParentFile().mkdirs();

            JsonObject root = new JsonObject();
            for (Map.Entry<String, List<CustomLootEntry>> entry : ROD_LOOT_MAP.entrySet()) {
                JsonArray arr = new JsonArray();
                for (CustomLootEntry loot : entry.getValue()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("item", loot.itemId);
                    obj.addProperty("chance", loot.chance);
                    obj.addProperty("min", loot.min);
                    obj.addProperty("max", loot.max);
                    if (loot.nbtJson != null) {
                        obj.addProperty("nbt", loot.nbtJson);
                    }
                    arr.add(obj);
                }
                root.add(entry.getKey(), arr);
            }

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception ex) {
            System.err.println("[AquaTech] Error saving custom fishing loot config: " + ex.getMessage());
        }
    }

    public static String normalizeRodId(String rodId) {
        if (rodId == null) return "";
        String s = rodId.trim().toLowerCase(java.util.Locale.ROOT);
        if (s.contains(":")) {
            s = s.substring(s.indexOf(":") + 1);
        }
        return s;
    }

    public static synchronized List<ItemStack> rollCustomLoot(String rodId, RandomSource random) {
        ensureLoaded();
        List<ItemStack> result = new ArrayList<>();
        String norm = normalizeRodId(rodId);
        List<CustomLootEntry> entries = ROD_LOOT_MAP.get(norm);
        if (entries == null || entries.isEmpty()) return result;

        for (CustomLootEntry entry : entries) {
            if (random.nextFloat() < entry.chance) {
                ItemStack stack = entry.createStack(random);
                if (!stack.isEmpty()) {
                    result.add(stack);
                }
            }
        }
        return result;
    }

    public static synchronized boolean addCustomLoot(String rodId, ItemStack heldItem, float chance, int min, int max) {
        ensureLoaded();
        if (heldItem == null || heldItem.isEmpty()) return false;
        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(heldItem.getItem());
        if (loc == null) return false;

        String normRod = normalizeRodId(rodId);
        String itemId = loc.toString();
        String nbtJson = (heldItem.hasTag() && heldItem.getTag() != null) ? heldItem.getTag().toString() : null;

        List<CustomLootEntry> list = ROD_LOOT_MAP.computeIfAbsent(normRod, k -> new ArrayList<>());
        list.removeIf(e -> e.itemId.equalsIgnoreCase(itemId));
        list.add(new CustomLootEntry(itemId, Math.max(0.001f, Math.min(1.0f, chance)), Math.max(1, min), Math.max(min, max), nbtJson));

        save();
        return true;
    }

    public static synchronized boolean removeCustomLoot(String rodId, ItemStack heldItem) {
        ensureLoaded();
        if (heldItem == null || heldItem.isEmpty()) return false;
        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(heldItem.getItem());
        if (loc == null) return false;

        String normRod = normalizeRodId(rodId);
        String itemId = loc.toString();
        List<CustomLootEntry> list = ROD_LOOT_MAP.get(normRod);
        if (list == null) return false;

        boolean removed = list.removeIf(e -> e.itemId.equalsIgnoreCase(itemId));
        if (removed) {
            save();
        }
        return removed;
    }

    public static synchronized boolean clearCustomLoot(String rodId) {
        ensureLoaded();
        String normRod = normalizeRodId(rodId);
        if (ROD_LOOT_MAP.containsKey(normRod)) {
            ROD_LOOT_MAP.remove(normRod);
            save();
            return true;
        }
        return false;
    }

    public static synchronized List<CustomLootEntry> getEntries(String rodId) {
        ensureLoaded();
        String normRod = normalizeRodId(rodId);
        List<CustomLootEntry> list = ROD_LOOT_MAP.get(normRod);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }
}
