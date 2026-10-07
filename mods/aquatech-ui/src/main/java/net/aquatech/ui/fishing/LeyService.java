package net.aquatech.ui.fishing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.OldLeyEntity;
import net.aquatech.ui.registry.ModEntities;
import net.aquatech.ui.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

/**
 * Старый Лей на сервере: раз в неделю всплывает рядом со случайным игроком на полчаса, в зоне радиусом
 * {@link LeyLogic#RADIUS} его можно взять на Удочку Лея, и достаётся он ровно одному игроку.
 * Состояние лежит в config/aquatech_ley.json. Работает в основном потоке сервера.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class LeyService {

    /** Метка именного трофея на предмете. */
    public static final String TROPHY_TAG = "AquaLey";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aquatech_ley.json");
    private static final int CHECK_EVERY_TICKS = 100;
    private static final int WATER_SEARCH_RADIUS = 40;
    private static final long DAY_MS = 24L * 3600_000L;

    private static LeyLogic.State state;
    private static UUID entityId;
    private static ResourceKey<Level> entityLevel;

    private LeyService() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = event.getServer();
        if (event.phase != TickEvent.Phase.END || server == null || server.getTickCount() % CHECK_EVERY_TICKS != 0) {
            return;
        }
        update(server);
    }

    private static void update(MinecraftServer server) {
        LeyLogic.State st = state();
        long now = System.currentTimeMillis();
        ZonedDateTime zdt = ZonedDateTime.now();
        int week = TournamentLogic.weekId(zdt.toLocalDate());
        if (st.week != week) {
            removeEntity(server, false);
            long weekStart = weekStartMs(zdt.toLocalDate(), zdt);
            LeyLogic.startWeek(st, week, weekStart, weekStart + 7 * DAY_MS, server.overworld().getRandom().nextDouble());
            save();
        }
        if (LeyLogic.expired(st, now)) {
            LeyLogic.close(st);
            save();
            removeEntity(server, false);
            broadcast(server, "§6[Старый Лей] §7Лей ушёл на глубину. До следующей недели его не увидеть.");
            return;
        }
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (LeyLogic.due(st, now, players.size())) {
            ServerPlayer anchor = players.get(server.overworld().getRandom().nextInt(players.size()));
            appear(server, st, now, anchor);
        }
        if (LeyLogic.active(st, now)) {
            ensureEntity(server, st);
        }
    }

    /** Принудительно открывает окно у игрока (команда оператора), даже если на этой неделе Лей уже был. */
    public static void forceAppear(ServerPlayer anchor) {
        LeyLogic.State st = state();
        st.appeared = false;
        st.caught = false;
        st.closed = false;
        removeEntity(anchor.server, false);
        appear(anchor.server, st, System.currentTimeMillis(), anchor);
    }

    /** Принудительно закрывает окно (команда оператора). */
    public static void forceClose(MinecraftServer server) {
        LeyLogic.State st = state();
        if (st.appeared && !st.caught) {
            LeyLogic.close(st);
            save();
            removeEntity(server, false);
            broadcast(server, "§6[Старый Лей] §7Лей ушёл на глубину.");
        }
    }

    public static String status() {
        LeyLogic.State st = state();
        long now = System.currentTimeMillis();
        if (st.caught) {
            return "Пойман: " + st.caughtBy + " (" + FishWeight.format(st.caughtGrams) + ")";
        }
        if (LeyLogic.active(st, now)) {
            long left = Math.max(0L, st.activeUntilMs - now) / 60_000L;
            return "Идёт окно, осталось ~" + left + " мин, точка " + (int) st.x + " " + (int) st.z + " (" + st.dimension + ")";
        }
        if (st.appeared) {
            return "Окно этой недели закончилось";
        }
        return "Ждёт: появится после " + java.time.Instant.ofEpochMilli(st.appearAtMs);
    }

    /** Лей на воде и окно открыто: его можно брать на крючок. Иначе null. */
    public static OldLeyEntity activeLey(MinecraftServer server) {
        LeyLogic.State st = state();
        if (!LeyLogic.active(st, System.currentTimeMillis()) || entityId == null || entityLevel == null) {
            return null;
        }
        ServerLevel level = server.getLevel(entityLevel);
        Entity existing = level == null ? null : level.getEntity(entityId);
        return existing instanceof OldLeyEntity ley && ley.isAlive() ? ley : null;
    }

    /** Игрок вымотал Лея: именной трофей в инвентарь, монеты, объявление, прыжок на прощание. */
    public static void award(ServerPlayer player) {
        LeyLogic.State st = state();
        int grams = LeyLogic.rollGrams(player.getRandom().nextDouble());
        int cm = LeyLogic.rollCm(player.getRandom().nextDouble());
        ItemStack trophy = trophy(player, grams, cm);
        if (!player.getInventory().add(trophy)) {
            player.drop(trophy, false);
        }
        LeyLogic.markCaught(st, player.getGameProfile().getName(), player.getUUID().toString(), grams);
        save();
        MinecraftServer server = player.server;
        leapAndForget(server);
        OceanEventsService.grantCoins(player, LeyLogic.REWARD_COINS);
        player.connection.send(new ClientboundSetTitlesAnimationPacket(6, 70, 20));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§6★ СТАРЫЙ ЛЕЙ ★")));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal("§e" + FishWeight.format(grams) + ", " + cm + " см · +" + LeyLogic.REWARD_COINS + " монет")));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 0.8F);
        broadcast(server, "§6[Старый Лей] §f" + player.getGameProfile().getName() + " §eвытащил Старого Лея! §7("
                + FishWeight.format(grams) + ", " + cm + " см) §eЭта рыба достаётся одному на всю неделю.");
    }

    private static ItemStack trophy(ServerPlayer player, int grams, int cm) {
        String name = player.getGameProfile().getName();
        String date = LocalDate.now().toString();
        ItemStack stack = new ItemStack(ModItems.OLD_LEY.get());
        stack.setHoverName(Component.literal("§6Старый Лей §e«" + name + "»").withStyle(s -> s.withItalic(false)));
        CompoundTag tag = new CompoundTag();
        tag.putString("owner", name);
        tag.putString("uuid", player.getUUID().toString());
        tag.putInt("grams", grams);
        tag.putInt("cm", cm);
        tag.putString("date", date);
        stack.getOrCreateTag().put(TROPHY_TAG, tag);
        ListTag lore = new ListTag();
        lore.add(loreLine("§6★ Легендарная рыба: одна на сервер в неделю"));
        lore.add(loreLine("§7Вес: §f" + FishWeight.format(grams) + "§7, длина: §f" + cm + " см"));
        lore.add(loreLine("§7Поймал §f" + name + "§7, " + date));
        stack.getOrCreateTagElement("display").put("Lore", lore);
        return stack;
    }

    private static StringTag loreLine(String text) {
        return StringTag.valueOf(Component.Serializer.toJson(Component.literal(text).withStyle(s -> s.withItalic(false))));
    }

    private static void appear(MinecraftServer server, LeyLogic.State st, long now, ServerPlayer anchor) {
        String dimension = anchor.level().dimension().location().toString();
        LeyLogic.appear(st, now, dimension, anchor.getX(), anchor.getZ());
        save();
        ensureEntity(server, st);
        broadcast(server, "§6[Старый Лей] §eИз глубины поднимается Старый Лей! Он кружит у §b" + anchor.getGameProfile().getName()
                + " §7(около " + (int) st.x + " " + (int) st.z + ")§e. §fНа вылов 30 минут, зона " + LeyLogic.RADIUS
                + " блоков. Нужна Удочка Лея: ПКМ в сторону воды, потом держите леску. Достанется одному.");
    }

    private static void ensureEntity(MinecraftServer server, LeyLogic.State st) {
        if (entityId != null && entityLevel != null) {
            ServerLevel level = server.getLevel(entityLevel);
            Entity existing = level == null ? null : level.getEntity(entityId);
            if (existing != null && existing.isAlive()) {
                return;
            }
        }
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(st.dimension)));
        if (level == null) {
            return;
        }
        BlockPos water = findWater(level, BlockPos.containing(st.x, level.getSeaLevel(), st.z));
        if (water == null) {
            AquaTechUI.LOGGER.warn("[ley] воды в радиусе {} блоков от {} {} нет, Лея не видно", WATER_SEARCH_RADIUS, (int) st.x, (int) st.z);
            return;
        }
        OldLeyEntity ley = ModEntities.OLD_LEY.get().create(level);
        if (ley == null) {
            return;
        }
        ley.anchorAt(water.getX() + 0.5, water.getY() + 0.89 - 0.45, water.getZ() + 0.5);
        level.addFreshEntity(ley);
        entityId = ley.getUUID();
        entityLevel = level.dimension();
        AquaTechUI.LOGGER.info("[ley] Лей появился в {} на {} {} {}", st.dimension, water.getX(), water.getY(), water.getZ());
    }

    /** Ближайшая к точке поверхность воды по расширяющемуся кольцу; null, если воды в радиусе нет. */
    static BlockPos findWater(ServerLevel level, BlockPos origin) {
        for (int r = 0; r <= WATER_SEARCH_RADIUS; r += 4) {
            int steps = r == 0 ? 1 : 8;
            for (int k = 0; k < steps; k++) {
                double angle = k * (2.0D * Math.PI / steps);
                int x = origin.getX() + (int) Math.round(r * Math.cos(angle));
                int z = origin.getZ() + (int) Math.round(r * Math.sin(angle));
                if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                    continue;
                }
                for (int y = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + 40); y >= level.getMinBuildHeight(); y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (level.getFluidState(p).is(FluidTags.WATER) && !level.getFluidState(p.above()).is(FluidTags.WATER)) {
                        return p;
                    }
                    if (y < origin.getY() - 40) {
                        break;
                    }
                }
            }
        }
        return null;
    }

    private static void leapAndForget(MinecraftServer server) {
        removeEntity(server, true);
    }

    private static void removeEntity(MinecraftServer server, boolean leap) {
        if (entityId != null && entityLevel != null) {
            ServerLevel level = server.getLevel(entityLevel);
            Entity existing = level == null ? null : level.getEntity(entityId);
            if (existing instanceof OldLeyEntity ley) {
                if (leap) {
                    ley.leapAndLeave();
                } else {
                    ley.discard();
                }
            }
        }
        entityId = null;
        entityLevel = null;
    }

    private static long weekStartMs(LocalDate today, ZonedDateTime zdt) {
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(zdt.getZone()).toInstant().toEpochMilli();
    }

    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    private static LeyLogic.State state() {
        if (state == null) {
            LeyLogic.State loaded = null;
            try {
                if (Files.exists(FILE)) {
                    loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), LeyLogic.State.class);
                }
            } catch (IOException | RuntimeException e) {
                AquaTechUI.LOGGER.warn("[ley] не удалось прочитать {}: {}", FILE.getFileName(), e.toString());
                try {
                    Files.copy(FILE, FILE.resolveSibling(FILE.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                }
            }
            state = loaded != null ? loaded : new LeyLogic.State();
            if (state.dimension == null) state.dimension = "";
            if (state.caughtBy == null) state.caughtBy = "";
            if (state.caughtByUuid == null) state.caughtByUuid = "";
        }
        return state;
    }

    private static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(state()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            AquaTechUI.LOGGER.warn("[ley] не удалось записать {}: {}", FILE.getFileName(), e.toString());
        }
    }
}
