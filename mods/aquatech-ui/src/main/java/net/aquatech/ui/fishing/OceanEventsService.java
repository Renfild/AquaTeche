package net.aquatech.ui.fishing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.aquatech.ui.AquaTechUI;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.world.scores.Scoreboard;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Океанские ивенты: Золотая рыба (случайное 20-минутное окно каждые ~3 часа,
 * шанс 5% на золотой улов с джекпотом), Задания дня (3 детерминированных
 * задания от даты, награда монетами) и Недельный турнир (сб+вс, самая тяжёлая
 * рыба, топ-3 с призами). Всё на честном скорборде "coins" и NBT веса рыбы.
 */
@Mod.EventBusSubscriber(modid = "aquatech_ui")
public final class OceanEventsService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path TOURNAMENT_FILE = FMLPaths.CONFIGDIR.get().resolve("aquatech_tournament.json");
    private static final Path FISH_SHOP_FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/fish_shop.json");
    private static final Path BOOST_FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/event_boost.json");
    private static final String WEIGHT_TAG = "aquatech_tournament_weight";
    private static final int REROLL_COST = 100;
    private static final long GOLDEN_BLOCK = 3L * 3600_000L;   // окно каждые 3 часа
    private static final long GOLDEN_WINDOW = 20L * 60_000L;   // длится 20 минут
    private static final double GOLDEN_CHANCE = 0.05;
    private static final double STORM_GOLDEN_CHANCE = 0.65;
    private static long goldStormUntil;
    private static boolean lastGoldenActive = false;
    private static boolean lastTournamentActive = false;
    private static boolean tournamentLoaded;

    // ─────────────────────────── косяки и всплески цен ───────────────────────────

    private record School(String fishId, String fishName, double x, double z, int radius, long until) {
    }

    private record Boost(String fishId, String fishName, double mult, long until) {
    }

    private static School school;
    private static Boost boost;
    private static long nextEventAt;

    // ─────────────────────────── монеты ───────────────────────────

    private static void pushHubUpdate(ServerPlayer player) {
        if (player == null) return;
        try {
            Class<?> hds = Class.forName("store.aquateche.aqualumen.common.service.HubDataService");
            hds.getMethod("push", ServerPlayer.class).invoke(null, player);
        } catch (Throwable ignored) {
        }
    }

    private static void addCoins(ServerPlayer player, long amount) {
        if (amount <= 0 || player == null) return;
        try {
            Class<?> eco = Class.forName("store.aquateche.aqualumen.common.service.HubEconomy");
            eco.getMethod("grantCoins", ServerPlayer.class, long.class).invoke(null, player, amount);
            return;
        } catch (Throwable ignored) {
        }
        Scoreboard sb = player.getScoreboard();
        Objective obj = sb.getObjective("coins");
        if (obj == null) obj = sb.getObjective("aquacoins");
        if (obj == null) return;
        Score score = sb.getOrCreatePlayerScore(player.getScoreboardName(), obj);
        score.setScore(score.getScore() + (int) amount);
    }

    private static boolean takeCoins(ServerPlayer player, long amount) {
        if (amount <= 0 || player == null) return true;
        try {
            Class<?> eco = Class.forName("store.aquateche.aqualumen.common.service.HubEconomy");
            Object res = eco.getMethod("trySpendCoins", ServerPlayer.class, long.class).invoke(null, player, amount);
            if (res instanceof Boolean b) {
                return b;
            }
        } catch (Throwable ignored) {
        }
        Scoreboard sb = player.getScoreboard();
        Objective obj = sb.getObjective("coins");
        if (obj == null) obj = sb.getObjective("aquacoins");
        if (obj == null) return true;
        Score score = sb.getOrCreatePlayerScore(player.getScoreboardName(), obj);
        if (score.getScore() < amount) return false;
        score.setScore(score.getScore() - (int) amount);
        return true;
    }

    private static void broadcast(MinecraftServer server, String message) {
        if (server == null) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal(message));
        }
    }

    // ─────────────────────────── золотая рыба ───────────────────────────

    /** Начало текущего золотого окна (epoch ms) или -1, если сейчас не окно. */
    private static long goldenWindowStart(long now) {
        long block = now / GOLDEN_BLOCK;
        long seed = block * 8191L + 17L;
        long span = GOLDEN_BLOCK - GOLDEN_WINDOW;
        long offset = Math.floorMod(seed, span);
        long start = block * GOLDEN_BLOCK + offset;
        return (now >= start && now < start + GOLDEN_WINDOW) ? start : -1L;
    }

    private static boolean goldenActive(long now) {
        return now < goldStormUntil || goldenWindowStart(now) >= 0;
    }

    private static boolean goldStormActive(long now) {
        return now < goldStormUntil;
    }

    private static double goldenChance(long now) {
        return goldStormActive(now) ? STORM_GOLDEN_CHANCE : GOLDEN_CHANCE;
    }

    public static int startGoldStorm(MinecraftServer server, int minutes) {
        if (server == null) {
            return 0;
        }
        int mins = Math.max(1, Math.min(60, minutes));
        goldStormUntil = System.currentTimeMillis() + mins * 60_000L;
        lastGoldenActive = true;
        ServerLevel overworld = server.overworld();
        if (overworld != null) {
            overworld.setWeatherParameters(0, mins * 1200, true, true);
        }
        Component title = Component.literal("§6✦ ЗОЛОТАЯ БУРЯ ✦");
        Component sub = Component.literal("§eЛови рыбу — джекпот до 2500 монет");
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(8, 70, 16));
            p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title));
            p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(sub));
            strikeNear(p, 3);
        }
        broadcast(server, "§6[Кот-рыболов] §e§lЗОЛОТАЯ БУРЯ! §r§f" + mins
                + " мин — почти каждый улов может стать золотым. Молнии бьют в воду!");
        return mins;
    }

    public static void stopGoldStorm(MinecraftServer server) {
        goldStormUntil = 0L;
        lastGoldenActive = goldenWindowStart(System.currentTimeMillis()) >= 0;
        if (server != null) {
            broadcast(server, "§6[Кот-рыболов] §7Золотая буря стихла.");
        }
    }

    private static void strikeNear(ServerPlayer player, int count) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        var random = player.getRandom();
        for (int i = 0; i < count; i++) {
            double x = player.getX() + (random.nextDouble() - 0.5) * 14.0;
            double z = player.getZ() + (random.nextDouble() - 0.5) * 14.0;
            double y = player.getY();
            var bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) {
                continue;
            }
            bolt.moveTo(x, y, z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }

    // ─────────────────────────── задания дня ───────────────────────────

    private record Quest(String desc, int goal, long reward, int kind) {
    }
    // kind: 0 — любая рыба, 1 — ночью, 2 — в дождь

    private static final List<Quest> QUEST_POOL = List.of(
            new Quest("Поймайте 12 рыб", 12, 300, 0),
            new Quest("Поймайте 6 рыб ночью", 6, 500, 1),
            new Quest("Поймайте 5 рыб в дождь", 5, 700, 2),
            new Quest("Поймайте 30 рыб", 30, 900, 0),
            new Quest("Поймайте 15 рыб ночью", 15, 1100, 1));
    /** Детерминированные индексы пула на день — общие для всех игроков. */
    private static int[] dailyIndices(int day) {
        int n = QUEST_POOL.size();
        int i1 = ((day * 7 + 3) % n + n) % n;
        int i2 = ((day * 13 + 5) % n + n) % n;
        if (i2 == i1) i2 = (i2 + 1) % n;
        int i3 = ((day * 29 + 11) % n + n) % n;
        if (i3 == i1 || i3 == i2) i3 = (i3 + 1) % n;
        if (i3 == i1) i3 = (i3 + 1) % n;
        return new int[]{i1, i2, i3};
    }

    private static List<Quest> questsFor(ServerPlayer player) {
        JsonObject st = questTag(player);
        List<Quest> out = new ArrayList<>();
        int[] def = dailyIndices(today());
        for (int i = 0; i < 3; i++) {
            int idx = st.has("q" + i) ? st.get("q" + i).getAsInt() : def[i];
            out.add(QUEST_POOL.get(Math.floorMod(idx, QUEST_POOL.size())));
        }
        return out;
    }

    private static int today() {
        return (int) (ZonedDateTime.now().toLocalDate().toEpochDay());
    }

    private static com.google.gson.JsonObject questTag(ServerPlayer player) {
        var root = player.getPersistentData();
        com.google.gson.JsonObject state = new com.google.gson.JsonObject();
        if (root.contains("aqev")) {
            try {
                state = com.google.gson.JsonParser.parseString(root.getString("aqev")).getAsJsonObject();
            } catch (Exception ignored) {
            }
        }
        return state;
    }

    private static void saveQuestTag(ServerPlayer player, com.google.gson.JsonObject state) {
        player.getPersistentData().putString("aqev", state.toString());
    }

    private static void ensureQuestDay(ServerPlayer player) {
        JsonObject st = questTag(player);
        int day = today();
        if (!st.has("day") || st.get("day").getAsInt() != day) {
            JsonObject fresh = new JsonObject();
            fresh.addProperty("day", day);
            int[] idx = dailyIndices(day);
            for (int i = 0; i < 3; i++) {
                fresh.addProperty("q" + i, idx[i]);
                fresh.addProperty("p" + i, 0);
                fresh.addProperty("c" + i, false);
            }
            fresh.addProperty("rr", 0);
            saveQuestTag(player, fresh);
            List<Quest> qs = questsFor(player);
            player.sendSystemMessage(Component.literal("§6[Кот-рыболов] §fЗадания дня: §b" + qs.get(0).desc()
                    + " §7(+" + qs.get(0).reward() + ")§f, §b" + qs.get(1).desc()
                    + " §7(+" + qs.get(1).reward() + ")§f, §b" + qs.get(2).desc()
                    + " §7(+" + qs.get(2).reward() + " монет) §7— прогресс в F4 → События"));
            return;
        }
        // миграция старого формата (d0..d2 -> c0..c2)
        boolean migrated = false;
        for (int i = 0; i < 3; i++) {
            if (!st.has("c" + i) && st.has("d" + i)) {
                st.addProperty("c" + i, st.get("d" + i).getAsBoolean());
                migrated = true;
            }
            if (!st.has("q" + i)) {
                st.addProperty("q" + i, dailyIndices(day)[i]);
                migrated = true;
            }
        }
        if (!st.has("rr")) {
            st.addProperty("rr", 0);
            migrated = true;
        }
        if (migrated) saveQuestTag(player, st);
    }

    // ─────────────────────────── турнир ───────────────────────────

    private static class TournamentState {
        int week = -1;
        List<TopEntry> top = new ArrayList<>();

        static class TopEntry {
            String name;
            String uuid;
            double weight;
            String fish;
        }
    }

    private static TournamentState tournament;

    private static TournamentState tournament() {
        if (tournament == null) {
            tournament = new TournamentState();
            try {
                if (Files.exists(TOURNAMENT_FILE)) {
                    tournament = GSON.fromJson(Files.readString(TOURNAMENT_FILE), TournamentState.class);
                }
            } catch (Exception ignored) {
            }
            if (tournament == null) tournament = new TournamentState();
            tournamentLoaded = true;
        }
        return tournament;
    }

    private static void saveTournament() {
        try {
            Files.writeString(TOURNAMENT_FILE, GSON.toJson(tournament()));
        } catch (Exception ignored) {
        }
    }

    private static int weekNumber() {
        return ZonedDateTime.now().getYear() * 100 + ZonedDateTime.now().getDayOfYear() / 7;
    }

    private static boolean tournamentActive() {
        DayOfWeek d = ZonedDateTime.now().getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    private static void awardTournament(MinecraftServer server) {
        TournamentState t = tournament();
        List<TournamentState.TopEntry> top = t.top;
        if (top == null || top.isEmpty()) {
            broadcast(server, "§6[Турнир] §7Турнир недели завершён — никто не поймал рыбу. Призовой фонд сгорел!");
            return;
        }
        long[] prizes = {2500, 1000, 500};
        String[] places = {"§6🥇", "§7🥈", "§f🥉"};
        broadcast(server, "§6[Турнир] §eИтоги недели — самые тяжёлые уловы:");
        for (int i = 0; i < Math.min(3, top.size()); i++) {
            TournamentState.TopEntry e = top.get(i);
            broadcast(server, "  " + places[i] + " §f" + e.name + " — §b" + e.fish
                    + " §7(" + String.format("%.2f", e.weight) + " кг) §6+" + prizes[i] + " монет");
            ServerPlayer online = server.getPlayerList().getPlayer(java.util.UUID.fromString(e.uuid));
            if (online != null) {
                addCoins(online, prizes[i]);
                online.sendSystemMessage(Component.literal("§6[Турнир] §aПриз " + prizes[i] + " монет зачислен!"));
            }
        }
        t.top = new ArrayList<>();
        t.week = -1;
        saveTournament();
    }

    // ─────────────────────────── обработка улова ───────────────────────────

    public static void onCatch(ServerPlayer player, List<ItemStack> awarded) {
        if (awarded == null || awarded.isEmpty()) return;
        MinecraftServer server = player.server;
        long now = System.currentTimeMillis();
        ensureQuestDay(player);

        // 1. Золотая рыба
        boolean golden = goldenActive(now);
        if (golden && player.getRandom().nextDouble() < goldenChance(now)) {
            ItemStack target = null;
            for (ItemStack st : awarded) {
                if (!st.isEmpty() && st.getItem() != Items.AIR) {
                    target = st;
                    break;
                }
            }
            if (target != null) {
                target.enchant(net.minecraft.world.item.enchantment.Enchantments.FISHING_SPEED, 1);
                target.setHoverName(Component.literal("§6✨ Золотая рыба ✨"));
                long jackpot = 500 + player.getRandom().nextInt(2000);
                addCoins(player, jackpot);
                strikeNear(player, goldStormActive(now) ? 2 : 1);
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(4, 40, 10));
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                        Component.literal("§6✨ ЗОЛОТАЯ РЫБА ✨")));
                player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                        Component.literal("§e+" + jackpot + " монет")));
                broadcast(server, "§6[Кот-рыболов] §e" + player.getGameProfile().getName()
                        + " поймал §6✨ЗОЛОТУЮ рыбу✨ §eи получил " + jackpot + " монет!");
            }
        }

        // 2. Контракты дня
        JsonObject st = questTag(player);
        List<Quest> qs = questsFor(player);
        boolean changed = false;
        for (int i = 0; i < 3 && i < qs.size(); i++) {
            Quest q = qs.get(i);
            String ck = "c" + i, pk = "p" + i;
            if (st.has(ck) && st.get(ck).getAsBoolean()) continue; // награда уже получена
            if (st.has(pk) && st.get(pk).getAsInt() >= q.goal()) continue; // готово к получению
            boolean fits = switch (q.kind()) {
                case 1 -> player.level().isNight();
                case 2 -> player.level().isRaining();
                default -> true;
            };
            if (!fits) continue;
            int progress = (st.has(pk) ? st.get(pk).getAsInt() : 0) + 1;
            st.addProperty(pk, progress);
            changed = true;
            if (progress >= q.goal()) {
                player.sendSystemMessage(Component.literal("§6[Кот-рыболов] §aКонтракт выполнен: "
                        + q.desc() + " §7— забери §6+" + q.reward() + " §7в F4 → События"));
                broadcast(server, "§6[Кот-рыболов] §f" + player.getGameProfile().getName()
                        + " §aвыполнил контракт дня: " + q.desc());
            } else if (progress % 5 == 0) {
                player.sendSystemMessage(Component.literal("§7[Контракт] " + q.desc() + " — §e"
                        + progress + "/" + q.goal()));
            }
        }
        if (changed) saveQuestTag(player, st);

        // 3. Турнир (вес: свой тег; StarCatcher 2.3.19 вес в NBT не пишет — проставляем при выдаче)
        if (tournamentActive()) {
            TournamentState t = tournament();
            if (t.week != weekNumber()) {
                t.week = weekNumber();
                t.top = new ArrayList<>();
                saveTournament();
            }
            double best = -1;
            String fishName = "";
            for (ItemStack stack : awarded) {
                if (stack == null || stack.isEmpty()) continue;
                double w;
                if (stack.hasTag() && stack.getTag().contains(WEIGHT_TAG)) {
                    w = stack.getTag().getDouble(WEIGHT_TAG);
                } else if (stack.hasTag() && stack.getTag().contains("caught_fish_info")
                        && stack.getTag().getCompound("caught_fish_info").contains("weight")) {
                    w = stack.getTag().getCompound("caught_fish_info").getDouble("weight");
                } else {
                    var rnd = player.getRandom();
                    w = Math.round((0.3 + rnd.nextDouble() * rnd.nextDouble() * 24.0) * 100) / 100.0;
                    stack.getOrCreateTag().putDouble(WEIGHT_TAG, w);
                }
                if (w > best) {
                    best = w;
                    fishName = stack.getHoverName().getString();
                }
            }
            if (best > 0) {
                TournamentState.TopEntry mine = null;
                for (TournamentState.TopEntry e : t.top) {
                    if (e.uuid.equals(player.getUUID().toString())) {
                        mine = e;
                        break;
                    }
                }
                if (mine == null) {
                    if (t.top.size() < 3 || best > t.top.get(t.top.size() - 1).weight) {
                        TournamentState.TopEntry e = new TournamentState.TopEntry();
                        e.name = player.getGameProfile().getName();
                        e.uuid = player.getUUID().toString();
                        e.weight = best;
                        e.fish = fishName;
                        t.top.add(e);
                        t.top.sort((a, b2) -> Double.compare(b2.weight, a.weight));
                        while (t.top.size() > 3) t.top.remove(t.top.size() - 1);
                        saveTournament();
                        int place = t.top.indexOf(e);
                        if (place >= 0 && place < 3) {
                            player.sendSystemMessage(Component.literal("§6[Турнир] §aВы на "
                                    + (place + 1) + " месте недели! §7(" + String.format("%.2f", best) + " кг)"));
                        }
                    }
                } else if (best > mine.weight) {
                    mine.weight = best;
                    mine.fish = fishName;
                    t.top.sort((a, b2) -> Double.compare(b2.weight, a.weight));
                    saveTournament();
                }
            }
        }

        // 4. Косяк: бонусная рыба в радиусе события (приват не мешает — заброс удочки не гвардится)
        if (school != null && now < school.until()) {
            double dx = player.getX() - school.x();
            double dz = player.getZ() - school.z();
            if (dx * dx + dz * dz <= (double) school.radius() * school.radius()) {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        new net.minecraft.resources.ResourceLocation(school.fishId()));
                if (item != Items.AIR) {
                    int extra = 1 + player.getRandom().nextInt(2);
                    var bonus = new ItemStack(item, extra);
                    if (!player.getInventory().add(bonus)) {
                        player.drop(bonus, false);
                    }
                    player.sendSystemMessage(Component.literal("§b[Косяк] §f+" + extra + " "
                            + school.fishName() + " §7— улов в косяке"));
                }
            }
        }
    }

    // ─────────────────── hub-интеграция: доступ для вкладки F4 ───────────────────

    /** Снимок контрактов игрока для вкладки «События» в F4. */
    public static List<Map<String, Object>> questView(ServerPlayer player) {
        ensureQuestDay(player);
        JsonObject st = questTag(player);
        List<Quest> qs = questsFor(player);
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < 3 && i < qs.size(); i++) {
            Quest q = qs.get(i);
            Map<String, Object> row = new java.util.HashMap<>();
            int progress = st.has("p" + i) ? st.get("p" + i).getAsInt() : 0;
            boolean claimed = st.has("c" + i) && st.get("c" + i).getAsBoolean();
            row.put("index", i);
            row.put("desc", q.desc());
            row.put("goal", q.goal());
            row.put("progress", Math.min(progress, q.goal()));
            row.put("reward", q.reward());
            row.put("claimed", claimed);
            out.add(row);
        }
        return out;
    }

    /** Выдача награды за выполненный контракт. */
    public static boolean claimQuest(ServerPlayer player, int index) {
        if (index < 0 || index > 2) return false;
        ensureQuestDay(player);
        JsonObject st = questTag(player);
        List<Quest> qs = questsFor(player);
        Quest q = qs.get(index);
        String ck = "c" + index, pk = "p" + index;
        boolean claimed = st.has(ck) && st.get(ck).getAsBoolean();
        int progress = st.has(pk) ? st.get(pk).getAsInt() : 0;
        if (claimed || progress < q.goal()) return false;
        addCoins(player, q.reward());
        st.addProperty(ck, true);
        saveQuestTag(player, st);
        pushHubUpdate(player);
        player.sendSystemMessage(Component.literal("§6[Кот-рыболов] §aНаграда получена: §6+"
                + q.reward() + " монет"));
        return true;
    }

    /** Реролл контракта за 100 монет. */
    public static boolean rerollQuest(ServerPlayer player, int index) {
        if (index < 0 || index > 2) return false;
        ensureQuestDay(player);
        JsonObject st = questTag(player);
        int rr = st.has("rr") ? st.get("rr").getAsInt() : 0;
        if (rr >= 25) {
            player.sendSystemMessage(Component.literal("§6[Кот-рыболов] §cЛимит замен на сегодня исчерпан."));
            return false;
        }
        if (st.has("c" + index) && st.get("c" + index).getAsBoolean()) {
            player.sendSystemMessage(Component.literal("§6[Кот-рыболов] §cНельзя заменить уже выполненное задание."));
            return false;
        }
        if (!takeCoins(player, REROLL_COST)) {
            player.sendSystemMessage(Component.literal("§6[Кот-рыболов] §cНужно " + REROLL_COST + " монет на реролл."));
            return false;
        }
        int[] used = new int[3];
        for (int i = 0; i < 3; i++) used[i] = st.has("q" + i) ? st.get("q" + i).getAsInt() : -1;
        int currentQ = st.has("q" + index) ? st.get("q" + index).getAsInt() : -1;
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < QUEST_POOL.size(); i++) {
            if (i != currentQ && i != used[0] && i != used[1] && i != used[2]) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) {
            for (int i = 0; i < QUEST_POOL.size(); i++) {
                if (i != currentQ) candidates.add(i);
            }
        }
        int pick = candidates.isEmpty() ? (currentQ + 1) % QUEST_POOL.size() : candidates.get(player.getRandom().nextInt(candidates.size()));
        st.addProperty("q" + index, pick);
        st.addProperty("p" + index, 0);
        st.addProperty("rr", rr + 1);
        saveQuestTag(player, st);
        pushHubUpdate(player);
        player.sendSystemMessage(Component.literal("§6[Кот-рыболов] §fНовый контракт: §b"
                + QUEST_POOL.get(pick).desc() + " §7(−" + REROLL_COST + " монет)"));
        return true;
    }

    /** Статус активных событий для баннера F4. */
    public static Map<String, Object> statusView() {
        long now = System.currentTimeMillis();
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("golden", goldenActive(now));
        m.put("storm", goldStormActive(now));
        m.put("tournament", tournamentActive());
        if (school != null && now < school.until()) {
            m.put("schoolFish", school.fishName());
            m.put("schoolUntil", school.until);
        }
        if (boost != null && now < boost.until()) {
            m.put("boostFish", boost.fishName());
            m.put("boostMult", boost.mult);
            m.put("boostUntil", boost.until);
        }
        return m;
    }

    /** Множитель цены вида для скупщика (тренд-всплеск; тренд считает aqualumen). */
    public static double boostMultiplier(String fishId) {
        if (boost != null && System.currentTimeMillis() < boost.until && boost.fishId().equals(fishId)) {
            return boost.mult;
        }
        return 1.0;
    }

    // ─────────────────── косяки и всплески цен: планировщик ───────────────────

    @SuppressWarnings("unchecked")
    private static List<java.util.Map<String, Object>> shopFish() {
        try {
            JsonObject shop = com.google.gson.JsonParser.parseString(
                    Files.readString(FISH_SHOP_FILE)).getAsJsonObject();
            JsonObject fishes = shop.getAsJsonObject("fishes");
            List<java.util.Map<String, Object>> out = new ArrayList<>();
            for (var entry : fishes.entrySet()) {
                JsonObject f = entry.getValue().getAsJsonObject();
                if (f.has("id") && f.has("name")) {
                    out.add(java.util.Map.of(
                            "id", f.get("id").getAsString(),
                            "name", f.get("name").getAsString(),
                            "price", f.has("priceCoins") ? f.get("priceCoins").getAsInt() : 20));
                }
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static void writeBoostFile() {
        try {
            Files.createDirectories(BOOST_FILE.getParent());
            JsonObject j = new JsonObject();
            if (boost != null) {
                j.addProperty("id", boost.fishId());
                j.addProperty("name", boost.fishName());
                j.addProperty("mult", boost.mult());
                j.addProperty("until", boost.until());
            } else {
                j.addProperty("id", "");
                j.addProperty("mult", 1.0);
                j.addProperty("until", 0);
            }
            Files.writeString(BOOST_FILE, GSON.toJson(j));
        } catch (Exception ignored) {
        }
    }

    private static void tickEvents(MinecraftServer server, long now) {
        if (nextEventAt == 0L) {
            nextEventAt = now + 15L * 60_000L; // первое событие через 15 минут после старта
            return;
        }
        if (school != null && now >= school.until()) {
            school = null;
            broadcast(server, "§b[Косяк] §7Косяк ушёл в глубину.");
        }
        if (boost != null && now >= boost.until()) {
            boost = null;
            writeBoostFile();
            broadcast(server, "§6[Рыбак] §7Всплеск цен закончился.");
        }
        if (school == null && boost == null && now >= nextEventAt) {
            var players = server.getPlayerList().getPlayers();
            if (players.isEmpty()) {
                nextEventAt = now + 10L * 60_000L;
                return;
            }
            var fish = shopFish();
            if (fish.isEmpty()) {
                nextEventAt = now + 30L * 60_000L;
                return;
            }
            var pick = fish.get(server.overworld().getRandom().nextInt(fish.size()));
            if (server.overworld().getRandom().nextBoolean()) {
                var anchor = players.get(server.overworld().getRandom().nextInt(players.size()));
                school = new School(String.valueOf(pick.get("id")), String.valueOf(pick.get("name")),
                        anchor.getX(), anchor.getZ(), 48, now + 7L * 60_000L);
                broadcast(server, "§b[Косяк] §eКосяк §f" + pick.get("name")
                        + " §eклубится рядом с §b" + anchor.getGameProfile().getName()
                        + "§e! Улов этой рыбы ×3 §7(7 минут) §7— приват не помеха, ловите!");
            } else {
                double mult = 2.5 + server.overworld().getRandom().nextDouble() * 0.5;
                boost = new Boost(String.valueOf(pick.get("id")), String.valueOf(pick.get("name")),
                        Math.round(mult * 100) / 100.0, now + 10L * 60_000L);
                writeBoostFile();
                broadcast(server, "§6[Рыбак] §eАжиотаж! §fЦены на §b" + pick.get("name")
                        + " §eвыросли до ×" + boost.mult() + " §7(10 минут) — успей продать!");
            }
            nextEventAt = now + (40 + server.overworld().getRandom().nextInt(31)) * 60_000L; // следующее через 40–70 мин
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        int tick = server.getTickCount();
        if (tick % 200 == 0) {
            sweepStaleDrops(server);
        }
        if (tick % 20 != 0) return;
        long now = System.currentTimeMillis();
        tickEvents(server, now);
        if (goldStormActive(now) && tick % 40 == 0) {
            var players = server.getPlayerList().getPlayers();
            if (!players.isEmpty()) {
                strikeNear(players.get(server.overworld().getRandom().nextInt(players.size())), 1);
            }
        }
        if (goldStormUntil > 0L && now >= goldStormUntil) {
            stopGoldStorm(server);
        }

        boolean golden = goldenActive(now);
        if (golden != lastGoldenActive) {
            lastGoldenActive = golden;
            if (golden && !goldStormActive(now)) {
                broadcast(server, "§6[Кот-рыболов] §e§lЗОЛОТАЯ РЫБА! §r§fСледующие 20 минут каждая пойманная рыба может оказаться золотой §7(шанс 5%, джекпот до 2500 монет)§f!");
            }
        }

        boolean tourney = tournamentActive();
        if (tourney != lastTournamentActive) {
            lastTournamentActive = tourney;
            if (tourney) {
                tournament();
                broadcast(server, "§6[Турнир] §eНедельный турнир стартовал! §fСамая тяжёлая рыба субботы и воскресенья приносит §62500§f/§71000§f/§8500 монет. Топ — в Tab!");
            } else if (tournamentLoaded) {
                awardTournament(server);
            }
        }
    }

    /** Roadmap: drop entities older than 3 minutes when TPS < 13. */
    private static void sweepStaleDrops(MinecraftServer server) {
        long[] times = server.tickTimes;
        if (times == null || times.length == 0) {
            return;
        }
        long total = 0L;
        for (long t : times) {
            total += t;
        }
        float mspt = (total / (float) times.length) / 1_000_000.0F;
        float tps = Math.min(20.0F, 1000.0F / Math.max(1.0F, mspt));
        if (tps >= 13.0F) {
            return;
        }
        int removed = 0;
        AABB loaded = new AABB(-3.0E7, -64, -3.0E7, 3.0E7, 512, 3.0E7);
        for (ServerLevel level : server.getAllLevels()) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, loaded, e -> e.getAge() > 3600)) {
                item.discard();
                if (++removed >= 256) {
                    break;
                }
            }
            if (removed >= 256) {
                break;
            }
        }
        if (removed > 0) {
            AquaTechUI.LOGGER.info("[AquaTech] cleared {} ground drops (TPS {})", removed, String.format("%.1f", tps));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ensureQuestDay(player);
        if (tournamentActive()) {
            player.sendSystemMessage(Component.literal("§6[Турнир] §eИдёт недельный турнир! §fЛовите самую тяжёлую рыбу — топ-3 получат призы."));
        }
    }

    private OceanEventsService() {
    }
}
