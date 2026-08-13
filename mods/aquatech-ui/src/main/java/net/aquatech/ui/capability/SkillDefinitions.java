package net.aquatech.ui.capability;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for the ocean skill tree (~55 nodes).
 * Costs: SMALL=1, NOTABLE=2, KEYSTONE=3. Effect text must match SkillEffects.
 */
public final class SkillDefinitions {

    public enum NodeType { SMALL, NOTABLE, KEYSTONE }

    public enum SkillCategory {
        NONE,
        FISHING_SPEED,
        CATCH_MULT,
        RARE_LOOT,
        MACHINE_SPEED,
        FE_GEN,
        FE_EFFICIENCY,
        SWIM_SPEED,
        AIR,
        WATER_BREATHING,
        NIGHT_VISION_WATER,
        WATER_RESIST,
        KELP_HARVEST,
        REGEN_WATER,
        ALL_BONUS
    }

    public record SkillDef(
            String id,
            NodeType type,
            int cost,
            String prereq,
            SkillCategory category,
            float value,
            String title,
            String effectText
    ) {
        public String typeLabel() {
            return switch (type) {
                case SMALL -> "Базовый";
                case NOTABLE -> "Усиленный";
                case KEYSTONE -> "Созвездие";
            };
        }
    }

    private static final Map<String, SkillDef> BY_ID = new LinkedHashMap<>();

    static {
        // ── HUB ──────────────────────────────────────────────────────────────
        put("origin", NodeType.KEYSTONE, 0, null, SkillCategory.NONE, 0f,
                "Исток Океана", "Открывает пять ветвей созвездий. Без бонуса.");

        // ── HUB RING ─────────────────────────────────────────────────────────
        put("inner_angler", NodeType.SMALL, 1, "origin", SkillCategory.FISHING_SPEED, 0.08f,
                "Школа Рыболова", "Скорость рыбалки +8% (ручная и авто).");
        put("inner_tech", NodeType.SMALL, 1, "origin", SkillCategory.FE_GEN, 0.08f,
                "Морская Схема", "Выработка FE реактора +8%.");
        put("inner_luck", NodeType.SMALL, 1, "origin", SkillCategory.RARE_LOOT, 0.08f,
                "Удача Морей", "Шанс редкого улова +8%.");
        put("inner_diving", NodeType.SMALL, 1, "origin", SkillCategory.AIR, 40f,
                "Глубокий Вдох", "Запас воздуха +2 сек.");
        put("inner_kelp", NodeType.SMALL, 1, "origin", SkillCategory.KELP_HARVEST, 0.25f,
                "Органический Исток", "Дроп водорослей +25%.");

        // ── SECTOR 1 — ANGLER (North) ────────────────────────────────────────
        put("rod_speed_1", NodeType.SMALL, 1, "inner_angler", SkillCategory.FISHING_SPEED, 0.08f,
                "Ловкие Руки I", "Скорость рыбалки +8%.");
        put("bait_sense", NodeType.SMALL, 1, "rod_speed_1", SkillCategory.FISHING_SPEED, 0.05f,
                "Чутьё Наживки", "Скорость рыбалки +5%.");
        put("double_catch", NodeType.NOTABLE, 2, "bait_sense", SkillCategory.CATCH_MULT, 0.15f,
                "Двойной Крючок", "Шанс удвоить улов +15%.");
        put("rod_speed_2", NodeType.SMALL, 1, "double_catch", SkillCategory.FISHING_SPEED, 0.10f,
                "Ловкие Руки II", "Скорость рыбалки +10%.");
        put("triple_hook", NodeType.NOTABLE, 2, "rod_speed_2", SkillCategory.CATCH_MULT, 0.10f,
                "Тройной Заброс", "Шанс удвоить улов +10%.");
        put("flood_rhythm", NodeType.SMALL, 1, "triple_hook", SkillCategory.RARE_LOOT, 0.08f,
                "Прилив Удачи", "Шанс редкого улова +8%.");
        put("five_hook", NodeType.NOTABLE, 2, "flood_rhythm", SkillCategory.CATCH_MULT, 0.10f,
                "Пятизубый Охват", "Шанс удвоить улов +10%.");
        put("casting_mastery", NodeType.SMALL, 1, "five_hook", SkillCategory.FISHING_SPEED, 0.05f,
                "Мастерство Заброса", "Скорость рыбалки +5%.");
        put("master_angler", NodeType.KEYSTONE, 3, "casting_mastery", SkillCategory.ALL_BONUS, 0.20f,
                "Владыка Удочки", "Все бонусы рыбалки (скорость и улов) +20%.");

        // ── SECTOR 2 — TECH (East) ───────────────────────────────────────────
        put("fe_collector", NodeType.SMALL, 1, "inner_tech", SkillCategory.FE_GEN, 0.08f,
                "Магнитная Индукция", "Выработка FE +8%.");
        put("efficiency_1", NodeType.SMALL, 1, "fe_collector", SkillCategory.FE_EFFICIENCY, 0.08f,
                "Энерго-Сбережение", "Расход FE машин −8%.");
        put("speed_boost_1", NodeType.NOTABLE, 2, "efficiency_1", SkillCategory.MACHINE_SPEED, 0.10f,
                "Турбо-Привод I", "Скорость машин +10%.");
        put("machine_cooling", NodeType.SMALL, 1, "speed_boost_1", SkillCategory.FE_EFFICIENCY, 0.05f,
                "Охлаждение Машин", "Расход FE −5%.");
        put("speed_boost_2", NodeType.NOTABLE, 2, "machine_cooling", SkillCategory.MACHINE_SPEED, 0.10f,
                "Турбо-Привод II", "Скорость машин +10%.");
        put("overclock", NodeType.NOTABLE, 2, "speed_boost_2", SkillCategory.MACHINE_SPEED, 0.15f,
                "Оверклокинг Ядра", "Скорость машин +15%.");
        put("zero_waste", NodeType.SMALL, 1, "overclock", SkillCategory.FE_EFFICIENCY, 0.10f,
                "Нулевые Потери", "Расход FE −10%.");
        put("deep_regen", NodeType.SMALL, 1, "zero_waste", SkillCategory.FE_GEN, 0.10f,
                "Глубокая Регенерация", "Выработка FE +10%.");
        put("overdrive_machine", NodeType.KEYSTONE, 3, "deep_regen", SkillCategory.ALL_BONUS, 0.20f,
                "Гипер-Двигатель", "Скорость машин и выработка FE +20%.");

        // ── SECTOR 3 — LUCK (South-East) ─────────────────────────────────────
        put("luck_1", NodeType.SMALL, 1, "inner_luck", SkillCategory.RARE_LOOT, 0.08f,
                "Чутьё Рыбака I", "Шанс редкого улова +8%.");
        put("lucky_cast", NodeType.SMALL, 1, "luck_1", SkillCategory.RARE_LOOT, 0.05f,
                "Счастливый Заброс", "Шанс редкого улова +5%.");
        put("luck_2", NodeType.NOTABLE, 2, "lucky_cast", SkillCategory.RARE_LOOT, 0.10f,
                "Чутьё Рыбака II", "Шанс редкого улова +10%.");
        put("treasure_map", NodeType.SMALL, 1, "luck_2", SkillCategory.RARE_LOOT, 0.05f,
                "Карта Сокровищ", "Шанс редкого улова +5%.");
        put("chest_finder", NodeType.NOTABLE, 2, "treasure_map", SkillCategory.RARE_LOOT, 0.08f,
                "Затерянные Сундуки", "Шанс редкого улова +8%.");
        put("gem_miner", NodeType.NOTABLE, 2, "chest_finder", SkillCategory.RARE_LOOT, 0.10f,
                "Самоцветный Улов", "Шанс редкого улова +10%.");
        put("abyssal_loot", NodeType.SMALL, 1, "gem_miner", SkillCategory.RARE_LOOT, 0.08f,
                "Дары Глубин", "Шанс редкого улова +8%.");
        put("sunken_relic", NodeType.NOTABLE, 2, "abyssal_loot", SkillCategory.RARE_LOOT, 0.10f,
                "Реликвия Затонувших", "Шанс редкого улова +10%.");
        put("poseidon_blessing", NodeType.KEYSTONE, 3, "sunken_relic", SkillCategory.ALL_BONUS, 0.20f,
                "Благословение Посейдона", "Шанс редкого улова +20%.");

        // ── SECTOR 4 — DIVING (South-West) ───────────────────────────────────
        put("swim_speed", NodeType.SMALL, 1, "inner_diving", SkillCategory.SWIM_SPEED, 0.08f,
                "Морская Грация", "Скорость плавания +8%.");
        put("lung_expand", NodeType.SMALL, 1, "swim_speed", SkillCategory.AIR, 40f,
                "Лёгкие Водолаза", "Запас воздуха +2 сек.");
        put("water_breathing", NodeType.NOTABLE, 2, "lung_expand", SkillCategory.WATER_BREATHING, 1f,
                "Аква-Лёгкие", "Дыхание под водой (эффект Water Breathing).");
        put("current_rider", NodeType.SMALL, 1, "water_breathing", SkillCategory.SWIM_SPEED, 0.10f,
                "Ездок на Течении", "Скорость плавания +10%.");
        put("night_vision", NodeType.NOTABLE, 2, "current_rider", SkillCategory.NIGHT_VISION_WATER, 1f,
                "Очи Бездны", "Ночное зрение под водой.");
        put("depth_armor", NodeType.NOTABLE, 2, "night_vision", SkillCategory.WATER_RESIST, 0.10f,
                "Давление Бездны", "Запас глубины +6 м, урон −10%.");
        put("pressure_resist", NodeType.SMALL, 1, "depth_armor", SkillCategory.WATER_RESIST, 0.10f,
                "Стойкость к Давлению", "Запас глубины +6 м, урон −10%.");
        put("tide_walker", NodeType.SMALL, 1, "pressure_resist", SkillCategory.SWIM_SPEED, 0.08f,
                "Ходок По Воде", "Скорость плавания +8%.");
        put("immortal_diver", NodeType.KEYSTONE, 3, "tide_walker", SkillCategory.ALL_BONUS, 0.20f,
                "Неуязвимый Водолаз", "Плавание +15%, дыхание под водой, урон −10%.");

        // ── SECTOR 5 — BIO (West) ────────────────────────────────────────────
        put("kelp_harvest", NodeType.SMALL, 1, "inner_kelp", SkillCategory.KELP_HARVEST, 0.25f,
                "Ламинариевый Сбор", "Дроп водорослей +25%.");
        put("algae_study", NodeType.SMALL, 1, "kelp_harvest", SkillCategory.KELP_HARVEST, 0.25f,
                "Изучение Водорослей", "Дроп водорослей +25%.");
        put("bio_fuel", NodeType.NOTABLE, 2, "algae_study", SkillCategory.FE_GEN, 0.15f,
                "Био-Синтез", "Выработка FE реактора +15%.");
        put("sea_grass_farm", NodeType.SMALL, 1, "bio_fuel", SkillCategory.KELP_HARVEST, 0.25f,
                "Морской Луг", "Дроп водорослей +25%.");
        put("sponge_grower", NodeType.NOTABLE, 2, "sea_grass_farm", SkillCategory.KELP_HARVEST, 0.25f,
                "Губочный Сад", "Дроп водорослей +25%.");
        put("living_kelp", NodeType.NOTABLE, 2, "sponge_grower", SkillCategory.REGEN_WATER, 1f,
                "Живые Водоросли", "Регенерация I в воде.");
        put("sea_garden", NodeType.SMALL, 1, "living_kelp", SkillCategory.KELP_HARVEST, 0.25f,
                "Морской Сад", "Дроп водорослей +25%.");
        put("mega_bloom", NodeType.NOTABLE, 2, "sea_garden", SkillCategory.KELP_HARVEST, 0.50f,
                "Мега-Цветение", "Дроп водорослей +50%.");
        put("immortal_organism", NodeType.KEYSTONE, 3, "mega_bloom", SkillCategory.ALL_BONUS, 0.20f,
                "Океанический Био-Разум", "Регенерация II в воде, дроп водорослей +25%.");

        // ── CROSS-SECTOR (4) ─────────────────────────────────────────────────
        put("ocean_harmony", NodeType.NOTABLE, 2, "master_angler", SkillCategory.ALL_BONUS, 0.03f,
                "Гармония Океана", "Все процентные бонусы навыков +3%.");
        put("tide_sync", NodeType.SMALL, 1, "ocean_harmony", SkillCategory.RARE_LOOT, 0.05f,
                "Синхрон Прилива", "Шанс редкого улова +5%.");
        put("kelp_cast", NodeType.SMALL, 1, "immortal_organism", SkillCategory.FISHING_SPEED, 0.05f,
                "Водорослевый Заброс", "Скорость рыбалки +5%.");
        put("deep_resonance", NodeType.NOTABLE, 2, "kelp_cast", SkillCategory.ALL_BONUS, 0.05f,
                "Глубинный Резонанс", "Все процентные бонусы навыков +5%.");
    }

    private static void put(String id, NodeType type, int cost, String prereq,
                            SkillCategory category, float value, String title, String effectText) {
        BY_ID.put(id, new SkillDef(id, type, cost, prereq, category, value, title, effectText));
    }

    private SkillDefinitions() {
    }

    public static SkillDef get(String id) {
        return BY_ID.get(id);
    }

    public static boolean isKnown(String id) {
        return id != null && BY_ID.containsKey(id);
    }

    public static Collection<SkillDef> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static int size() {
        return BY_ID.size();
    }

    public static int costOf(String id) {
        SkillDef def = BY_ID.get(id);
        return def == null ? 1 : Math.max(0, def.cost());
    }

    public static boolean canUnlock(String skillId, java.util.Set<String> unlocked) {
        if (!isKnown(skillId) || unlocked.contains(skillId)) return false;
        if ("origin".equals(skillId)) return false; // granted separately / free hub
        SkillDef def = BY_ID.get(skillId);
        String prereq = def.prereq();
        if (prereq == null) return true;
        if ("origin".equals(prereq)) return true; // origin is always considered unlocked
        return unlocked.contains(prereq);
    }
}
