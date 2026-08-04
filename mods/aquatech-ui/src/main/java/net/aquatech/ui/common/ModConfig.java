package net.aquatech.ui.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> SERVER_DOMAIN;
    public static final ForgeConfigSpec.ConfigValue<String> SERVER_NAME;
    public static final ForgeConfigSpec.IntValue SYNC_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue BUBBLE_DURATION_TICKS;
    public static final ForgeConfigSpec.ConfigValue<String> TIPS;
    public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> RANK_WEIGHTS;
    public static final ForgeConfigSpec.BooleanValue AUTO_STORM_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> STORM_TIMEZONE;
    public static final ForgeConfigSpec.BooleanValue FISHING_MINIGAME;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("general");
        SERVER_DOMAIN = builder.define("serverDomain", "bk.aquatech.net");
        SERVER_NAME = builder.define("serverName", "AquaTech Ocean Horizon");
        SYNC_INTERVAL_TICKS = builder.defineInRange("syncIntervalTicks", 40, 5, 200);
        BUBBLE_DURATION_TICKS = builder.defineInRange("bubbleDurationTicks", 100, 20, 600);
        TIPS = builder.define("tips",
                "Совет: улучшай Удочку Ресурсов для редкого улова|Совет: океанское дно опасно — готовь броню|Совет: /ftbquests откроет сюжет и награды|Совет: рыбалка — главный источник ресурсов на старте");
        RANK_WEIGHTS = builder
                .comment(
                        "Веса групп LuckPerms для сортировки TAB и выбора главного ранга.",
                        "Формат: groupId=weight. Добавь свои привилегии сюда."
                )
                .defineList(
                        "rankWeights",
                        java.util.List.of(
                                "owner=100",
                                "admin=80",
                                "developer=75",
                                "mod=60",
                                "staff=55",
                                "helper=50",
                                "manager=48",
                                "legend=50",
                                "admiral=45",
                                "vipplus=42",
                                "vip=40",
                                "streamer=35",
                                "twitch=34",
                                "youtuber=34",
                                "artist=32",
                                "captain=30",
                                "builder=28",
                                "skipper=25",
                                "friend=22",
                                "sailor=20",
                                "trainee=18",
                                "default=10",
                                "npc=5"
                        ),
                        o -> o instanceof String s && s.contains("=")
                );
        builder.pop();
        builder.push("storm");
        AUTO_STORM_ENABLED = builder
                .comment("Авто-шторм каждую пятницу–воскресенье по stormTimezone")
                .define("autoStormEnabled", true);
        STORM_TIMEZONE = builder
                .comment("Часовой пояс расписания шторма (IANA), например Europe/Moscow")
                .define("stormTimezone", "Europe/Moscow");
        builder.pop();
        builder.push("fishing");
        FISHING_MINIGAME = builder
                .comment("Rhythm Hook mini-game on AquaTech rod catch (false = instant loot)")
                .define("tideTensionEnabled", true);
        builder.pop();
        SPEC = builder.build();
    }
}
