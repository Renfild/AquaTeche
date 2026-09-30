package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.skyblock.WorldGuardIslandLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * «Сокровище из глубин»: раз в пару часов посреди океана поднимается светящийся сундук со столбом света.
 * Кто первым доплывёт и нажмёт по нему, забирает 5000–10000 монет и с шансом ключ кейса.
 * Состояние живёт только в памяти: перезапуск сервера просто снимает событие, а оставшиеся в чанках
 * сущности сундука удаляются при загрузке (см. {@link #onEntityJoin}).
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class DeepChestService {

    private static final String ENTITY_TAG = "aquatech_deepchest";
    private static final long FIRST_DELAY_MS = 20L * 60_000L;
    private static final long RETRY_NO_PLAYERS_MS = 5L * 60_000L;
    private static final long RETRY_NO_PLACE_MS = 2L * 60_000L;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int BEAM_HEIGHT = 70;
    private static final double PARTICLE_RANGE = 260.0;
    private static final double NEAR_DISTANCE = 12.0;
    /** Модель сундука 32 юнита в ширину: масштаб 0.7 даёт ~1.4 блока. */
    private static final float MODEL_SCALE = 0.7f;
    /** Если в игре сундук повёрнут спиной к игроку, поменяй на 180. */
    private static final float MODEL_YAW_OFFSET = 0.0f;
    private static final double WATER_LEVEL_OFFSET = 0.85;

    private static final class Chest {
        final ServerLevel level;
        final BlockPos surface;
        final long endsAt;
        final long totalMs;
        final UUID displayId;
        final UUID hitboxId;
        final ServerBossEvent bar;

        Chest(ServerLevel level, BlockPos surface, long endsAt, long totalMs, UUID displayId, UUID hitboxId,
              ServerBossEvent bar) {
            this.level = level;
            this.surface = surface;
            this.endsAt = endsAt;
            this.totalMs = totalMs;
            this.displayId = displayId;
            this.hitboxId = hitboxId;
            this.bar = bar;
        }
    }

    private static Chest active;
    private static long nextAt;
    private static UUID lastWinner;

    private DeepChestService() {
    }

    // ─────────────────────────── управление ───────────────────────────

    /** Для команды /aquatech deepchest: запускает событие сразу, не дожидаясь расписания. */
    public static boolean startNow(MinecraftServer server) {
        return active == null && begin(server, System.currentTimeMillis());
    }

    public static boolean stopNow(MinecraftServer server) {
        if (active == null) return false;
        cleanup(active);
        active = null;
        nextAt = System.currentTimeMillis() + nextDelay();
        return true;
    }

    public static String statusLine() {
        if (active != null) {
            return "§6Сокровище глубин активно: §f" + active.surface.getX() + " " + active.surface.getZ()
                    + " §7, осталось " + DeepChestLogic.timeLeft(active.endsAt - System.currentTimeMillis());
        }
        long left = nextAt - System.currentTimeMillis();
        return "§7Сокровища сейчас нет. Следующее через ~" + (nextAt == 0L ? "?" : Math.max(0L, left / 60_000L) + " мин");
    }

    // ─────────────────────────── тик ───────────────────────────

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        int tick = server.getTickCount();
        if (tick % 10 != 0) return;
        long now = System.currentTimeMillis();
        if (active != null) {
            tickActive(server, active, now, tick % 20 == 0);
            return;
        }
        if (!ModConfig.DEEP_CHEST_ENABLED.get()) return;
        if (nextAt == 0L) nextAt = now + FIRST_DELAY_MS;
        if (now >= nextAt) begin(server, now);
    }

    private static void tickActive(MinecraftServer server, Chest chest, long now, boolean everySecond) {
        long left = chest.endsAt - now;
        if (left <= 0L) {
            fizzle(server, chest);
            return;
        }
        double cx = chest.surface.getX() + 0.5;
        double cz = chest.surface.getZ() + 0.5;
        double baseY = chest.surface.getY() + 1.0;
        for (ServerPlayer player : chest.level.players()) {
            if (player.distanceToSqr(cx, baseY, cz) > PARTICLE_RANGE * PARTICLE_RANGE) continue;
            for (int dy = 0; dy <= BEAM_HEIGHT; dy += 2) {
                chest.level.sendParticles(player, ParticleTypes.END_ROD, true, cx, baseY + dy, cz, 1, 0.05, 0.0, 0.05, 0.0);
            }
            chest.level.sendParticles(player, ParticleTypes.FIREWORK, true, cx, baseY + 0.6, cz, 6, 0.5, 0.3, 0.5, 0.05);
            chest.level.sendParticles(player, ParticleTypes.SPLASH, true, cx, baseY, cz, 12, 1.2, 0.1, 1.2, 0.1);
        }
        if (!everySecond) return;
        chest.bar.setProgress(DeepChestLogic.progress(left, chest.totalMs));
        chest.bar.setName(Component.literal("§6Сокровище из глубин §7— осталось §e" + DeepChestLogic.timeLeft(left)));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(Level.OVERWORLD)) continue;
            chest.bar.addPlayer(player);
            double dx = cx - player.getX();
            double dz = cz - player.getZ();
            String text = Math.sqrt(dx * dx + dz * dz) <= NEAR_DISTANCE
                    ? "§a§lТы у сундука! §fЖми по нему!"
                    : "§6Сокровище: §e" + DeepChestLogic.distance(dx, dz) + " м §f" + DeepChestLogic.compass(dx, dz);
            player.displayClientMessage(Component.literal(text), true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (active != null && event.getEntity() instanceof ServerPlayer player) {
            active.bar.removePlayer(player);
        }
    }

    // ─────────────────────────── старт ───────────────────────────

    private static boolean begin(MinecraftServer server, long now) {
        List<ServerPlayer> anchors = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isSpectator() && player.level().dimension().equals(Level.OVERWORLD)) anchors.add(player);
        }
        if (anchors.isEmpty()) {
            nextAt = now + RETRY_NO_PLAYERS_MS;
            return false;
        }
        ServerPlayer anchor = anchors.get(anchors.size() == 1 ? 0 : new Random().nextInt(anchors.size()));
        ServerLevel level = anchor.serverLevel();
        BlockPos surface = findPlace(level, anchor);
        if (surface == null) {
            nextAt = now + RETRY_NO_PLACE_MS;
            return false;
        }
        Display.ItemDisplay display = EntityType.ITEM_DISPLAY.create(level);
        Interaction hitbox = EntityType.INTERACTION.create(level);
        if (display == null || hitbox == null) {
            nextAt = now + RETRY_NO_PLACE_MS;
            return false;
        }
        configureDisplay(display);
        configureHitbox(hitbox);
        double x = surface.getX() + 0.5;
        double y = surface.getY() + WATER_LEVEL_OFFSET;
        double z = surface.getZ() + 0.5;
        float yaw = (float) Math.toDegrees(Math.atan2(-(anchor.getX() - x), anchor.getZ() - z)) + MODEL_YAW_OFFSET;
        display.moveTo(x, y, z, yaw, 0.0f);
        hitbox.moveTo(x, y, z);
        display.addTag(ENTITY_TAG);
        hitbox.addTag(ENTITY_TAG);

        long totalMs = ModConfig.DEEP_CHEST_DURATION_MINUTES.get() * 60_000L;
        ServerBossEvent bar = new ServerBossEvent(Component.literal("§6Сокровище из глубин"),
                BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        active = new Chest(level, surface, now + totalMs, totalMs, display.getUUID(), hitbox.getUUID(), bar);
        level.addFreshEntity(display);
        level.addFreshEntity(hitbox);
        strike(level, x, y, z);

        OceanEventsService.announce(server, "§6§l[Сокровище глубин] §eИз глубин поднялся светящийся сундук! §fКто первым "
                + "доберётся и нажмёт по нему, заберёт §65 000–10 000 монет §fи с шансом §bключ кейса§f. §7У тебя "
                + DeepChestLogic.timeLeft(totalMs) + ": следи за полосой вверху и подсказкой над хотбаром.");
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            bar.addPlayer(player);
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§6Сокровище из глубин")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§eУспей добраться первым!")));
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }

    private static BlockPos findPlace(ServerLevel level, ServerPlayer anchor) {
        int minDistance = ModConfig.DEEP_CHEST_MIN_DISTANCE.get();
        int maxDistance = Math.max(minDistance + 1, ModConfig.DEEP_CHEST_MAX_DISTANCE.get());
        RandomSource random = anchor.getRandom();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            int distance = minDistance + random.nextInt(maxDistance - minDistance + 1);
            int x = anchor.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = anchor.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            BlockPos surface = FishingSpotService.waterSurface(level, x, z);
            if (surface == null || !FishingSpotService.isOpenWater(level, surface)) continue;
            if (WorldGuardIslandLookup.ownerAt(level, surface) != null) continue;
            return surface;
        }
        return null;
    }

    /** Данные дисплея задаются через NBT: публичного API для item и transformation в 1.20.1 нет. */
    private static void configureDisplay(Display.ItemDisplay display) {
        CompoundTag tag = new CompoundTag();
        display.saveWithoutId(tag);
        CompoundTag item = new CompoundTag();
        item.putString("id", "aquatech_ui:deep_chest_model");
        item.putByte("Count", (byte) 1);
        tag.put("item", item);
        tag.putString("item_display", "none");
        tag.putBoolean("Glowing", true);
        tag.putInt("glow_color_override", 0xFFC93C);
        CompoundTag brightness = new CompoundTag();
        brightness.putInt("sky", 15);
        brightness.putInt("block", 15);
        tag.put("brightness", brightness);
        CompoundTag transformation = new CompoundTag();
        transformation.put("translation", floats(0.0f, 0.5f * MODEL_SCALE, 0.0f));
        transformation.put("scale", floats(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE));
        transformation.put("left_rotation", floats(0.0f, 0.0f, 0.0f, 1.0f));
        transformation.put("right_rotation", floats(0.0f, 0.0f, 0.0f, 1.0f));
        tag.put("transformation", transformation);
        display.load(tag);
    }

    private static void configureHitbox(Interaction hitbox) {
        CompoundTag tag = new CompoundTag();
        hitbox.saveWithoutId(tag);
        tag.putFloat("width", 2.0f);
        tag.putFloat("height", 1.6f);
        tag.putBoolean("response", true);
        hitbox.load(tag);
    }

    private static ListTag floats(float... values) {
        ListTag list = new ListTag();
        for (float value : values) list.add(FloatTag.valueOf(value));
        return list;
    }

    // ─────────────────────────── забирание награды ───────────────────────────

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (tryClaim(event.getTarget(), event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (tryClaim(event.getTarget(), event.getEntity())) event.setCanceled(true);
    }

    /** true, если цель это хитбокс сундука (событие поглощаем, даже когда награду не отдали). */
    private static boolean tryClaim(Entity target, net.minecraft.world.entity.player.Player who) {
        Chest chest = active;
        if (chest == null || !(target instanceof Interaction) || !target.getUUID().equals(chest.hitboxId)) return false;
        if (!(who instanceof ServerPlayer player)) return true;
        MinecraftServer server = player.getServer();
        if (server == null) return true;
        if (!DeepChestLogic.mayClaim(lastWinner, player.getUUID(), server.getPlayerList().getPlayerCount())) {
            player.displayClientMessage(Component.literal("§cПрошлое сокровище забрал ты. Дай шанс другим!"), true);
            return true;
        }
        DeepChestLogic.Reward reward = DeepChestLogic.rollReward(new Random());
        OceanEventsService.grantCoins(player, reward.coins());
        if (reward.caseId() != null) OceanEventsService.grantCaseKey(player, reward.caseId());
        lastWinner = player.getUUID();

        ServerLevel level = chest.level;
        double x = chest.surface.getX() + 0.5;
        double y = chest.surface.getY() + 1.5;
        double z = chest.surface.getZ() + 0.5;
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 140, 1.4, 1.6, 1.4, 0.5);
        level.playSound(null, chest.surface, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
        strike(level, x, y, z);

        String keyPart = reward.caseId() == null ? ""
                : " §7и §bключ Кейса " + TournamentLogic.caseNumeral(reward.caseId());
        OceanEventsService.announce(server, "§6§l[Сокровище глубин] §e" + player.getGameProfile().getName()
                + " §fпервым добрался до сундука и забрал §6+" + reward.coins() + " монет" + keyPart + "§f!");
        player.sendSystemMessage(Component.literal("§6[Сокровище глубин] §aТвоя награда: §6+" + reward.coins()
                + " монет" + (reward.caseId() == null ? "" : " §aи §bключ Кейса " + TournamentLogic.caseNumeral(reward.caseId())) + "§a!"));
        cleanup(chest);
        active = null;
        nextAt = System.currentTimeMillis() + nextDelay();
        return true;
    }

    private static void fizzle(MinecraftServer server, Chest chest) {
        chest.level.sendParticles(ParticleTypes.SPLASH, chest.surface.getX() + 0.5, chest.surface.getY() + 1.0,
                chest.surface.getZ() + 0.5, 80, 1.4, 0.4, 1.4, 0.3);
        OceanEventsService.announce(server, "§6[Сокровище глубин] §7Никто не успел: сундук ушёл на дно. Следующий поднимется позже.");
        cleanup(chest);
        active = null;
        nextAt = System.currentTimeMillis() + nextDelay();
    }

    private static void cleanup(Chest chest) {
        chest.bar.removeAllPlayers();
        for (UUID id : new UUID[]{chest.displayId, chest.hitboxId}) {
            Entity entity = chest.level.getEntity(id);
            if (entity != null) entity.discard();
        }
    }

    /** Сущности сундука сохраняются в чанках: после рестарта или выгрузки чанка лишние удаляем. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide() || !entity.getTags().contains(ENTITY_TAG)) return;
        Chest chest = active;
        boolean known = chest != null && (entity.getUUID().equals(chest.displayId) || entity.getUUID().equals(chest.hitboxId));
        if (!known) event.setCanceled(true);
    }

    private static void strike(ServerLevel level, double x, double y, double z) {
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(x, y, z);
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
    }

    private static long nextDelay() {
        return DeepChestLogic.nextDelayMs(new Random(), ModConfig.DEEP_CHEST_MIN_MINUTES.get(),
                ModConfig.DEEP_CHEST_MAX_MINUTES.get());
    }
}
