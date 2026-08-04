package com.casesmod.data;

import com.casesmod.CasesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

/**
 * Счётчик "неудачных" открытий подряд для pity-системы (гарантия редкого приза после N попыток).
 * Ключ — "playerUUID#caseId". Сохраняется на диск, переживает рестарт сервера.
 */
public class PityManager {
    public static final PityManager INSTANCE = new PityManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, Integer>>(){}.getType();

    private final Map<String, Integer> counters = new HashMap<>();
    private Path file;

    public void load() {
        counters.clear();
        file = Paths.get("config", "casesmod", "pity.json");
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) return;
            try (Reader r = new InputStreamReader(new FileInputStream(file.toFile()), "UTF-8")) {
                Map<String, Integer> raw = GSON.fromJson(r, MAP_TYPE);
                if (raw != null) counters.putAll(raw);
            }
        } catch (Exception e) {
            CasesMod.LOGGER.error("Ошибка загрузки pity-счётчиков", e);
        }
    }

    public void save() {
        if (file == null) return;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
            GSON.toJson(counters, MAP_TYPE, w);
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка сохранения pity-счётчиков", e);
        }
    }

    public int getCount(UUID player, String caseId) {
        return counters.getOrDefault(player + "#" + caseId, 0);
    }

    public void increment(UUID player, String caseId) {
        String key = player + "#" + caseId;
        counters.put(key, counters.getOrDefault(key, 0) + 1);
        save();
    }

    public void reset(UUID player, String caseId) {
        counters.remove(player + "#" + caseId);
        save();
    }
}
