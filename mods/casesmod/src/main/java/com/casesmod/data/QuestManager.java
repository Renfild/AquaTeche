package com.casesmod.data;

import com.casesmod.CasesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public class QuestManager {
    public static final QuestManager INSTANCE = new QuestManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type PROGRESS_TYPE = new TypeToken<HashMap<String, PlayerQuestData>>(){}.getType();

    private final Map<String, QuestDefinition> quests = new LinkedHashMap<>();

    /** playerUUID -> (questId -> прогресс) */
    private final Map<UUID, Map<String, Integer>> progress = new HashMap<>();
    /** playerUUID -> набор уже полученных наград */
    private final Map<UUID, Set<String>> claimed = new HashMap<>();
    private Path progressFile;

    public Collection<QuestDefinition> getQuests() { return quests.values(); }
    public QuestDefinition get(String id) { return quests.get(id); }

    /** Загружает ОПИСАНИЯ квестов из quests.json. Не трогает прогресс игроков — безопасно вызывать через /casesmod reload. */
    public void load() {
        quests.clear();
        Path file = Paths.get("config", "casesmod", "quests.json");
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) writeDefaults(file);
            try (Reader r = new InputStreamReader(new FileInputStream(file.toFile()), "UTF-8")) {
                QuestDefinition[] arr = GSON.fromJson(r, QuestDefinition[].class);
                if (arr != null) for (QuestDefinition q : arr) quests.put(q.id, q);
            }
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка загрузки квестов", e);
        }
    }

    /** Загружает прогресс всех игроков с диска. Вызывать один раз при старте сервера (не при /reload). */
    public void loadProgress() {
        progress.clear();
        claimed.clear();
        progressFile = Paths.get("config", "casesmod", "quest_progress.json");
        try {
            Files.createDirectories(progressFile.getParent());
            if (!Files.exists(progressFile)) return;
            try (Reader r = new InputStreamReader(new FileInputStream(progressFile.toFile()), "UTF-8")) {
                Map<String, PlayerQuestData> raw = GSON.fromJson(r, PROGRESS_TYPE);
                if (raw == null) return;
                raw.forEach((uuidStr, data) -> {
                    UUID uuid = UUID.fromString(uuidStr);
                    progress.put(uuid, new HashMap<>(data.progress));
                    claimed.put(uuid, new HashSet<>(data.claimed));
                });
            }
        } catch (Exception e) {
            CasesMod.LOGGER.error("Ошибка загрузки прогресса квестов", e);
        }
    }

    /** Сохраняет прогресс всех игроков на диск. Вызывается при выходе игрока и остановке сервера. */
    public void saveProgress() {
        if (progressFile == null) return;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(progressFile.toFile()), "UTF-8")) {
            Map<String, PlayerQuestData> raw = new HashMap<>();
            Set<UUID> allPlayers = new HashSet<>();
            allPlayers.addAll(progress.keySet());
            allPlayers.addAll(claimed.keySet());
            for (UUID uuid : allPlayers) {
                PlayerQuestData data = new PlayerQuestData();
                data.progress = new HashMap<>(progress.getOrDefault(uuid, Collections.emptyMap()));
                data.claimed = new ArrayList<>(claimed.getOrDefault(uuid, Collections.emptySet()));
                raw.put(uuid.toString(), data);
            }
            GSON.toJson(raw, PROGRESS_TYPE, w);
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка сохранения прогресса квестов", e);
        }
    }

    private void writeDefaults(Path file) throws IOException {
        QuestDefinition mine = new QuestDefinition();
        mine.id = "mine_stone"; mine.displayName = "§eДобытчик"; mine.description = "Добудьте 64 камня";
        mine.iconItemId = "minecraft:stone_pickaxe"; mine.type = "MINE_BLOCK"; mine.target = "minecraft:stone";
        mine.requiredAmount = 64; mine.rewardItemId = "minecraft:diamond"; mine.rewardCount = 5;

        QuestDefinition kill = new QuestDefinition();
        kill.id = "kill_zombies"; kill.displayName = "§cОхотник на зомби"; kill.description = "Убейте 20 зомби";
        kill.iconItemId = "minecraft:iron_sword"; kill.type = "KILL_MOB"; kill.target = "minecraft:zombie";
        kill.requiredAmount = 20; kill.rewardItemId = "minecraft:emerald"; kill.rewardCount = 10;

        QuestDefinition collect = new QuestDefinition();
        collect.id = "collect_wood"; collect.displayName = "§6Лесоруб"; collect.description = "Соберите 128 дерева";
        collect.iconItemId = "minecraft:oak_log"; collect.type = "COLLECT_ITEM"; collect.target = "minecraft:oak_log";
        collect.requiredAmount = 128; collect.rewardItemId = "minecraft:gold_ingot"; collect.rewardCount = 8;

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
            GSON.toJson(new QuestDefinition[]{mine, kill, collect}, w);
        }
    }

    public int getProgress(UUID player, String questId) {
        return progress.getOrDefault(player, Collections.emptyMap()).getOrDefault(questId, 0);
    }

    public void addProgress(UUID player, String type, String target, int amount) {
        for (QuestDefinition q : quests.values()) {
            if (!q.type.equalsIgnoreCase(type) || !q.target.equalsIgnoreCase(target)) continue;
            if (isClaimed(player, q.id)) continue;
            Map<String, Integer> map = progress.computeIfAbsent(player, k -> new HashMap<>());
            int cur = map.getOrDefault(q.id, 0);
            map.put(q.id, Math.min(q.requiredAmount, cur + amount));
        }
        // Не пишем на диск на каждый удар кирки — это будет вызываться очень часто.
        // Прогресс сохраняется при выходе игрока и остановке сервера (см. event/PersistenceHandler.java).
    }

    public boolean isComplete(UUID player, QuestDefinition q) {
        return getProgress(player, q.id) >= q.requiredAmount;
    }

    public boolean isClaimed(UUID player, String questId) {
        return claimed.getOrDefault(player, Collections.emptySet()).contains(questId);
    }

    public void markClaimed(UUID player, String questId) {
        claimed.computeIfAbsent(player, k -> new HashSet<>()).add(questId);
        saveProgress(); // получение награды — редкое событие, безопасно сохранить сразу
    }
}
