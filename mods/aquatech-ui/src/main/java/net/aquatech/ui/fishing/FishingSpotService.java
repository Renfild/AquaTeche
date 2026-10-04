package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.S2CSpotPacket;
import net.aquatech.ui.skyblock.WorldGuardIslandLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Личные точки лова. Рядом с игроком на воде появляется точка одного из {@link SpotType},
 * видимая только ему. Рыба, пойманная в радиусе точки, получает NBT-метку {@link #TAG}
 * (множитель цены как float), а скупщик aqualumen (FishShopConfig) умножает её цену на него.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class FishingSpotService {

    /** Тот же тег читает aqualumen FishShopConfig: модули связаны только через NBT. */
    public static final String TAG = "AquaSpotMult";
    private static final float ZHILA_GRADE_BOOST_CHANCE = 0.2f;

    private static final long FIRST_SPOT_DELAY_MS = 3L * 60_000L;
    /** Новичку — быстро и гарантированно Заводь, пока фича ещё в новинку. */
    private static final long FIRST_SPOT_DELAY_NEW_PLAYER_MS = 75L * 1000L;
    private static final String TAG_SPOT_INTRO_DONE = "aquatech_ui:spot_intro_done";
    private static final long RETRY_DELAY_MS = 2L * 60_000L;
    private static final double MIN_GAP_BETWEEN_SPOTS = 40.0;
    private static final int WATER_DEPTH = 5;
    static final int OPEN_WATER_RING = 6;
    private static final double PARTICLE_RANGE = 220.0;
    private static final int RING_POINTS = 18;

    private static final class Spot {
        final BlockPos pos;
        final SpotType type;
        final long expiresAt;
        int catchesLeft;

        Spot(BlockPos pos, SpotType type, long expiresAt, int catchesLeft) {
            this.pos = pos;
            this.type = type;
            this.expiresAt = expiresAt;
            this.catchesLeft = catchesLeft;
        }
    }

    private static final Map<UUID, Spot> SPOTS = new ConcurrentHashMap<>();
    /** Живёт до перезапуска сервера: перезаход не даёт новую точку раньше срока. */
    private static final Map<UUID, Long> NEXT_AT = new ConcurrentHashMap<>();

    private FishingSpotService() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !ModConfig.SPOT_ENABLED.get()) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        int tick = server.getTickCount();
        if (tick % 10 != 0) return;
        long now = System.currentTimeMillis();
        boolean everySecond = tick % 20 == 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Spot spot = SPOTS.get(player.getUUID());
            if (spot == null) {
                if (everySecond) {
                    maybeSpawn(player, now);
                }
                continue;
            }
            if (now >= spot.expiresAt || spot.catchesLeft <= 0
                    || !player.level().dimension().equals(Level.OVERWORLD)) {
                expire(player, spot);
                continue;
            }
            emitParticles(player, spot, tick);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            SPOTS.remove(event.getEntity().getUUID());
        }
    }

    private static void maybeSpawn(ServerPlayer player, long now) {
        if (player.isSpectator() || !player.level().dimension().equals(Level.OVERWORLD)) return;
        boolean introDone = player.getPersistentData().getBoolean(TAG_SPOT_INTRO_DONE);
        long firstDelay = introDone ? FIRST_SPOT_DELAY_MS : FIRST_SPOT_DELAY_NEW_PLAYER_MS;
        long nextAt = NEXT_AT.computeIfAbsent(player.getUUID(), id -> now + firstDelay);
        if (now < nextAt) return;
        if (!PersonalSpotFinder.isNearOwnRaft(player.serverLevel(), player, PersonalSpotFinder.AT_HOME_BLOCKS)) {
            NEXT_AT.put(player.getUUID(), now + PersonalSpotFinder.AWAY_RETRY_MS);
            return;
        }
        if (!spawn(player, now, !introDone)) {
            NEXT_AT.put(player.getUUID(), now + RETRY_DELAY_MS);
        }
    }

    private static boolean spawn(ServerPlayer player, long now) {
        return spawn(player, now, false);
    }

    /** forceZavod — новичку: первая точка всегда Заводь, без сюрприза редкого типа. */
    private static boolean spawn(ServerPlayer player, long now, boolean forceZavod) {
        BlockPos place = findPlace(player.serverLevel(), player);
        if (place == null) return false;
        SpotType type = forceZavod ? SpotType.ZAVOD : SpotType.roll(player.getRandom());
        if (forceZavod) {
            player.getPersistentData().putBoolean(TAG_SPOT_INTRO_DONE, true);
        }
        Spot spot = new Spot(place, type, now + type.lifetimeMinutes() * 60_000L, type.maxCatches());
        SPOTS.put(player.getUUID(), spot);
        int minMinutes = ModConfig.SPOT_MIN_MINUTES.get();
        int maxMinutes = Math.max(minMinutes, ModConfig.SPOT_MAX_MINUTES.get());
        long gap = (minMinutes + player.getRandom().nextInt(maxMinutes - minMinutes + 1)) * 60_000L;
        NEXT_AT.put(player.getUUID(), spot.expiresAt + gap);
        announce(player, spot);
        sendState(player, spot);
        return true;
    }

    /** Вокруг плота игрока, а не вокруг того места, где он сейчас стоит: у каждого своя вода. */
    private static BlockPos findPlace(ServerLevel level, ServerPlayer player) {
        return PersonalSpotFinder.find(level, player, ModConfig.SPOT_MIN_DISTANCE.get(),
                ModConfig.SPOT_MAX_DISTANCE.get(), surface -> !tooCloseToOtherSpot(surface));
    }

    /** Только уже загруженные чанки: генерация на ходу бьёт по TPS. */
    static BlockPos waterSurface(ServerLevel level, int x, int z) {
        if (!level.hasChunkAt(new BlockPos(x, 0, z))) return null;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        BlockPos pos = new BlockPos(x, y, z);
        return level.getFluidState(pos).is(FluidTags.WATER) ? pos : null;
    }

    static boolean isOpenWater(ServerLevel level, BlockPos surface) {
        if (!level.getBlockState(surface.above()).isAir()) return false;
        for (int depth = 1; depth <= WATER_DEPTH; depth++) {
            if (!level.getFluidState(surface.below(depth)).is(FluidTags.WATER)) return false;
        }
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            BlockPos ring = new BlockPos(
                    surface.getX() + (int) Math.round(Math.cos(angle) * OPEN_WATER_RING),
                    surface.getY(),
                    surface.getZ() + (int) Math.round(Math.sin(angle) * OPEN_WATER_RING));
            if (!level.hasChunkAt(ring) || !level.getFluidState(ring).is(FluidTags.WATER)) return false;
        }
        return true;
    }

    private static boolean tooCloseToOtherSpot(BlockPos candidate) {
        for (Spot other : SPOTS.values()) {
            if (other.pos.distSqr(candidate) < MIN_GAP_BETWEEN_SPOTS * MIN_GAP_BETWEEN_SPOTS) return true;
        }
        return false;
    }

    private static void announce(ServerPlayer player, Spot spot) {
        double dx = spot.pos.getX() + 0.5 - player.getX();
        double dz = spot.pos.getZ() + 0.5 - player.getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        SpotType type = spot.type;
        player.sendSystemMessage(Component.literal(type.chatColor() + "[" + type.label() + "] §fПоявилась точка лова: §e"
                + distance + " м §f" + compass(dx, dz) + "§f. Рыба оттуда продаётся §6×"
                + type.multLabel() + "§f дороже. §7Живёт " + type.lifetimeMinutes() + " мин."));
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
                1.0F, type == SpotType.ZHILA ? 1.6F : 1.3F);
    }

    /** В Minecraft +X это восток, +Z это юг. */
    private static String compass(double dx, double dz) {
        String[] names = {"на восток", "на юго-восток", "на юг", "на юго-запад",
                "на запад", "на северо-запад", "на север", "на северо-восток"};
        double degrees = Math.toDegrees(Math.atan2(dz, dx));
        int index = (int) Math.round(((degrees + 360.0) % 360.0) / 45.0) % 8;
        return names[index];
    }

    private static void expire(ServerPlayer player, Spot spot) {
        SPOTS.remove(player.getUUID(), spot);
        String reason = spot.catchesLeft <= 0 ? "иссякла" : "погасла";
        player.sendSystemMessage(Component.literal("§b[" + spot.type.label() + "] §7Твоя точка " + reason
                + ". Следующая появится позже."));
        player.playNotifySound(SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.PLAYERS, 0.8F, 0.7F);
        sendClear(player);
    }

    private static void emitParticles(ServerPlayer player, Spot spot, int tick) {
        double cx = spot.pos.getX() + 0.5;
        double cz = spot.pos.getZ() + 0.5;
        double surfaceY = spot.pos.getY() + 1.0;
        if (player.distanceToSqr(cx, surfaceY, cz) > PARTICLE_RANGE * PARTICLE_RANGE) return;
        ServerLevel level = player.serverLevel();
        double radius = ModConfig.SPOT_RADIUS.get();
        double phase = tick * 0.05;
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = phase + i * (Math.PI * 2.0 / RING_POINTS);
            level.sendParticles(player, ParticleTypes.FISHING, true,
                    cx + Math.cos(angle) * radius, surfaceY, cz + Math.sin(angle) * radius,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(player, ParticleTypes.SPLASH, true, cx, surfaceY, cz, 8, 1.0, 0.1, 1.0, 0.1);
        // Жила искрит гуще — заметно ещё до того, как игрок прочитал тип в чате.
        int sparkles = spot.type == SpotType.ZHILA ? 6 : 3;
        level.sendParticles(player, ParticleTypes.END_ROD, true, cx, surfaceY + 1.0, cz, sparkles, 0.2, 1.5, 0.2, 0.01);
    }

    /**
     * Ставит метку на рыбу из улова, если ловил владелец точки рядом с ней.
     * Поплавок может уже не существовать (мини-игра), тогда меряем по игроку.
     */
    public static void stamp(ServerPlayer player, FishingHook hook, List<ItemStack> drops) {
        if (!ModConfig.SPOT_ENABLED.get() || drops == null || drops.isEmpty()) return;
        Spot spot = SPOTS.get(player.getUUID());
        if (spot == null || System.currentTimeMillis() >= spot.expiresAt) return;
        double ax = hook != null ? hook.getX() : player.getX();
        double az = hook != null ? hook.getZ() : player.getZ();
        double dx = ax - (spot.pos.getX() + 0.5);
        double dz = az - (spot.pos.getZ() + 0.5);
        double radius = ModConfig.SPOT_RADIUS.get();
        if (dx * dx + dz * dz > radius * radius) return;
        SpotType type = spot.type;
        float mult = (float) type.priceMult();
        int stamped = 0;
        for (ItemStack stack : drops) {
            if (!isSpotFish(stack)) continue;
            stack.getOrCreateTag().putFloat(TAG, mult);
            stamped++;
            if (type.gradeBoost()) {
                maybeBoostGrade(player, stack);
            }
        }
        if (stamped == 0) return;
        spot.catchesLeft--;
        player.displayClientMessage(Component.literal("§b[" + type.label() + "] §fРыба §6×" + type.multLabel()
                + "§f к цене §7(осталось уловов: " + Math.max(0, spot.catchesLeft) + ")"), true);
        sendState(player, spot);
    }

    /** Жила: рыбе без грейда даёт шанс стать серебряной, задним числом (и правит атлас). */
    private static void maybeBoostGrade(ServerPlayer player, ItemStack stack) {
        if (FishGrade.of(stack) != FishGrade.NONE) return;
        if (player.getRandom().nextFloat() >= ZHILA_GRADE_BOOST_CHANCE) return;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) return;
        FishGrade.set(stack, FishGrade.SILVER);
        FishingAtlasService.bumpSpeciesGrade(player, key.toString(), FishGrade.SILVER);
    }

    private static boolean isSpotFish(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (FishingLootHandler.isStarCatcherFishItem(stack)) return true;
        return stack.is(Items.COD) || stack.is(Items.SALMON)
                || stack.is(Items.TROPICAL_FISH) || stack.is(Items.PUFFERFISH);
    }

    private static void sendState(ServerPlayer player, Spot spot) {
        send(player, new S2CSpotPacket(true, spot.pos,
                Math.max(0L, spot.expiresAt - System.currentTimeMillis()),
                Math.max(0, spot.catchesLeft), ModConfig.SPOT_RADIUS.get(),
                spot.type.label(), (float) spot.type.priceMult(), spot.type.colorRgb()));
    }

    private static void sendClear(ServerPlayer player) {
        send(player, new S2CSpotPacket(false, BlockPos.ZERO, 0L, 0, 0, "", 0f, 0));
    }

    /** Клиент без мода не знает этот канал: не шлём ему пакет. */
    private static void send(ServerPlayer player, S2CSpotPacket packet) {
        try {
            if (player.connection == null || !NetworkHandler.CHANNEL.isRemotePresent(player.connection.connection)) return;
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        } catch (RuntimeException error) {
            AquaTechUI.LOGGER.debug("[FishingSpot] packet skipped: {}", error.toString());
        }
    }

    /** "2" или "3.5" — без лишних нулей после точки. Используется и на клиенте (тултип/HUD). */
    public static String formatMult(float mult) {
        return mult == (float) Math.floor(mult) ? String.valueOf((int) mult) : String.valueOf(mult);
    }

    // ── админ-команды /aquatech spot ────────────────────────────────────────

    public static boolean forceSpawn(ServerPlayer player) {
        Spot old = SPOTS.remove(player.getUUID());
        if (old != null) sendClear(player);
        return spawn(player, System.currentTimeMillis());
    }

    public static boolean forceClear(ServerPlayer player) {
        Spot old = SPOTS.remove(player.getUUID());
        if (old == null) return false;
        sendClear(player);
        return true;
    }

    /** Тестовая команда /aquatech debug: возвращает игрока в состояние «новичок» для точек лова. */
    public static void resetIntro(ServerPlayer player) {
        player.getPersistentData().remove(TAG_SPOT_INTRO_DONE);
        NEXT_AT.remove(player.getUUID());
        Spot old = SPOTS.remove(player.getUUID());
        if (old != null) sendClear(player);
    }

    public static String info(ServerPlayer player) {
        Spot spot = SPOTS.get(player.getUUID());
        if (spot == null) {
            Long nextAt = NEXT_AT.get(player.getUUID());
            long wait = nextAt == null ? 0L : Math.max(0L, nextAt - System.currentTimeMillis());
            return "Точки нет. Следующая через ~" + (wait / 60_000L) + " мин.";
        }
        long left = Math.max(0L, spot.expiresAt - System.currentTimeMillis());
        return "Точка (" + spot.type.label() + ") " + spot.pos.getX() + " " + spot.pos.getY() + " " + spot.pos.getZ()
                + ", осталось " + (left / 60_000L) + " мин, уловов " + spot.catchesLeft;
    }
}
