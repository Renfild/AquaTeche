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
 * Внутриигровая валюта мода. Баланс хранится по UUID игрока и сохраняется в
 * config/casesmod/balances.json — переживает перезапуск сервера.
 * Открытие кейса теперь стоит валюту вместо физического ключа.
 */
public class CurrencyManager {
    public static final CurrencyManager INSTANCE = new CurrencyManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, Long>>(){}.getType();

    private final Map<UUID, Long> balances = new HashMap<>();
    private Path file;

    public void load() {
        balances.clear();
        file = Paths.get("config", "casesmod", "balances.json");
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                save();
                return;
            }
            try (Reader r = new InputStreamReader(new FileInputStream(file.toFile()), "UTF-8")) {
                Map<String, Long> raw = GSON.fromJson(r, MAP_TYPE);
                if (raw != null) {
                    raw.forEach((k, v) -> balances.put(UUID.fromString(k), v));
                }
            }
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка загрузки баланса игроков", e);
        }
    }

    public void save() {
        if (file == null) return;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
            Map<String, Long> raw = new HashMap<>();
            balances.forEach((k, v) -> raw.put(k.toString(), v));
            GSON.toJson(raw, MAP_TYPE, w);
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка сохранения баланса игроков", e);
        }
    }

    public long getBalance(UUID player) {
        return balances.getOrDefault(player, 0L);
    }

    public void setBalance(UUID player, long amount) {
        balances.put(player, Math.max(0, amount));
        save();
    }

    public void add(UUID player, long amount) {
        setBalance(player, getBalance(player) + amount);
    }

    /** Пытается списать сумму. Возвращает true и списывает, если средств достаточно; иначе false и ничего не меняет. */
    public boolean tryCharge(UUID player, long amount) {
        long bal = getBalance(player);
        if (bal < amount) return false;
        setBalance(player, bal - amount);
        return true;
    }
}
