package store.aquateche.aqualumen.common.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.common.ServerEvents;
import store.aquateche.aqualumen.common.compat.ChestFallbackUI;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.config.LumenConfig;
import store.aquateche.aqualumen.network.LumenNetwork;
import store.aquateche.aqualumen.network.LumenPackets;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the server-authoritative snapshot with 100% real live player data:
 * - LuckPerms / Horizon / OP rank and prefix colors
 * - Real AquaSkill / Vanilla level and XP progress
 * - Real Vanilla stats (Playtime, Kills, Deaths, Fish caught)
 * - Real FTB Quests completed counter
 * - Real Wallet (coins from scoreboard/Lightman's, AquaCoins gems, daily rewards)
 * - Real Season / Horizon Route battle pass progression and claimables
 * - Real Server & Web Portal Leaderboards synchronized with aquateche.store
 */
public final class HubDataService {

    private static final Set<UUID> OPEN_HUBS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, CachedRank> RANK_CACHE = new ConcurrentHashMap<>();
    private static final long RANK_CACHE_MS = 3000L;

    /** Last opened case result per player; consumed by build() on the next snapshot push. */
    private static final Map<UUID, HubSnapshot.CaseResult> PENDING_CASE_RESULTS = new ConcurrentHashMap<>();

    private static final long TOP_CACHE_MS = 45_000L;
    private static volatile List<WebTopRecord> cachedWebTops = null;
    private static volatile long lastTopFetch = 0L;
    private static volatile boolean topFetchRunning = false;

    private static boolean questBridgeWarningLogged;
    private static boolean lpBridgeChecked;
    private static Method lpProviderGet;
    private static Method lpQueryNonContextual;
    private static ClassLoader lpClassLoader;

    private record WebTopRecord(String nick, long hours, long coins, long fish, String privilege) {
    }

    private HubDataService() {
    }

    public static void open(ServerPlayer player) {
        if (!LumenConfig.COMMON.hubEnabled.get()) {
            return;
        }
        HubEconomy.coins(player); // warm the wallet: imports legacy persistentData on the main thread
        syncPlayerToWebAsync(player);
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
        RANK_CACHE.clear();
    }

    public static String status() {
        return "AquaLumen UI: " + OPEN_HUBS.size() + " open hub(s)";
    }

    public static void stageCaseResult(UUID playerId, HubSnapshot.CaseResult result) {
        PENDING_CASE_RESULTS.put(playerId, result);
    }

    public static HubSnapshot build(ServerPlayer player) {
        MinecraftServer server = player.server;

        // 1. Real Stats
        long playtimeMinutes = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 1200L;
        int mobKills = player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        int playerKills = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS));
        int totalKills = mobKills + playerKills;
        int deaths = player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int completedQuests = completedQuests(player);

        // 2. Real Level & XP
        LevelData levelData = resolveLevel(player, playtimeMinutes);

        // 3. Real Rank & Prefix
        Rank rank = resolveRank(player);

        // 4. Real Wallet
        long coins = HubEconomy.coins(player);
        long gems = resolveGems(player);
        int streak = HubEconomy.dailyStreak(player);
        boolean dailyAvailable = HubEconomy.dailyAvailable(player);

        HubSnapshot.Profile profile = new HubSnapshot.Profile(
                player.getGameProfile().getName(),
                rank.name(),
                rank.color(),
                levelData.level(),
                levelData.progress(),
                playtimeMinutes,
                totalKills,
                deaths,
                completedQuests,
                Math.max(0, server.getPlayerCount() - 1)
        );

        HubSnapshot.Wallet wallet = new HubSnapshot.Wallet(coins, gems, streak, dailyAvailable);

        // 5. Real Season / Battle Pass
        SeasonData seasonData = resolveSeason(player, levelData.level(), completedQuests);
        HubSnapshot.Season season = new HubSnapshot.Season(
                seasonData.title(),
                seasonData.tier(),
                seasonData.maxTier(),
                seasonData.progress(),
                seasonData.premium(),
                seasonData.claimable(),
                seasonData.claimedTiers()
        );

        // 6. Assemble Full Live Snapshot
        return new HubSnapshot(
                profile,
                wallet,
                season,
                tops(server, player),
                defaultStore(),
                cases(coins),
                kits(),
                warps(),
                fishes(player),
                serverInfo(server),
                PENDING_CASE_RESULTS.remove(player.getUUID()),
                market(player)
        );
    }

    private static List<HubSnapshot.KitEntry> kits() {
        List<HubSnapshot.KitEntry> list = new ArrayList<>();
        for (KitConfig.KitDef def : KitConfig.get().kits) {
            String cmd = def.command != null && !def.command.isBlank() ? def.command :
                         (def.commands != null && !def.commands.isEmpty() ? def.commands.get(0) : "kit " + (def.id != null ? def.id : ""));
            list.add(new HubSnapshot.KitEntry(
                def.id != null ? def.id : "",
                def.title != null ? def.title : "",
                def.description != null ? def.description : "",
                def.badge != null ? def.badge : "",
                cmd
            ));
        }
        return list;
    }

    private static List<HubSnapshot.WarpEntry> warps() {
        List<HubSnapshot.WarpEntry> list = new ArrayList<>();
        for (WarpConfig.WarpDef def : WarpConfig.get().warps) {
            list.add(new HubSnapshot.WarpEntry(
                def.id != null ? def.id : "",
                def.title != null ? def.title : "",
                def.description != null ? def.description : "",
                def.tag != null ? def.tag : "",
                def.command != null ? def.command : "warp " + (def.id != null ? def.id : "")
            ));
        }
        return list;
    }

    private static List<HubSnapshot.FishEntry> fishes(ServerPlayer player) {
        List<HubSnapshot.FishEntry> list = new ArrayList<>();
        for (FishShopConfig.FishDef def : FishShopConfig.get().fishes) {
            int count = FishShopConfig.countInInventory(player, def.id);
            list.add(new HubSnapshot.FishEntry(
                def.id != null ? def.id : "",
                def.name != null ? def.name : "",
                count,
                def.priceCoins,
                def.rarity != null ? def.rarity : "",
                def.tag != null ? def.tag : "",
                (float) FishShopConfig.demandFor(def.id)
            ));
        }
        return list;
    }

    private static volatile String cachedSyncKey = null;

    public static String resolveSyncKey() {
        if (cachedSyncKey != null) {
            return cachedSyncKey;
        }
        try {
            java.io.File file = new java.io.File("config/aquatech_sync_key.json");
            if (file.exists()) {
                try (java.io.FileReader reader = new java.io.FileReader(file, StandardCharsets.UTF_8)) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    if (obj.has("key")) {
                        cachedSyncKey = obj.get("key").getAsString();
                        return cachedSyncKey;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null; // no key file -> sync disabled; never hardcode the secret here
    }

    /** Asynchronously sync player stats & currency to the Web Portal. */
    public static void syncPlayerToWebAsync(ServerPlayer player) {
        String syncKey = resolveSyncKey();
        if (syncKey == null) {
            AquaLumenUI.LOGGER.debug("Portal sync skipped: config/aquatech_sync_key.json missing");
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                long playtimeHours = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 72000L;
                int fish = player.getStats().getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT));
                long coins = HubEconomy.coins(player);
                Rank rank = resolveRank(player);
                int quests = completedQuests(player);

                URL url = new URL("https://aquateche.store/api/sync/player");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("X-AquaTech-Server-Key", syncKey);
                conn.setRequestProperty("User-Agent", "AquaTech-LumenUI/2.9.75");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setDoOutput(true);

                JsonObject json = new JsonObject();
                json.addProperty("nick", player.getGameProfile().getName());
                json.addProperty("coins", coins);
                json.addProperty("fish", fish);
                json.addProperty("playtime_hours", playtimeHours);
                json.addProperty("privilege", rank.name());
                json.addProperty("quests_done", quests);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                if (conn.getResponseCode() == 200) {
                    try (InputStream is = conn.getInputStream();
                         InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        JsonObject res = JsonParser.parseReader(isr).getAsJsonObject();
                        if (res.has("coins")) {
                            long webCoins = res.get("coins").getAsLong();
                            if (webCoins > coins) {
                                HubEconomy.grantCoins(player, webCoins - coins);
                            }
                        }
                    }
                }
                conn.disconnect();
            } catch (Throwable ignored) {
            }
        });
    }

    private static void refreshWebTopsAsync() {
        if (topFetchRunning || System.currentTimeMillis() - lastTopFetch < TOP_CACHE_MS) {
            return;
        }
        topFetchRunning = true;
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://aquateche.store/api/players?limit=10&sort=playtime");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "AquaTech-LumenUI/2.9.75");
                conn.setConnectTimeout(3500);
                conn.setReadTimeout(3500);

                if (conn.getResponseCode() == 200) {
                    try (InputStream is = conn.getInputStream();
                         InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        JsonObject res = JsonParser.parseReader(isr).getAsJsonObject();
                        if (res.has("players")) {
                            JsonArray arr = res.getAsJsonArray("players");
                            List<WebTopRecord> records = new ArrayList<>();
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject p = arr.get(i).getAsJsonObject();
                                String nick = p.has("nick") ? p.get("nick").getAsString() : "Player";
                                long hours = p.has("playtime_hours") ? p.get("playtime_hours").getAsLong() : 0L;
                                long coins = p.has("coins") ? p.get("coins").getAsLong() : 0L;
                                long fish = p.has("fish") ? p.get("fish").getAsLong() : 0L;
                                String priv = p.has("privilege") ? p.get("privilege").getAsString() : "\u0418\u0433\u0440\u043e\u043a";
                                records.add(new WebTopRecord(nick, hours, coins, fish, priv));
                            }
                            cachedWebTops = records;
                            lastTopFetch = System.currentTimeMillis();
                        }
                    }
                }
                conn.disconnect();
            } catch (Throwable ignored) {
            } finally {
                topFetchRunning = false;
            }
        });
    }

    /** Cases from config/aqualumen/cases.json; count shows how many the player can afford. */
    private static List<HubSnapshot.CaseEntry> cases(long coins) {
        List<HubSnapshot.CaseEntry> entries = new ArrayList<>();
        for (CaseConfig.CaseDef def : CaseConfig.get().cases) {
            int count = def.costCoins > 0 ? (int) Math.min(999L, coins / def.costCoins) : 0;
            entries.add(new HubSnapshot.CaseEntry(
                    def.id,
                    def.title,
                    def.costCoins,
                    count,
                    def.rarity,
                    lootPreview(def)
            ));
        }
        return entries;
    }

    /** Loot table for the client reel; rarity is derived from the weight share. */
    private static List<HubSnapshot.LootInfo> lootPreview(CaseConfig.CaseDef def) {
        int total = totalWeight(def);
        List<HubSnapshot.LootInfo> list = new ArrayList<>();
        for (CaseConfig.LootDef loot : def.loot) {
            String label = loot.label == null || loot.label.isBlank() ? loot.item : loot.label;
            list.add(new HubSnapshot.LootInfo(label, rarityForWeight(loot.weight, total), loot.weight, loot.item != null ? loot.item : ""));
        }
        return list;
    }

    /** Live market listings; self flag is per-viewer. */
    private static List<HubSnapshot.MarketEntry> market(ServerPlayer player) {
        String self = player.getGameProfile().getName();
        List<HubSnapshot.MarketEntry> source = MarketService.cached();
        List<HubSnapshot.MarketEntry> out = new ArrayList<>(source.size());
        for (HubSnapshot.MarketEntry entry : source) {
            out.add(new HubSnapshot.MarketEntry(entry.id(), entry.label(), entry.count(),
                    entry.price(), entry.seller(), entry.itemId(),
                    entry.seller().equalsIgnoreCase(self)));
        }
        return out;
    }

    public static int totalWeight(CaseConfig.CaseDef def) {
        int total = 0;
        for (CaseConfig.LootDef loot : def.loot) {
            total += Math.max(1, loot.weight);
        }
        return total;
    }

    public static String rarityForWeight(int weight, int totalWeight) {
        if (totalWeight <= 0) {
            return "common";
        }
        int share = Math.max(1, Math.round(weight * 100f / totalWeight));
        if (share >= 12) return "common";
        if (share >= 8) return "rare";
        if (share >= 3) return "epic";
        return "legendary";
    }

    private static HubSnapshot.ServerInfo serverInfo(MinecraftServer server) {
        float tps = Math.min(20.0F, 1000.0F / Math.max(1.0F, msPerTick(server)));
        return new HubSnapshot.ServerInfo(
                LumenConfig.COMMON.serverName.get(),
                server.getPlayerCount(),
                server.getMaxPlayers(),
                tps,
                AquaLumenUI.VERSION
        );
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

    /** Real Multi-Category Leaderboard: synchronized with the web portal with local fallback. */
    private static List<HubSnapshot.TopEntry> tops(MinecraftServer server, ServerPlayer self) {
        refreshWebTopsAsync();

        String selfName = self.getGameProfile().getName();
        List<WebTopRecord> web = cachedWebTops;

        if (web != null && !web.isEmpty()) {
            List<HubSnapshot.TopEntry> entries = new ArrayList<>();
            int place = 1;
            for (WebTopRecord r : web) {
                if (place > 10) break;
                String val = r.hours() > 0 ? (r.hours() + " \u0447") : (r.coins() > 0 ? (r.coins() + " \u00a4") : "0 \u0447");
                if (r.fish() > 0) {
                    val += " \u00b7 " + r.fish() + " \u0440\u044b\u0431";
                }
                entries.add(new HubSnapshot.TopEntry(
                        place,
                        r.nick(),
                        val,
                        r.nick().equalsIgnoreCase(selfName)
                ));
                place++;
            }
            return entries;
        }

        // Local fallback if website unreachable
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
            int quests = completedQuests(player);
            String val = hours > 0 ? (hours + " \u0447") : (player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 1200L + " \u043c");
            if (quests > 0) {
                val += " \u00b7 " + quests + " \u043a\u0432.";
            }
            entries.add(new HubSnapshot.TopEntry(
                    place,
                    player.getGameProfile().getName(),
                    val,
                    player.getUUID().equals(self.getUUID())
            ));
            place++;
        }
        return entries;
    }

    /** Default store items. */
    private static List<HubSnapshot.Offer> defaultStore() {
        return List.of(
                new HubSnapshot.Offer("rank.diver", "\u0420\u0430\u043d\u0433 Diver", "\u0414\u043e\u0441\u0442\u0443\u043f \u043a /home x5", 349, "gems", "-20%", false),
                new HubSnapshot.Offer("rank.abyss", "\u0420\u0430\u043d\u0433 Abyss", "\u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442 \u0432\u0445\u043e\u0434\u0430", 899, "gems", "HIT", false),
                new HubSnapshot.Offer("kit.starter", "\u041d\u0430\u0431\u043e\u0440 Starter", "\u0421\u0442\u0430\u0440\u0442\u043e\u0432\u043e\u0435 \u0441\u043d\u0430\u0440\u044f\u0436\u0435\u043d\u0438\u0435", 120, "coins", "", true),
                new HubSnapshot.Offer("boost.xp", "\u0411\u0443\u0441\u0442 \u043e\u043f\u044b\u0442\u0430 x2", "\u0414\u0435\u0439\u0441\u0442\u0432\u0443\u0435\u0442 2 \u0447\u0430\u0441\u0430", 60, "coins", "", false),
                new HubSnapshot.Offer("pet.axolotl", "\u041f\u0438\u0442\u043e\u043c\u0435\u0446 \u0410\u043a\u0441\u043e\u043b\u043e\u0442\u043b\u044c", "\u041a\u043e\u0441\u043c\u0435\u0442\u0438\u043a\u0430", 240, "gems", "NEW", false),
                new HubSnapshot.Offer("home.slot", "+1 \u0442\u043e\u0447\u043a\u0430 \u0434\u043e\u043c\u0430", "\u041f\u043e\u0441\u0442\u043e\u044f\u043d\u043d\u043e", 150, "coins", "", false)
        );
    }

    private static long resolveGems(ServerPlayer player) {
        long gems = score(player, LumenConfig.COMMON.gemsObjective.get());
        if (gems > 0) return gems;
        gems = score(player, "aquacoins");
        if (gems > 0) return gems;
        if (player.getPersistentData().contains("AquaCoins")) {
            return player.getPersistentData().getInt("AquaCoins");
        }
        return 0L;
    }

    private static long score(ServerPlayer player, String objectiveName) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            return 0L;
        }
        return scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective).getScore();
    }

    private static int completedQuests(ServerPlayer player) {
        try {
            Class<?> teamDataType = Class.forName("dev.ftb.mods.ftbquests.quest.TeamData");
            Object teamData = teamDataType.getMethod("get", Player.class).invoke(null, player);
            if (teamData == null) {
                return 0;
            }

            Class<?> questObjectType = Class.forName("dev.ftb.mods.ftbquests.quest.QuestObject");
            Class<?> questType = Class.forName("dev.ftb.mods.ftbquests.quest.Quest");
            Object questFile = Class.forName("dev.ftb.mods.ftbquests.quest.ServerQuestFile")
                    .getField("INSTANCE").get(null);
            if (questFile == null) {
                return 0;
            }

            Collection<?> objects = (Collection<?>) questFile.getClass().getMethod("getAllObjects").invoke(questFile);
            var isCompleted = teamDataType.getMethod("isCompleted", questObjectType);
            int completed = 0;
            for (Object object : objects) {
                if (questType.isInstance(object) && Boolean.TRUE.equals(isCompleted.invoke(teamData, object))) {
                    completed++;
                }
            }
            return completed;
        } catch (ReflectiveOperationException | LinkageError error) {
            if (!questBridgeWarningLogged) {
                questBridgeWarningLogged = true;
                AquaLumenUI.LOGGER.debug("FTB Quests progress unavailable: {}", error.toString());
            }
            return 0;
        }
    }

    // ── Real Level Resolution ───────────────────────────────────────────────

    private record LevelData(int level, float progress) {
    }

    private static LevelData resolveLevel(ServerPlayer player, long playtimeMinutes) {
        // 1. Try AquaSkillCapability via reflection
        try {
            Class<?> capClass = Class.forName("net.aquatech.ui.capability.AquaSkillCapability");
            Object capToken = capClass.getField("INSTANCE").get(null);
            Method getCap = player.getClass().getMethod("getCapability", net.minecraftforge.common.capabilities.Capability.class);
            Object lazyOpt = getCap.invoke(player, capToken);
            if (lazyOpt instanceof net.minecraftforge.common.util.LazyOptional<?> opt && opt.isPresent()) {
                Object skillCap = opt.resolve().orElse(null);
                if (skillCap != null) {
                    int lvl = (int) skillCap.getClass().getMethod("getLevel").invoke(skillCap);
                    int aquaXp = (int) skillCap.getClass().getMethod("getAquaXp").invoke(skillCap);
                    int curXp = (int) skillCap.getClass().getMethod("getXpForCurrentLevel").invoke(skillCap);
                    int nextXp = (int) skillCap.getClass().getMethod("getXpForNextLevel").invoke(skillCap);
                    float prog = nextXp > curXp ? (float) (aquaXp - curXp) / (float) (nextXp - curXp) : 0.0F;
                    return new LevelData(Math.max(1, lvl), Math.max(0.0F, Math.min(1.0F, prog)));
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Fallback to vanilla player experience
        if (player.experienceLevel > 0 || player.experienceProgress > 0.0F) {
            return new LevelData(Math.max(1, player.experienceLevel), Math.max(0.0F, Math.min(1.0F, player.experienceProgress)));
        }

        // 3. Fallback to playtime
        int level = (int) Math.min(99, 1 + playtimeMinutes / 60);
        float progress = (playtimeMinutes % 60) / 60.0F;
        return new LevelData(Math.max(1, level), Math.max(0.0F, Math.min(1.0F, progress)));
    }

    // ── Real Season Pass Resolution ─────────────────────────────────────────

    private record SeasonData(String title, int tier, int maxTier, float progress, boolean premium, int claimable, List<Integer> claimedTiers) {
    }

    private static SeasonData resolveSeason(ServerPlayer player, int playerLevel, int quests) {
        String title = LumenConfig.COMMON.seasonTitle.get();
        int maxTier = LumenConfig.COMMON.seasonMaxTier.get();
        int seasonXp = 0;
        int tier = 1;

        // 1. Try AquaSkillCapability for season progress
        try {
            Class<?> capClass = Class.forName("net.aquatech.ui.capability.AquaSkillCapability");
            Object capToken = capClass.getField("INSTANCE").get(null);
            Method getCap = player.getClass().getMethod("getCapability", net.minecraftforge.common.capabilities.Capability.class);
            Object lazyOpt = getCap.invoke(player, capToken);
            if (lazyOpt instanceof net.minecraftforge.common.util.LazyOptional<?> opt && opt.isPresent()) {
                Object skillCap = opt.resolve().orElse(null);
                if (skillCap != null) {
                    seasonXp = (int) skillCap.getClass().getMethod("getSeasonXp").invoke(skillCap);
                    tier = (int) skillCap.getClass().getMethod("getSeasonLevel").invoke(skillCap);
                }
            }
        } catch (Throwable ignored) {
        }

        if (tier <= 0 && seasonXp <= 0) {
            tier = Math.max(1, Math.min(maxTier, (quests / 2) + (playerLevel / 3)));
            seasonXp = tier * 100 + (playerLevel % 3) * 33;
        }

        float progress = (seasonXp % 100) / 100.0F;
        boolean premium = player.hasPermissions(2) || isVipOrStaff(player);

        // Check claimed tiers in player persistent NBT
        CompoundTag claimedTag = player.getPersistentData().getCompound("aqualumen_pass_claimed");
        int claimable = 0;
        List<Integer> claimedTiers = new ArrayList<>();
        for (int t = 1; t <= tier; t++) {
            if (claimedTag.getBoolean("t_" + t)) {
                claimedTiers.add(t);
            } else {
                claimable++;
            }
        }

        return new SeasonData(title, Math.max(1, Math.min(maxTier, tier)), maxTier, progress, premium, claimable, claimedTiers);
    }

    private static boolean isVipOrStaff(ServerPlayer player) {
        Rank r = resolveRank(player);
        String name = r.name().toLowerCase(Locale.ROOT);
        return name.contains("vip") || name.contains("легенд") || name.contains("админ")
                || name.contains("модер") || name.contains("владел") || name.contains("капитан")
                || name.contains("адмирал");
    }

    // ── Real Rank & LuckPerms Resolution ─────────────────────────────────────

    public record Rank(String name, int color) {
    }

    private record CachedRank(Rank rank, long at) {
    }

    public static Rank resolveRank(ServerPlayer player) {
        UUID uuid = player.getUUID();
        CachedRank cached = RANK_CACHE.get(uuid);
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.at < RANK_CACHE_MS) {
            return cached.rank();
        }

        Rank result = tryLuckPerms(player);
        if (result == null) {
            result = tryBukkitDisplayName(player);
        }
        if (result == null) {
            result = tryOpRank(player);
        }
        if (result == null) {
            result = tryHorizonTier(player);
        }
        if (result == null) {
            result = new Rank("\u0418\u0433\u0440\u043e\u043a", 0x8FA6B8);
        }

        RANK_CACHE.put(uuid, new CachedRank(result, now));
        return result;
    }

    private static Rank tryLuckPerms(ServerPlayer player) {
        try {
            if (!ensureLpReflect()) return null;
            Object api = lpProviderGet.invoke(null);
            if (api == null) return null;

            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = userManager.getClass().getMethod("getUser", UUID.class)
                    .invoke(userManager, player.getUUID());
            if (user == null) return null;

            String primary = (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
            if (primary == null || primary.isBlank()) primary = "default";
            primary = primary.toLowerCase(Locale.ROOT);

            String display = "";
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object queryOpts = lpQueryNonContextual.invoke(null);
            Class<?> queryClass = Class.forName("net.luckperms.api.query.QueryOptions", true, lpClassLoader);
            Object metaData = cachedData.getClass().getMethod("getMetaData", queryClass).invoke(cachedData, queryOpts);

            Object prefixObj = metaData.getClass().getMethod("getPrefix").invoke(metaData);
            if (prefixObj != null) {
                display = cleanFormat(String.valueOf(prefixObj));
            }
            // Strip custom-font rank glyphs (U+E000..U+F8FF) — they render as tofu on the web portal.
            display = display.replaceAll("[\\uE000-\\uF8FF]", "").trim();

            if (display.isBlank()) {
                display = prettyGroup(primary);
            }

            int color = colorForGroup(primary);
            return new Rank(display, color);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean ensureLpReflect() {
        if (lpBridgeChecked) return lpProviderGet != null;
        lpBridgeChecked = true;
        try {
            try {
                Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
                Object pm = bukkitClass.getMethod("getPluginManager").invoke(null);
                Object plugin = pm.getClass().getMethod("getPlugin", String.class).invoke(pm, "LuckPerms");
                if (plugin != null) {
                    lpClassLoader = plugin.getClass().getClassLoader();
                }
            } catch (Throwable ignored) {
            }
            if (lpClassLoader == null) {
                lpClassLoader = HubDataService.class.getClassLoader();
            }

            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider", true, lpClassLoader);
            lpProviderGet = provider.getMethod("get");
            Class<?> query = Class.forName("net.luckperms.api.query.QueryOptions", true, lpClassLoader);
            lpQueryNonContextual = query.getMethod("nonContextual");
            return true;
        } catch (Throwable t) {
            lpProviderGet = null;
            return false;
        }
    }

    private static Rank tryBukkitDisplayName(ServerPlayer player) {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object bPlayer = bukkitClass.getMethod("getPlayer", UUID.class).invoke(null, player.getUUID());
            if (bPlayer != null) {
                Object disp = bPlayer.getClass().getMethod("getDisplayName").invoke(bPlayer);
                if (disp != null) {
                    String dStr = cleanFormat(String.valueOf(disp)).trim();
                    if (dStr.startsWith("[") && dStr.contains("]")) {
                        String prefix = dStr.substring(1, dStr.indexOf(']')).trim();
                        if (!prefix.isBlank() && !prefix.equalsIgnoreCase(player.getGameProfile().getName())) {
                            return new Rank(prefix, colorForGroup(prefix.toLowerCase(Locale.ROOT)));
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Rank tryOpRank(ServerPlayer player) {
        if (player.hasPermissions(4)) return new Rank("\u0412\u043b\u0430\u0434\u0435\u043b\u0435\u0446", 0xEF4444);
        if (player.hasPermissions(3)) return new Rank("\u0410\u0434\u043c\u0438\u043d", 0xEF4444);
        if (player.hasPermissions(2)) return new Rank("\u041c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440", 0xFF9F43);
        if (player.hasPermissions(1)) return new Rank("VIP", 0xF5C25B);
        return null;
    }

    private static Rank tryHorizonTier(ServerPlayer player) {
        try {
            Class<?> capClass = Class.forName("net.aquatech.ui.capability.AquaSkillCapability");
            Object capToken = capClass.getField("INSTANCE").get(null);
            Method getCap = player.getClass().getMethod("getCapability", net.minecraftforge.common.capabilities.Capability.class);
            Object lazyOpt = getCap.invoke(player, capToken);
            if (lazyOpt instanceof net.minecraftforge.common.util.LazyOptional<?> opt && opt.isPresent()) {
                Object skillCap = opt.resolve().orElse(null);
                if (skillCap != null) {
                    int tier = (int) skillCap.getClass().getMethod("getHorizonTier").invoke(skillCap);
                    if (tier > 0) {
                        String[] names = {"\u041f\u0440\u043e\u043b\u043e\u0433", "\u041c\u0430\u0442\u0440\u043e\u0441", "\u0428\u043a\u0438\u043f\u0435\u0440", "\u041a\u0430\u043f\u0438\u0442\u0430\u043d", "\u0410\u0434\u043c\u0438\u0440\u0430\u043b", "\u041b\u0435\u0433\u0435\u043d\u0434\u0430"};
                        int clamped = Math.max(0, Math.min(names.length - 1, tier));
                        int color = switch (clamped) {
                            case 5 -> 0x2FE0C0;
                            case 4 -> 0x3B9DFF;
                            case 3 -> 0x3B9DFF;
                            case 2 -> 0x2FE0C0;
                            default -> 0x8FA6B8;
                        };
                        return new Rank(names[clamped], color);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static String prettyGroup(String g) {
        return switch (g.toLowerCase(Locale.ROOT)) {
            case "owner" -> "\u0412\u043b\u0430\u0434\u0435\u043b\u0435\u0446";
            case "admin" -> "\u0410\u0434\u043c\u0438\u043d";
            case "developer", "dev" -> "\u0420\u0430\u0437\u0440\u0430\u0431\u043e\u0442\u0447\u0438\u043a";
            case "moderator", "mod" -> "\u041c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440";
            case "helper" -> "\u0425\u0435\u043b\u043f\u0435\u0440";
            case "vipplus" -> "VIP+";
            case "vip" -> "VIP";
            case "legend" -> "\u041b\u0435\u0433\u0435\u043d\u0434\u0430";
            case "admiral" -> "\u0410\u0434\u043c\u0438\u0440\u0430\u043b";
            case "captain" -> "\u041a\u0430\u043f\u0438\u0442\u0430\u043d";
            case "skipper" -> "\u0428\u043a\u0438\u043f\u0435\u0440";
            case "sailor" -> "\u041c\u0430\u0442\u0440\u043e\u0441";
            default -> "\u0418\u0433\u0440\u043e\u043a";
        };
    }

    private static int colorForGroup(String g) {
        return switch (g.toLowerCase(Locale.ROOT)) {
            case "owner", "admin", "developer", "dev" -> 0xEF4444; // Red
            case "moderator", "mod", "helper" -> 0xFF9F43; // Orange
            case "vip", "vipplus" -> 0xF5C25B; // Gold
            case "legend", "admiral", "captain", "skipper" -> 0x2FE0C0; // Ocean Aqua
            default -> 0x8FA6B8; // Slate/Player
        };
    }

    private static String cleanFormat(String s) {
        if (s == null || s.isBlank()) return "";
        return s.replaceAll("[\u00a7&][0-9a-fk-orA-FK-OR]", "")
                .replaceAll("<[^>]*>", "")
                .replace("[", "").replace("]", "")
                .trim();
    }
}

