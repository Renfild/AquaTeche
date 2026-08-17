package store.aquateche.aqualumen.common.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.common.ServerEvents;
import store.aquateche.aqualumen.common.compat.ChestFallbackUI;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.config.LumenConfig;
import store.aquateche.aqualumen.network.LumenNetwork;
import store.aquateche.aqualumen.network.LumenPackets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the server authoritative snapshot and decides which UI a player gets.
 *
 * <p>Integration points for a real project (economy plugin, quests, store back end) are marked
 * with {@code TODO bridge}. Everything else already works on a bare Forge / Mohist server.</p>
 */
public final class HubDataService {

    private static final Set<UUID> OPEN_HUBS = ConcurrentHashMap.newKeySet();

    private HubDataService() {
    }

    public static void open(ServerPlayer player) {
        if (!LumenConfig.COMMON.hubEnabled.get()) {
            return;
        }
        HubSnapshot snapshot = build(player);

        if (ServerEvents.hasClientMod(player)) {
            OPEN_HUBS.add(player.getUUID());
            LumenNetwork.toPlayer(player, new LumenPackets.HubSync(snapshot, true));
            return;
        }
        if (LumenConfig.COMMON.chestFallback.get()) {
            ChestFallbackUI.open(player, snapshot);
            player.sendSystemMessage(Component.translatable("msg.aqualumen.fallback").withStyle(ChatFormatting.GRAY));
        }
    }

    public static void push(ServerPlayer player) {
        if (ServerEvents.hasClientMod(player)) {
            LumenNetwork.toPlayer(player, new LumenPackets.HubSync(build(player), false));
        }
    }

    public static void refreshOpenHubs(MinecraftServer server) {
        if (OPEN_HUBS.isEmpty()) {
            return;
        }
        for (UUID id : OPEN_HUBS) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                push(player);
            }
        }
    }

    public static void closeFor(UUID id) {
        OPEN_HUBS.remove(id);
    }

    public static void invalidate() {
        OPEN_HUBS.clear();
    }

    public static String status() {
        return "AquaLumen UI: " + OPEN_HUBS.size() + " open hub(s)";
    }

    public static HubSnapshot build(ServerPlayer player) {
        MinecraftServer server = player.server;

        long playtimeMinutes = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 1200L;
        int kills = player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        int deaths = player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int level = (int) Math.min(99, 1 + playtimeMinutes / 60);
        float levelProgress = (playtimeMinutes % 60) / 60.0F;

        Rank rank = rankFor(level);
        long coins = score(player, LumenConfig.COMMON.coinsObjective.get());
        long gems = score(player, LumenConfig.COMMON.gemsObjective.get());

        HubSnapshot.Profile profile = new HubSnapshot.Profile(
                player.getGameProfile().getName(), rank.name(), rank.color(), level, levelProgress,
                playtimeMinutes, kills, deaths, /* TODO bridge: quest system */ 0,
                Math.max(0, server.getPlayerCount() - 1));

        HubSnapshot.Wallet wallet = new HubSnapshot.Wallet(coins, gems,
                /* TODO bridge: daily streak storage */ 1, true);

        int maxTier = LumenConfig.COMMON.seasonMaxTier.get();
        int tier = Math.min(maxTier, level);
        HubSnapshot.Season season = new HubSnapshot.Season(LumenConfig.COMMON.seasonTitle.get(),
                tier, maxTier, levelProgress, false, /* TODO bridge: claimable rewards */ 0);

        return new HubSnapshot(profile, wallet, season, tops(server, player), defaultStore(), defaultCases(),
                serverInfo(server));
    }

    private static HubSnapshot.ServerInfo serverInfo(MinecraftServer server) {
        float tps = Math.min(20.0F, 1000.0F / Math.max(1.0F, msPerTick(server)));
        return new HubSnapshot.ServerInfo(LumenConfig.COMMON.serverName.get(), server.getPlayerCount(),
                server.getMaxPlayers(), tps, AquaLumenUI.VERSION);
    }

    private static float msPerTick(MinecraftServer server) {
        long total = 0L;
        long[] times = server.tickTimes;
        if (times == null || times.length == 0) {
            return 50.0F;
        }
        for (long time : times) {
            total += time;
        }
        return (total / (float) times.length) / 1_000_000.0F;
    }

    private static List<HubSnapshot.TopEntry> tops(MinecraftServer server, ServerPlayer self) {
        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
        players.sort(Comparator.comparingInt(
                (ServerPlayer p) -> p.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))).reversed());

        List<HubSnapshot.TopEntry> entries = new ArrayList<>();
        int place = 1;
        for (ServerPlayer player : players) {
            if (place > 10) {
                break;
            }
            long hours = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 72000L;
            entries.add(new HubSnapshot.TopEntry(place, player.getGameProfile().getName(), hours + " \u0447",
                    player.getUUID().equals(self.getUUID())));
            place++;
        }
        return entries;
    }

    /** TODO bridge: replace with the real store back end. */
    private static List<HubSnapshot.Offer> defaultStore() {
        return List.of(
                new HubSnapshot.Offer("rank.diver", "\u0420\u0430\u043d\u0433 Diver", "\u0414\u043e\u0441\u0442\u0443\u043f \u043a /home x5", 349, "gems", "-20%", false),
                new HubSnapshot.Offer("rank.abyss", "\u0420\u0430\u043d\u0433 Abyss", "\u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442 \u0432\u0445\u043e\u0434\u0430", 899, "gems", "HIT", false),
                new HubSnapshot.Offer("kit.starter", "\u041d\u0430\u0431\u043e\u0440 Starter", "\u0421\u0442\u0430\u0440\u0442\u043e\u0432\u043e\u0435 \u0441\u043d\u0430\u0440\u044f\u0436\u0435\u043d\u0438\u0435", 120, "coins", "", true),
                new HubSnapshot.Offer("boost.xp", "\u0411\u0443\u0441\u0442 \u043e\u043f\u044b\u0442\u0430 x2", "\u0414\u0435\u0439\u0441\u0442\u0432\u0443\u0435\u0442 2 \u0447\u0430\u0441\u0430", 60, "coins", "", false),
                new HubSnapshot.Offer("pet.axolotl", "\u041f\u0438\u0442\u043e\u043c\u0435\u0446 \u0410\u043a\u0441\u043e\u043b\u043e\u0442\u043b\u044c", "\u041a\u043e\u0441\u043c\u0435\u0442\u0438\u043a\u0430", 240, "gems", "NEW", false),
                new HubSnapshot.Offer("home.slot", "+1 \u0442\u043e\u0447\u043a\u0430 \u0434\u043e\u043c\u0430", "\u041f\u043e\u0441\u0442\u043e\u044f\u043d\u043d\u043e", 150, "coins", "", false));
    }

    /** TODO bridge: replace with the real case inventory. */
    private static List<HubSnapshot.CaseEntry> defaultCases() {
        return List.of(
                new HubSnapshot.CaseEntry("case.common", "\u041e\u0431\u044b\u0447\u043d\u044b\u0439 \u043a\u0435\u0439\u0441", 3, "common"),
                new HubSnapshot.CaseEntry("case.rare", "\u0420\u0435\u0434\u043a\u0438\u0439 \u043a\u0435\u0439\u0441", 1, "rare"),
                new HubSnapshot.CaseEntry("case.abyss", "\u0410\u0431\u0438\u0441\u0441\u0430\u043b\u044c\u043d\u044b\u0439 \u043a\u0435\u0439\u0441", 0, "legendary"));
    }

    private static long score(ServerPlayer player, String objectiveName) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            return 0L;
        }
        return scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective).getScore();
    }

    private static Rank rankFor(int level) {
        Rank best = new Rank("Player", 0x8FA6B8);
        for (String raw : LumenConfig.COMMON.ranks.get()) {
            String[] parts = raw.split(":");
            if (parts.length < 3) {
                continue;
            }
            try {
                int required = Integer.parseInt(parts[1].trim());
                if (level >= required) {
                    best = new Rank(parts[0].trim(), (int) Long.parseLong(parts[2].trim(), 16));
                }
            } catch (NumberFormatException ignored) {
                // malformed config line, keep the previous rank
            }
        }
        return best;
    }

    private record Rank(String name, int color) {
    }
}
