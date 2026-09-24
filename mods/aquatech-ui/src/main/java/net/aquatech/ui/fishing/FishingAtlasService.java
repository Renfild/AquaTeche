package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.capability.OceanProgressCapability;
import net.aquatech.ui.horizon.StormEvent;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.S2CSyncOceanProgressPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Рыбный атлас: виды, рекорды веса, собранные грейды и условия поимки.
 * Живёт в persistentData игрока ("aquatech_atlas"), F4-хаб aqualumen читает тот же тег.
 *
 * Формат: { total:int, s:{ "<itemId>": { c:int (поимок), w:double (лучший вес, кг),
 *          g:int (маска грейдов 1=серебро 2=золото 4=радужная), m:int (маска условий),
 *          d:long (эпохальный день первой поимки) } } }
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class FishingAtlasService {

    public static final String KEY = "aquatech_atlas";

    public static final int COND_DAY = 1;
    public static final int COND_NIGHT = 2;
    public static final int COND_RAIN = 4;
    public static final int COND_STORM = 8;
    public static final int COND_GOLDEN = 16;

    private FishingAtlasService() {
    }

    /** Грейды + запись в атлас для ручной ловли. Дропы — настоящие стеки, не копии. */
    public static void onManualCatch(ServerPlayer player, List<ItemStack> drops) {
        if (player == null || drops == null || drops.isEmpty()) return;
        try {
            CompoundTag root = player.getPersistentData();
            CompoundTag atlas = root.contains(KEY, Tag.TAG_COMPOUND) ? root.getCompound(KEY) : new CompoundTag();
            CompoundTag species = atlas.contains("s", Tag.TAG_COMPOUND) ? atlas.getCompound("s") : new CompoundTag();

            int cond = conditions(player);
            int catches = atlas.getInt("total");
            long today = LocalDate.now().toEpochDay();
            Map<String, Integer> gradeBySpecies = new HashMap<>();
            java.util.Set<String> seenSpecies = new java.util.HashSet<>();

            for (ItemStack stack : drops) {
                if (!FishingLootHandler.isStarCatcherFishItem(stack)) continue;
                var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (key == null) continue;
                String id = key.toString();

                CompoundTag rec = species.contains(id, Tag.TAG_COMPOUND)
                        ? species.getCompound(id) : new CompoundTag();
                if (seenSpecies.add(id)) {
                    // Один бросок веса и грейда на вид за улов: грейд идёт на весь стак,
                    // рейт-моды не размножают грейды.
                    boolean firstEver = rec.getInt("c") == 0;
                    if (rec.getLong("d") <= 0L) {
                        rec.putLong("d", today);
                    }
                    rec.putInt("c", rec.getInt("c") + 1);
                    String displayName = stack.getHoverName().getString();
                    double weight = rollWeight(player);
                    if (weight > rec.getDouble("w")) {
                        rec.putDouble("w", weight);
                    }
                    updateRecord(player, id, displayName, weight);
                    if (firstEver) {
                        grantFirstCatch(player, displayName);
                    }
                    int grade = rollGrade(player);
                    if (grade > FishGrade.NONE) {
                        rec.putInt("g", rec.getInt("g") | FishGrade.maskBit(grade));
                    }
                    gradeBySpecies.put(id, grade);
                    catches++;
                }
                rec.putInt("m", rec.getInt("m") | cond);
                FishGrade.set(stack, gradeBySpecies.getOrDefault(id, FishGrade.NONE));
                species.put(id, rec);
            }

            if (!seenSpecies.isEmpty()) {
                atlas.put("s", species);
                atlas.putInt("total", catches);
                claimMilestones(player, atlas, species.getAllKeys().size());
                root.put(KEY, atlas);
            }
        } catch (Throwable ignored) {
        }
    }

    /** Вехи коллекции: пороги по числу видов, награда монетами, маска в "ms". */
    private static final int[] MILESTONES = {5, 10, 20, 30, 50, 75};
    private static final long[] MILESTONE_REWARDS = {3000, 8000, 20000, 40000, 80000, 150000};
    private static final long ALL_SPECIES_REWARD = 300000;

    private static void claimMilestones(ServerPlayer player, CompoundTag atlas, int found) {
        long mask = atlas.getLong("ms");
        for (int i = 0; i < MILESTONES.length; i++) {
            if ((mask & (1L << i)) != 0L || found < MILESTONES[i]) continue;
            mask |= 1L << i;
            OceanEventsService.grantCoins(player, MILESTONE_REWARDS[i]);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§b[Атлас] §aОткрыто видов: §f" + found + " §7— веха взята: §6+"
                            + MILESTONE_REWARDS[i] + " монет"));
        }
        int total = OceanEventsService.shopSpeciesCount();
        int allBit = MILESTONES.length;
        if (total > 0 && found >= total && (mask & (1L << allBit)) == 0L) {
            mask |= 1L << allBit;
            OceanEventsService.grantCoins(player, ALL_SPECIES_REWARD);
            OceanEventsService.announce(player.getServer(), "§b[Атлас] §e" + player.getGameProfile().getName()
                    + " §fзакрыл Рыбный атлас полностью: §b" + total + " видов§f! §6+"
                    + ALL_SPECIES_REWARD + " монет");
        }
        atlas.putLong("ms", mask);

        int next = 0;
        long nextReward = 0L;
        for (int i = 0; i < MILESTONES.length; i++) {
            if (found < MILESTONES[i]) {
                next = MILESTONES[i];
                nextReward = MILESTONE_REWARDS[i];
                break;
            }
        }
        if (next == 0 && total > found) {
            next = total;
            nextReward = ALL_SPECIES_REWARD;
        }
        atlas.putInt("nm", next);
        atlas.putLong("nr", nextReward);
    }

    // ─────────────────── серверные рекорды видов + опыт за первый вид ───────────────────

    private static final Path RECORDS_FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/atlas_records.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static volatile JsonObject recordsCache;
    private static volatile long recordsMtime = -1L;

    /** Рекорды видов: { "<itemId>": { holder, weight } }. Читает F4-хаб aqualumen. */
    private static JsonObject records() {
        try {
            if (!Files.exists(RECORDS_FILE)) {
                return recordsCache != null ? recordsCache : new JsonObject();
            }
            long mtime = Files.getLastModifiedTime(RECORDS_FILE).toMillis();
            if (recordsCache != null && mtime == recordsMtime) {
                return recordsCache;
            }
            recordsCache = JsonParser.parseString(Files.readString(RECORDS_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            recordsMtime = mtime;
            return recordsCache;
        } catch (Exception e) {
            return recordsCache != null ? recordsCache : new JsonObject();
        }
    }

    private static void updateRecord(ServerPlayer player, String id, String displayName, double weight) {
        try {
            JsonObject recs = records();
            JsonObject prev = recs.has(id) && recs.get(id).isJsonObject() ? recs.getAsJsonObject(id) : null;
            double prevWeight = prev != null && prev.has("weight") ? prev.get("weight").getAsDouble() : 0.0;
            if (weight <= prevWeight) return;
            boolean beatExisting = prevWeight > 0.0;
            JsonObject row = new JsonObject();
            row.addProperty("holder", player.getGameProfile().getName());
            row.addProperty("weight", weight);
            recs.add(id, row);
            Files.createDirectories(RECORDS_FILE.getParent());
            Files.writeString(RECORDS_FILE, GSON.toJson(recs), StandardCharsets.UTF_8);
            recordsMtime = Files.getLastModifiedTime(RECORDS_FILE).toMillis();
            if (beatExisting) {
                OceanEventsService.announce(player.getServer(), "§b[Атлас] §e" + player.getGameProfile().getName()
                        + " §fпобил рекорд вида §b" + displayName + "§f: §e"
                        + String.format("%.2f", weight) + " кг");
            }
        } catch (Exception ignored) {
        }
    }

    /** Первый в жизни вид в атласе: +25 сезонного опыта, чтобы коллекция кормила боевой пропуск. */
    private static void grantFirstCatch(ServerPlayer player, String displayName) {
        player.getCapability(OceanProgressCapability.INSTANCE).ifPresent(cap -> {
            cap.addSeasonXp(25);
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2CSyncOceanProgressPacket(cap));
        });
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§b[Атлас] §aНовый вид: §f" + displayName + " §7(+25 сезонного опыта)"));
    }

    /** Условия текущей поимки: день/ночь, дождь, шторм выходного дня, окно золотой рыбы. */
    public static int conditions(ServerPlayer player) {
        int cond = player.level().isDay() ? COND_DAY : COND_NIGHT;
        if (player.level().isRaining()) cond |= COND_RAIN;
        if (StormEvent.isActive()) cond |= COND_STORM;
        if (OceanEventsService.goldenWindowOpen()) cond |= COND_GOLDEN;
        return cond;
    }

    private static int rollGrade(ServerPlayer player) {
        double silver = 0.10, gold = 0.03, rainbow = 0.008;
        if (StormEvent.isActive()) {
            silver = 0.20;
            gold = 0.06;
            rainbow = 0.015;
        }
        if (OceanEventsService.goldStormRunning()) {
            silver = 0.30;
            gold = 0.10;
            rainbow = 0.03;
        }
        if (OceanEventsService.biteActive()) {
            gold += 0.015;
            rainbow += 0.005;
        }
        if (player.level().getMoonPhase() == 0) {
            gold += 0.02;
            rainbow += 0.012;
        }
        double r = player.getRandom().nextDouble();
        if (r < rainbow) return FishGrade.RAINBOW;
        if (r < rainbow + gold) return FishGrade.GOLD;
        if (r < rainbow + gold + silver) return FishGrade.SILVER;
        return FishGrade.NONE;
    }

    /** Вес только для рекорда атласа; на предмет не пишется, чтобы не ломать цену и турнир. */
    private static double rollWeight(ServerPlayer player) {
        var rnd = player.getRandom();
        return Math.round((0.3 + rnd.nextDouble() * rnd.nextDouble() * 24.0) * 100) / 100.0;
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        if (oldData.contains(KEY, Tag.TAG_COMPOUND)) {
            event.getEntity().getPersistentData().put(KEY, oldData.getCompound(KEY).copy());
        }
    }
}
