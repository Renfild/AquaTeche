package store.aquateche.aqualumen.common.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import store.aquateche.aqualumen.AquaLumenUI;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Case catalog for the F4 hub, editable live via config/aqualumen/cases.json.
 * Mirrors the site catalog (ocean / fisher / depth) so both stay in one piece.
 */
public final class CaseConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/cases.json");

    private static Data cached;
    private static long cachedMtime;

    private CaseConfig() {
    }

    public static synchronized Data get() {
        try {
            long mtime = Files.getLastModifiedTime(FILE).toMillis();
            if (cached == null || mtime != cachedMtime) {
                if (!Files.exists(FILE)) {
                    Files.createDirectories(FILE.getParent());
                    Files.writeString(FILE, GSON.toJson(defaults()), StandardCharsets.UTF_8);
                }
                cached = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), Data.class);
                cachedMtime = mtime;
            }
        } catch (IOException | RuntimeException error) {
            AquaLumenUI.LOGGER.warn("Case config unreadable, using defaults: {}", error.toString());
            cached = defaults();
            cachedMtime = 0L;
        }
        return cached;
    }

    public static CaseDef find(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (CaseDef def : get().cases) {
            if (id.equals(def.id)) {
                return def;
            }
        }
        return null;
    }

    public static LootDef roll(CaseDef def, net.minecraft.util.RandomSource random) {
        int total = 0;
        for (LootDef loot : def.loot) {
            total += Math.max(1, loot.weight);
        }
        int pick = random.nextInt(Math.max(1, total));
        for (LootDef loot : def.loot) {
            pick -= Math.max(1, loot.weight);
            if (pick < 0) {
                return loot;
            }
        }
        return def.loot.get(def.loot.size() - 1);
    }

    public static int rollAmount(LootDef loot, net.minecraft.util.RandomSource random) {
        int min = Math.max(1, Math.min(loot.min, loot.max));
        int max = Math.max(min, Math.max(loot.min, loot.max));
        return min == max ? min : min + random.nextInt(max - min + 1);
    }

    public static final class Data {
        public List<CaseDef> cases = new ArrayList<>();
        public Daily daily = new Daily();
    }

    public static final class CaseDef {
        public String id = "case";
        public String title = "Кейс";
        public String rarity = "common";
        public int costCoins = 0;
        public List<LootDef> loot = new ArrayList<>();
    }

    public static final class LootDef {
        public String type = "item";
        public String item = "minecraft:air";
        public String label = "";
        public int min = 1;
        public int max = 1;
        public int weight = 1;
    }

    public static final class Daily {
        public int baseCoins = 150;
        public int streakBonusCoins = 50;
        public int maxStreak = 7;
    }

    private static Data defaults() {
        Data data = new Data();

        CaseDef ocean = new CaseDef();
        ocean.id = "ocean";
        ocean.title = "Океанский кейс";
        ocean.rarity = "common";
        ocean.costCoins = 250;
        ocean.loot.add(loot("coins", null, "AquaCoins", 10000, 50000, 45));
        ocean.loot.add(loot("item", "aquatech_ui:kelp_bio_pellet", "Биогранулы из водорослей", 16, 16, 30));
        ocean.loot.add(loot("item", "aquatech_ui:abyssal_magnet", "Магнит бездны", 1, 1, 15));
        ocean.loot.add(loot("item", "minecraft:experience_bottle", "Бутыльки опыта", 16, 16, 10));
        data.cases.add(ocean);

        CaseDef fisher = new CaseDef();
        fisher.id = "fisher";
        fisher.title = "Рыбацкий кейс";
        fisher.rarity = "rare";
        fisher.costCoins = 750;
        fisher.loot.add(loot("item", "starcatcher:iceborn_rod", "Ледяная удочка [T6]", 1, 1, 35));
        fisher.loot.add(loot("item", "starcatcher:starcatcher_rod", "Удочка Ловца Звёзд [T7]", 1, 1, 25));
        fisher.loot.add(loot("item", "starcatcher:azure_crystal_rod", "Лазурный кристалл [T8]", 1, 1, 20));
        fisher.loot.add(loot("item", "starcatcher:sharktooth_rod", "Акулий клык [T9]", 1, 1, 12));
        fisher.loot.add(loot("item", "starcatcher:magmaforged_rod", "Магматическая [T10]", 1, 1, 8));
        data.cases.add(fisher);

        CaseDef depth = new CaseDef();
        depth.id = "depth";
        depth.title = "Кейс Бездны";
        depth.rarity = "legendary";
        depth.costCoins = 2000;
        depth.loot.add(loot("gems", null, "Рамка профиля «Глубинная Бездна» (гемы ×2)", 2, 2, 40));
        depth.loot.add(loot("coins", null, "AquaCoins", 250000, 250000, 30));
        depth.loot.add(loot("gems", null, "Привилегия Deluxe 14 дней (гемы ×5)", 5, 5, 20));
        depth.loot.add(loot("gems", null, "Привилегия Ultimate 30 дней (гемы ×10)", 10, 10, 10));
        data.cases.add(depth);

        return data;
    }

    private static LootDef loot(String type, String item, String label, int min, int max, int weight) {
        LootDef def = new LootDef();
        def.type = type;
        def.item = item == null ? "" : item;
        def.label = label;
        def.min = min;
        def.max = max;
        def.weight = weight;
        return def;
    }
}
