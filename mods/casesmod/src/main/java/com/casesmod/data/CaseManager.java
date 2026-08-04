package com.casesmod.data;

import com.casesmod.CasesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** Загружает все *.json кейсы из config/casesmod/cases/ и хранит их в памяти. */
public class CaseManager {
    public static final CaseManager INSTANCE = new CaseManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, CaseDefinition> cases = new LinkedHashMap<>();

    public Collection<CaseDefinition> getCases() { return cases.values(); }
    public CaseDefinition get(String id) { return cases.get(id); }

    public void load() {
        cases.clear();
        Path dir = Paths.get("config", "casesmod", "cases");
        try {
            Files.createDirectories(dir);
            boolean empty;
            try (Stream<Path> s = Files.list(dir)) {
                empty = s.findFirst().isEmpty();
            }
            if (empty) {
                writeExampleCases(dir);
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
                for (Path p : stream) {
                    try (Reader r = new InputStreamReader(new FileInputStream(p.toFile()), "UTF-8")) {
                        CaseDefinition def = GSON.fromJson(r, CaseDefinition.class);
                        if (def != null && def.id != null) {
                            cases.put(def.id, def);
                        }
                    } catch (Exception e) {
                        CasesMod.LOGGER.error("Не удалось загрузить кейс {}", p, e);
                    }
                }
            }
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка загрузки кейсов", e);
        }
    }

    /** Создаёт четыре готовых примера кейсов при первом запуске, чтобы было видно систему в деле. */
    private void writeExampleCases(Path dir) {
        // Обычный кейс — недорогой, для всех игроков
        CaseDefinition common = new CaseDefinition();
        common.id = "common_case";
        common.displayName = "§fОбычный кейс";
        common.price = 100;
        common.iconItemId = "minecraft:chest";
        common.items = Arrays.asList(
                item("minecraft:iron_ingot", 5, 40, "COMMON", "§7Слиток железа"),
                item("minecraft:gold_ingot", 3, 25, "UNCOMMON", "§aСлиток золота"),
                item("minecraft:emerald", 4, 15, "UNCOMMON", "§aИзумруды"),
                item("minecraft:diamond", 2, 10, "RARE", "§9Алмазы"),
                item("minecraft:experience_bottle", 10, 8, "RARE", "§9Опыт"),
                item("minecraft:netherite_scrap", 1, 2, "EPIC", "§5Осколок незерита")
        );

        // Донат-кейс — дороже, лучше призы, с pity-гарантией (после 15 открытий подряд без
        // EPIC+ приза — следующее открытие его гарантирует)
        CaseDefinition donate = new CaseDefinition();
        donate.id = "donate_case";
        donate.displayName = "§6§lДонат-кейс";
        donate.price = 500;
        donate.iconItemId = "minecraft:ender_chest";
        donate.pityThreshold = 15;
        donate.pityRarity = "EPIC";
        CaseItem rankReward = item("minecraft:name_tag", 1, 5, "LEGENDARY", "§6§lРанг VIP на 7 дней");
        rankReward.command = "lp user %player% parent addtemp vip 7d";
        donate.items = Arrays.asList(
                item("minecraft:diamond_block", 1, 30, "RARE", "§9Блок алмаза"),
                item("minecraft:netherite_ingot", 1, 15, "EPIC", "§5Слиток незерита"),
                item("minecraft:elytra", 1, 5, "LEGENDARY", "§6§lЭлитры"),
                item("minecraft:totem_of_undying", 1, 10, "EPIC", "§5Тотем бессмертия"),
                item("minecraft:shulker_box", 1, 20, "UNCOMMON", "§aШалкер бокс"),
                item("minecraft:enchanted_golden_apple", 2, 15, "RARE", "§9Зачарованное яблоко"),
                rankReward
        );

        // Сезонный/ивентовый кейс — пример использования availableFrom/availableUntil.
        // По умолчанию окно широкое (2024–2030), чтобы кейс был виден и открывался сразу после
        // установки мода для демонстрации — задайте свои даты под конкретный ивент.
        CaseDefinition event = new CaseDefinition();
        event.id = "event_case";
        event.displayName = "§d§lИвентовый кейс";
        event.price = 300;
        event.iconItemId = "minecraft:beacon";
        event.availableFrom = "2024-01-01";
        event.availableUntil = "2030-12-31";
        event.items = Arrays.asList(
                item("minecraft:nether_star", 1, 10, "EPIC", "§5Звезда Незера"),
                item("minecraft:dragon_egg", 1, 2, "LEGENDARY", "§6§lЯйцо дракона"),
                item("minecraft:diamond", 5, 40, "UNCOMMON", "§aАлмазы"),
                item("minecraft:gold_block", 2, 30, "COMMON", "§7Золотые блоки"),
                item("minecraft:emerald_block", 1, 18, "RARE", "§9Блок изумруда")
        );

        // Бесплатный кейс — открывается без валюты, доступен всем
        CaseDefinition free = new CaseDefinition();
        free.id = "free_case";
        free.displayName = "§aБесплатный кейс";
        free.price = 0;
        free.iconItemId = "minecraft:bundle";
        free.items = Arrays.asList(
                item("minecraft:bread", 3, 50, "COMMON", "§7Хлеб"),
                item("minecraft:apple", 3, 30, "COMMON", "§7Яблоко"),
                item("minecraft:iron_ingot", 2, 15, "UNCOMMON", "§aЖелезо"),
                item("minecraft:diamond", 1, 5, "RARE", "§9Алмаз")
        );

        for (CaseDefinition def : new CaseDefinition[]{common, donate, event, free}) {
            try (Writer w = new OutputStreamWriter(new FileOutputStream(dir.resolve(def.id + ".json").toFile()), "UTF-8")) {
                GSON.toJson(def, w);
            } catch (IOException e) {
                CasesMod.LOGGER.error("Не удалось создать пример кейса", e);
            }
        }
    }

    private static CaseItem item(String id, int count, double weight, String rarity, String name) {
        CaseItem it = new CaseItem();
        it.itemId = id; it.count = count; it.weight = weight; it.rarity = rarity; it.displayName = name;
        return it;
    }

    /** Выбирает случайный приз кейса с учётом весов. */
    public CaseItem roll(CaseDefinition def, Random random) {
        double total = def.totalWeight();
        double r = random.nextDouble() * total;
        double cursor = 0;
        for (CaseItem it : def.items) {
            cursor += it.weight;
            if (r <= cursor) return it;
        }
        return def.items.isEmpty() ? null : def.items.get(def.items.size() - 1);
    }

    /**
     * Для pity-системы: выбирает случайный приз только среди предметов с редкостью >= minRarity.
     * Если в кейсе таких предметов нет (неудачно настроен конфиг), откатывается на обычный roll().
     */
    public CaseItem rollWithMinRarity(CaseDefinition def, Random random, CaseItem.Rarity minRarity) {
        List<CaseItem> pool = new ArrayList<>();
        for (CaseItem it : def.items) {
            if (it.rarityEnum().ordinal() >= minRarity.ordinal()) pool.add(it);
        }
        if (pool.isEmpty()) return roll(def, random);
        double total = 0;
        for (CaseItem it : pool) total += it.weight;
        double r = random.nextDouble() * total;
        double cursor = 0;
        for (CaseItem it : pool) {
            cursor += it.weight;
            if (r <= cursor) return it;
        }
        return pool.get(pool.size() - 1);
    }

    /**
     * Доступен ли кейс для открытия прямо сейчас по датам availableFrom/availableUntil.
     * Ошибка парсинга даты трактуется как "доступно всегда" (fail-open), чтобы опечатка в конфиге
     * не заблокировала кейс наглухо и незаметно для админа — ошибка при этом пишется в лог.
     */
    public boolean isAvailable(CaseDefinition def) {
        java.time.LocalDate today = java.time.LocalDate.now();
        try {
            if (def.availableFrom != null && !def.availableFrom.isEmpty()) {
                java.time.LocalDate from = java.time.LocalDate.parse(def.availableFrom);
                if (today.isBefore(from)) return false;
            }
            if (def.availableUntil != null && !def.availableUntil.isEmpty()) {
                java.time.LocalDate until = java.time.LocalDate.parse(def.availableUntil);
                if (today.isAfter(until)) return false;
            }
        } catch (Exception e) {
            CasesMod.LOGGER.warn("Некорректная дата в кейсе {} (availableFrom/availableUntil), кейс считается доступным: {}",
                    def.id, e.toString());
        }
        return true;
    }

    private static Path caseFile(String id) {
        return Paths.get("config", "casesmod", "cases", id + ".json");
    }

    /** Создаёт или обновляет кейс и сразу пишет отдельный .json — видно без reload. */
    public void addOrUpdate(CaseDefinition def) {
        cases.put(def.id, def);
        try {
            Path file = caseFile(def.id);
            Files.createDirectories(file.getParent());
            try (Writer w = new OutputStreamWriter(new FileOutputStream(file.toFile()), "UTF-8")) {
                GSON.toJson(def, w);
            }
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка сохранения кейса {}", def.id, e);
        }
    }

    public void remove(String id) {
        cases.remove(id);
        try {
            Files.deleteIfExists(caseFile(id));
        } catch (IOException e) {
            CasesMod.LOGGER.error("Ошибка удаления кейса {}", id, e);
        }
    }
}
