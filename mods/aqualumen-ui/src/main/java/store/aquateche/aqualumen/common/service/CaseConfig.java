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
        ocean.title = "\u041e\u043a\u0435\u0430\u043d\u0441\u043a\u0438\u0439 \u043a\u0435\u0439\u0441";
        ocean.rarity = "common";
        ocean.costCoins = 500;
        ocean.loot.add(loot("coins", null, "AquaCoins", 60, 120, 20));
        ocean.loot.add(loot("item", "minecraft:iron_ore", "\u0416\u0435\u043b\u0435\u0437\u043d\u0430\u044f \u0440\u0443\u0434\u0430", 4, 8, 15));
        ocean.loot.add(loot("item", "industrialupgrade:classicore/tin", "\u041e\u043b\u043e\u0432\u044f\u043d\u043d\u0430\u044f \u0440\u0443\u0434\u0430", 3, 6, 14));
        ocean.loot.add(loot("item", "minecraft:redstone", "\u0420\u0435\u0434\u0441\u0442\u043e\u0443\u043d", 4, 8, 12));
        ocean.loot.add(loot("item", "minecraft:copper_ingot", "\u041c\u0435\u0434\u043d\u044b\u0435 \u0441\u043b\u0438\u0442\u043a\u0438", 4, 8, 12));
        ocean.loot.add(loot("item", "minecraft:lapis_lazuli", "\u041b\u0430\u0437\u0443\u0440\u0438\u0442", 3, 6, 10));
        ocean.loot.add(loot("item", "minecraft:slime_ball", "\u0421\u043b\u0438\u0437\u043a\u0438\u0435 \u0448\u0430\u0440\u044b", 4, 9, 9));
        ocean.loot.add(loot("item", "minecraft:string", "\u041d\u0438\u0442\u044c", 4, 8, 4));
        ocean.loot.add(loot("item", "minecraft:experience_bottle", "\u0411\u0443\u0442\u044b\u043b\u044c\u043a\u0438 \u043e\u043f\u044b\u0442\u0430", 4, 8, 4));
        data.cases.add(ocean);

        CaseDef fisher = new CaseDef();
        fisher.id = "fisher";
        fisher.title = "\u0420\u044b\u0431\u0430\u0446\u043a\u0438\u0439 \u043a\u0435\u0439\u0441";
        fisher.rarity = "rare";
        fisher.costCoins = 1500;
        fisher.loot.add(loot("item", "industrialupgrade:baseore/silver", "\u0421\u0435\u0440\u0435\u0431\u0440\u043e \u00d72\u20134", 2, 4, 15));
        fisher.loot.add(loot("item", "industrialupgrade:baseore/aluminium", "\u0410\u043b\u044e\u043c\u0438\u043d\u0438\u0439 \u00d72\u20134", 2, 4, 13));
        fisher.loot.add(loot("item", "industrialupgrade:preciousgem/sapphire_gem", "\u0421\u0430\u043f\u0444\u0438\u0440", 1, 2, 12));
        fisher.loot.add(loot("item", "industrialupgrade:baseore/tungsten", "\u0412\u043e\u043b\u044c\u0444\u0440\u0430\u043c \u00d71\u20133", 1, 3, 11));
        fisher.loot.add(loot("item", "industrialupgrade:baseore/chromium", "\u0425\u0440\u043e\u043c \u00d71\u20133", 1, 3, 11));
        fisher.loot.add(loot("item", "minecraft:iron_ore", "\u0416\u0435\u043b\u0435\u0437\u043d\u0430\u044f \u0440\u0443\u0434\u0430 \u00d76\u201312", 6, 12, 13));
        fisher.loot.add(loot("item", "industrialupgrade:baseore/cobalt", "\u041a\u043e\u0431\u0430\u043b\u044c\u0442 \u00d71\u20132", 1, 2, 8));
        fisher.loot.add(loot("item", "minecraft:experience_bottle", "\u0411\u0443\u0442\u044b\u043b\u044c\u043a\u0438 \u043e\u043f\u044b\u0442\u0430", 8, 16, 6));
        fisher.loot.add(loot("item", "starcatcher:iceborn_rod", "\u041b\u0435\u0434\u044f\u043d\u0430\u044f \u0443\u0434\u043e\u0447\u043a\u0430 [T6]", 1, 1, 4));
        fisher.loot.add(loot("item", "starcatcher:starcatcher_rod", "\u0423\u0434\u043e\u0447\u043a\u0430 \u041b\u043e\u0432\u0446\u0430 \u0417\u0432\u0451\u0437\u0434 [T7]", 1, 1, 3));
        fisher.loot.add(loot("item", "starcatcher:azure_crystal_rod", "\u041b\u0430\u0437\u0443\u0440\u043d\u044b\u0439 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b [T8]", 1, 1, 2));
        fisher.loot.add(loot("item", "starcatcher:sharktooth_rod", "\u0410\u043a\u0443\u043b\u0438\u0439 \u043a\u043b\u044b\u043a [T9]", 1, 1, 2));
        data.cases.add(fisher);

        CaseDef depth = new CaseDef();
        depth.id = "depth";
        depth.title = "\u041a\u0435\u0439\u0441 \u0411\u0435\u0437\u0434\u043d\u044b";
        depth.rarity = "legendary";
        depth.costCoins = 5000;
        depth.loot.add(loot("item", "industrialupgrade:baseore/platinum", "\u041f\u043b\u0430\u0442\u0438\u043d\u0430 \u00d72\u20134", 2, 4, 13));
        depth.loot.add(loot("item", "minecraft:diamond", "\u0410\u043b\u043c\u0430\u0437\u044b \u00d72\u20134", 2, 4, 12));
        depth.loot.add(loot("item", "industrialupgrade:crushed/uranium", "\u0414\u0440\u043e\u0431\u043b\u0451\u043d\u044b\u0439 \u0443\u0440\u0430\u043d \u00d71\u20133", 1, 3, 11));
        depth.loot.add(loot("item", "industrialupgrade:alloyingot/inconel", "\u0418\u043d\u043a\u043e\u043d\u0435\u043b\u044c \u00d71\u20132", 1, 2, 11));
        depth.loot.add(loot("gems", null, "\u0413\u0435\u043c\u044b \u00d72\u20133", 2, 3, 9));
        depth.loot.add(loot("item", "minecraft:heart_of_the_sea", "\u0421\u0435\u0440\u0434\u0446\u0435 \u043c\u043e\u0440\u044f", 1, 1, 8));
        depth.loot.add(loot("item", "industrialupgrade:alloyingot/osmiridium", "\u041e\u0441\u043c\u0438\u0440\u0438\u0434\u0438\u0439 \u00d71\u20132", 1, 2, 8));
        depth.loot.add(loot("item", "industrialupgrade:asteroidore/asteroid_adamantium_ore", "\u0410\u0434\u0430\u043c\u0430\u043d\u0442\u0438\u0435\u0432\u0430\u044f \u0440\u0443\u0434\u0430 \u00d71\u20132", 1, 2, 7));
        depth.loot.add(loot("item", "starcatcher:lush_glowberry_rod", "\u0421\u0432\u0435\u0442\u044f\u0449\u0430\u044f\u0441\u044f \u044f\u0433\u043e\u0434\u0430 [T11]", 1, 1, 6));
        depth.loot.add(loot("item", "starcatcher:obsidian_rod", "\u041e\u0431\u0441\u0438\u0434\u0438\u0430\u043d\u043e\u0432\u0430\u044f [T10]", 1, 1, 5));
        depth.loot.add(loot("item", "starcatcher:magmaforged_rod", "\u041c\u0430\u0433\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0430\u044f [T12]", 1, 1, 4));
        depth.loot.add(loot("item", "starcatcher:alpha_rod", "\u0410\u043b\u044c\u0444\u0430 [T13]", 1, 1, 3));
        depth.loot.add(loot("item", "minecraft:nether_star", "\u0417\u0432\u0435\u0437\u0434\u0430 \u041d\u0435\u0437\u0435\u0440\u0430", 1, 1, 3));
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
