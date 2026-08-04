package com.casesmod.data;

import com.casesmod.CasesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Персистентные аккаунты: config/casesmod/players/<uuid>.json */
public class PlayerAccountManager {
    public static final PlayerAccountManager INSTANCE = new PlayerAccountManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<UUID, PlayerAccount> cache = new ConcurrentHashMap<>();

    public PlayerAccount get(UUID id) {
        return cache.computeIfAbsent(id, this::load);
    }

    public void save(UUID id) {
        PlayerAccount acc = cache.get(id);
        if (acc == null) return;
        Path file = pathFor(id);
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
                GSON.toJson(acc, w);
            }
        } catch (IOException e) {
            CasesMod.LOGGER.error("Не удалось сохранить аккаунт {}", id, e);
        }
    }

    public void saveAll() {
        for (UUID id : cache.keySet()) save(id);
    }

    public void unload(UUID id) {
        save(id);
        cache.remove(id);
    }

    private PlayerAccount load(UUID id) {
        Path file = pathFor(id);
        if (Files.exists(file)) {
            try (Reader r = new InputStreamReader(new FileInputStream(file.toFile()), "UTF-8")) {
                PlayerAccount acc = GSON.fromJson(r, PlayerAccount.class);
                if (acc != null) {
                    if (acc.cases == null) acc.cases = new java.util.HashMap<>();
                    return acc;
                }
            } catch (Exception e) {
                CasesMod.LOGGER.error("Не удалось загрузить аккаунт {}", id, e);
            }
        }
        return new PlayerAccount();
    }

    private static Path pathFor(UUID id) {
        return Paths.get("config", "casesmod", "players", id.toString() + ".json");
    }
}
