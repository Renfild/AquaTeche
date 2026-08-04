package com.casesmod.data;

import com.casesmod.CasesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public class KitManager {
    public static final KitManager INSTANCE = new KitManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type COOLDOWN_TYPE = new TypeToken<HashMap<String, Long>>(){}.getType();

    private final Map<String, KitDefinition> kits = new LinkedHashMap<>();
    /** playerUUID#kitId -> timestamp последнего получения (millis) */
    private final Map<String, Long> lastClaim = new HashMap<>();
    private Path cooldownFile;

    public Collection<KitDefinition> getKits() { return kits.values(); }
    public KitDefinition get(String id) { return kits.get(id); }

    /** Загружает ОПИСАНИЯ китов из kits.json. Не трогает кулдауны — безопасно вызывать через /casesmod reload. */
    public void load() {
        kits.clear();
        Path file = Paths.get("config", "casesmod", "kits.json");
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                writeDefaults(file);
            }
            try (Reader r = new InputStreamReader(new FileInputStream(file.toFile()), "UTF-8")) {
                KitDefinition[] arr = GSON.fromJson(r, KitDefinition[].class);
                if (arr != null) {
                    for (KitDefinition d : arr) kits.put(d.id, d);
                }
            }
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка загрузки китов", e);
        }
    }

    /** Загружает кулдауны китов с диска. Вызывать один раз при старте сервера. */
    public void loadCooldowns() {
        lastClaim.clear();
        cooldownFile = Paths.get("config", "casesmod", "kit_cooldowns.json");
        try {
            Files.createDirectories(cooldownFile.getParent());
            if (!Files.exists(cooldownFile)) return;
            try (Reader r = new InputStreamReader(new FileInputStream(cooldownFile.toFile()), "UTF-8")) {
                Map<String, Long> raw = GSON.fromJson(r, COOLDOWN_TYPE);
                if (raw != null) lastClaim.putAll(raw);
            }
        } catch (Exception e) {
            CasesMod.LOGGER.error("Ошибка загрузки кулдаунов китов", e);
        }
    }

    /** Сохраняет кулдауны на диск. */
    public void saveCooldowns() {
        if (cooldownFile == null) return;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(cooldownFile.toFile()), "UTF-8")) {
            GSON.toJson(lastClaim, COOLDOWN_TYPE, w);
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка сохранения кулдаунов китов", e);
        }
    }

    private void writeDefaults(Path file) throws IOException {
        KitDefinition starter = new KitDefinition();
        starter.id = "starter"; starter.displayName = "§aСтартовый набор";
        starter.iconItemId = "minecraft:chest_minecart"; starter.cooldownSeconds = 0;
        KitDefinition.KitItem bread = new KitDefinition.KitItem(); bread.itemId = "minecraft:bread"; bread.count = 8;
        KitDefinition.KitItem axe = new KitDefinition.KitItem(); axe.itemId = "minecraft:stone_axe"; axe.count = 1;
        starter.items = Arrays.asList(bread, axe);

        KitDefinition vip = new KitDefinition();
        vip.id = "vip"; vip.displayName = "§6VIP набор"; vip.iconItemId = "minecraft:golden_apple";
        vip.cooldownSeconds = 86400; vip.permission = "casesmod.kit.vip";
        KitDefinition.KitItem gapple = new KitDefinition.KitItem(); gapple.itemId = "minecraft:golden_apple"; gapple.count = 4;
        KitDefinition.KitItem diamond = new KitDefinition.KitItem(); diamond.itemId = "minecraft:diamond"; diamond.count = 3;
        vip.items = Arrays.asList(gapple, diamond);

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
            GSON.toJson(new KitDefinition[]{starter, vip}, w);
        }
    }

    public long secondsUntilAvailable(UUID player, KitDefinition kit) {
        if (kit.cooldownSeconds <= 0) return 0;
        Long last = lastClaim.get(player + "#" + kit.id);
        if (last == null) return 0;
        long elapsed = (System.currentTimeMillis() - last) / 1000L;
        long remain = kit.cooldownSeconds - elapsed;
        return Math.max(0, remain);
    }

    public void markClaimed(UUID player, KitDefinition kit) {
        lastClaim.put(player + "#" + kit.id, System.currentTimeMillis());
        saveCooldowns(); // получение кита — не самое частое событие, безопасно сохранить сразу
    }

    /** Создаёт или обновляет кит и сразу сохраняет kits.json — без /reload. */
    public void addOrUpdate(KitDefinition kit) {
        kits.put(kit.id, kit);
        save();
    }

    public void remove(String id) {
        kits.remove(id);
        save();
    }

    public void save() {
        Path file = Paths.get("config", "casesmod", "kits.json");
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
                GSON.toJson(kits.values().toArray(new KitDefinition[0]), w);
            }
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка сохранения китов", e);
        }
    }
}
