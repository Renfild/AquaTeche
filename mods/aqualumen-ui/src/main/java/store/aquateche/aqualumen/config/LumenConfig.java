package store.aquateche.aqualumen.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/** Server balance / behaviour lives in COMMON, look and feel lives in CLIENT. */
public final class LumenConfig {

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<Common, ForgeConfigSpec> common = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = common.getLeft();
        COMMON_SPEC = common.getRight();

        Pair<Client, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();
    }

    private LumenConfig() {
    }

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue hubEnabled;
        public final ForgeConfigSpec.BooleanValue chestFallback;
        public final ForgeConfigSpec.IntValue actionCooldownMs;
        public final ForgeConfigSpec.IntValue snapshotIntervalTicks;
        public final ForgeConfigSpec.ConfigValue<String> serverName;
        public final ForgeConfigSpec.ConfigValue<String> seasonTitle;
        public final ForgeConfigSpec.IntValue seasonMaxTier;
        public final ForgeConfigSpec.ConfigValue<String> coinsObjective;
        public final ForgeConfigSpec.ConfigValue<String> gemsObjective;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> ranks;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> enabledTabs;

        Common(ForgeConfigSpec.Builder b) {
            b.comment("AquaLumen UI - server side settings").push("hub");
            hubEnabled = b.comment("Master switch for the hub interface.").define("enabled", true);
            chestFallback = b.comment("Open a chest-based hub for players without the client mod (vanilla clients on Mohist).")
                    .define("chestFallback", true);
            actionCooldownMs = b.comment("Minimum delay between two hub actions of one player, in milliseconds.")
                    .defineInRange("actionCooldownMs", 350, 0, 10_000);
            snapshotIntervalTicks = b.comment("How often an open hub is refreshed for a player, in ticks.")
                    .defineInRange("snapshotIntervalTicks", 40, 5, 600);
            b.pop();

            b.comment("Season / battle pass").push("season");
            serverName = b.define("serverName", "aquaTeche \u2022 TechnoAqua");
            seasonTitle = b.define("title", "\u0421\u0435\u0437\u043e\u043d 1 \u2014 \u0413\u043b\u0443\u0431\u0438\u043d\u0430");
            seasonMaxTier = b.defineInRange("maxTier", 50, 1, 500);
            b.pop();

            b.comment("Economy bridge. Scoreboard objectives are used when no economy plugin is detected.").push("economy");
            coinsObjective = b.define("coinsObjective", "coins");
            gemsObjective = b.define("gemsObjective", "gems");
            b.pop();

            b.comment("Content").push("content");
            ranks = b.comment("Rank ladder as 'name:requiredLevel:hexColor'.")
                    .defineList("ranks", List.of(
                            "\u041d\u043e\u0432\u0438\u0447\u043e\u043a:0:8FA6B8",
                            "\u0414\u0430\u0439\u0432\u0435\u0440:10:2FE0C0",
                            "\u0418\u043d\u0436\u0435\u043d\u0435\u0440:25:3B9DFF",
                            "\u0410\u0431\u0438\u0441\u0441:40:9B7BFF",
                            "\u041b\u0435\u0433\u0435\u043d\u0434\u0430:60:F5C25B"), o -> o instanceof String);
            enabledTabs = b.comment("Tabs shown in the sidebar, in order.")
                    .defineList("enabledTabs", List.of("profile", "store", "cases", "pass", "fishing", "events", "auction", "kits", "warps", "tops", "settings"), o -> o instanceof String);
            b.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.ConfigValue<String> theme;
        public final ForgeConfigSpec.ConfigValue<String> accentOverride;
        public final ForgeConfigSpec.DoubleValue panelOpacity;
        public final ForgeConfigSpec.BooleanValue animations;
        public final ForgeConfigSpec.BooleanValue blurBackground;
        public final ForgeConfigSpec.BooleanValue compactMode;
        public final ForgeConfigSpec.BooleanValue sounds;
        public final ForgeConfigSpec.BooleanValue webChat;

        Client(ForgeConfigSpec.Builder b) {
            b.comment("AquaLumen UI - look and feel").push("theme");
            theme = b.comment("aqua_lumen | violet_lumen | midnight_rose").define("preset", "aqua_lumen");
            accentOverride = b.comment("Optional accent colour override, RRGGBB. Empty = use preset.").define("accentOverride", "");
            panelOpacity = b.defineInRange("panelOpacity", 0.86D, 0.35D, 1.0D);
            blurBackground = b.define("blurBackground", true);
            animations = b.define("animations", true);
            compactMode = b.comment("Denser layout for 1080p windows and low GUI scale.").define("compactMode", false);
            sounds = b.define("sounds", true);
            webChat = b.comment("Web-rendered chat overlay (cards, avatars, channels). False = vanilla chat.")
                    .define("webChat", true);
            b.pop();
        }
    }
}
