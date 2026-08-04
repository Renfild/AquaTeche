package com.casesmod.data;

import com.casesmod.CasesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WarpManager {
    public static final WarpManager INSTANCE = new WarpManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, WarpDefinition> warps = new LinkedHashMap<>();

    public Collection<WarpDefinition> getWarps() { return warps.values(); }
    public WarpDefinition get(String id) { return warps.get(id); }

    public void load() {
        warps.clear();
        Path file = Paths.get("config", "casesmod", "warps.json");
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) writeDefaults(file);
            try (Reader r = new InputStreamReader(new FileInputStream(file.toFile()), "UTF-8")) {
                WarpDefinition[] arr = GSON.fromJson(r, WarpDefinition[].class);
                if (arr != null) for (WarpDefinition w : arr) warps.put(w.id, w);
            }
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка загрузки варпов", e);
        }
    }

    public void save() {
        Path file = Paths.get("config", "casesmod", "warps.json");
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
            GSON.toJson(warps.values().toArray(new WarpDefinition[0]), w);
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка сохранения варпов", e);
        }
    }

    public void addOrUpdate(WarpDefinition w) { warps.put(w.id, w); save(); }
    public void remove(String id) { warps.remove(id); save(); }

    private void writeDefaults(Path file) throws IOException {
        WarpDefinition spawn = new WarpDefinition();
        spawn.id = "spawn"; spawn.displayName = "§bСпавн"; spawn.iconItemId = "minecraft:compass";
        spawn.dimension = "minecraft:overworld"; spawn.x = 0; spawn.y = 100; spawn.z = 0;

        WarpDefinition shop = new WarpDefinition();
        shop.id = "shop"; shop.displayName = "§eМагазин"; shop.iconItemId = "minecraft:emerald";
        shop.dimension = "minecraft:overworld"; shop.x = 50; shop.y = 70; shop.z = 50;

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
            GSON.toJson(new WarpDefinition[]{spawn, shop}, w);
        }
    }
}
