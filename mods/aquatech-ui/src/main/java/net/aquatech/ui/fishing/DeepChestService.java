package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
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
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * «Сокровище из глубин»: у каждого игрока раз в пару часов возле его плота всплывает личный пиратский сундук
 * со столбом света. Полоса, подсказка, столб света и награда принадлежат только владельцу: нажав по сундуку,
 * он забирает 5000–10000 монет и с шансом ключ кейса. Чужой сундук забрать нельзя.
 * Корпус и крышка это два ItemDisplay: крышка приоткрывается, стучит, при победе распахивается с фейерверком.
 * Состояние живёт только в памяти: перезапуск сервера снимает событие, а оставшиеся в чанках сущности
 * сундука удаляются при загрузке (см. {@link #onEntityJoin}).
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class DeepChestService {

    private static final String ENTITY_TAG = "aquatech_deepchest";
    private static final String BODY_ITEM = "aquatech_ui:deep_chest_model";
    private static final String LID_ITEM = "aquatech_ui:deep_chest_lid";
    private static final long FIRST_DELAY_MS = 20L * 60_000L;
    private static final long RETRY_NO_PLACE_MS = 2L * 60_000L;
    private static final int BEAM_HEIGHT = 70;
    private static final double CHEST_GAP = 60.0;
    private static final double PARTICLE_RANGE = 260.0;
    private static final double NEAR_DISTANCE = 12.0;
    /** Ближе этого расстояния сундук «оживает» на полную; дальше FAR он ведёт себя спокойно. */
    private static final double ALIVE_NEAR = 5.0;
    private static final double ALIVE_FAR = 15.0;
    /** Модель сундука 32 юнита в ширину: масштаб 0.7 даёт ~1.4 блока. */
    private static final float MODEL_SCALE = 1.1f;
    /** Модель нарисована лицом на +Z, а не на -Z: без 180 сундук спавнится спиной к игроку (проверено в игре). */
    private static final float MODEL_YAW_OFFSET = 180.0f;
    private static final double WATER_LEVEL_OFFSET = 0.85;
    private static final int IDLE_STEP_TICKS = 5;
    private static final int RATTLE_STEP_TICKS = 2;
    private static final int[] FIREWORK_COLORS = {0xFFC93C, 0xFF7A1A, 0x3BE6E0, 0xFFFFFF};

    private enum Phase {ACTIVE, WON, FIZZLING}

    private static final class Chest {
        final UUID owner;
        final ServerLevel level;
        final BlockPos surface;
        final long spawnedAt;
        final long endsAt;
        final long totalMs;
        final UUID bodyId;
        final UUID lidId;
        final UUID hitboxId;
        final ServerBossEvent bar;
        Phase phase = Phase.ACTIVE;
        long phaseStart;
        double lidAtPhaseStart;
        int lastRattleWindow = -1;
        int lastAnimTick = DeepChestAnimation.NEVER_ANIMATED;
        int fireworksShot;

        Chest(UUID owner, ServerLevel level, BlockPos surface, long now, long totalMs, UUID bodyId, UUID lidId,
              UUID hitboxId, ServerBossEvent bar) {
            this.owner = owner;
            this.level = level;
            this.surface = surface;
            this.spawnedAt = now;
            this.endsAt = now + totalMs;
            this.totalMs = totalMs;
            this.bodyId = bodyId;
            this.lidId = lidId;
            this.hitboxId = hitboxId;
            this.bar = bar;
        }
    }

    /** Активные сундуки по владельцам. Трогаем только из серверного потока. */
    private static final Map<UUID, Chest> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> NEXT_AT = new HashMap<>();

    private DeepChestService() {
    }

    // ─────────────────────────── управление ───────────────────────────

    /** Для команды /aquatech deepchest: запускает событие сразу. {@code only} null = всем подходящим игрокам. */
    public static boolean startNow(MinecraftServer server, ServerPlayer only) {
        long now = System.currentTimeMillis();
        boolean started = false;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (only != null && !player.getUUID().equals(only.getUUID())) continue;
            if (!isEligible(player) || ACTIVE.containsKey(player.getUUID())) continue;
            started |= begin(server, player, now);
        }
        return started;
    }

    public static boolean stopNow(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return false;
        long now = System.currentTimeMillis();
        for (Chest chest : ACTIVE.values()) {
            cleanup(chest);
            NEXT_AT.put(chest.owner, now + nextDelay());
        }
        ACTIVE.clear();
        return true;
    }

    public static String statusLine() {
        if (ACTIVE.isEmpty()) {
            return "§7Личных сокровищ сейчас нет. Новые появятся по расписанию у каждого игрока отдельно.";
        }
        StringBuilder out = new StringBuilder("§6Личных сокровищ активно: §f" + ACTIVE.size());
        for (Chest chest : ACTIVE.values()) {
            String phase = chest.phase == Phase.ACTIVE
                    ? "осталось " + DeepChestLogic.timeLeft(chest.endsAt - System.currentTimeMillis())
                    : "финал анимации";
            out.append("\n§7- §f").append(chest.surface.getX()).append(' ').append(chest.surface.getZ())
                    .append(" §7(").append(phase).append(')');
        }
        return out.toString();
    }

    private static boolean isEligible(ServerPlayer player) {
        return !player.isSpectator() && player.level().dimension().equals(Level.OVERWORLD);
    }

    // ─────────────────────────── тик ───────────────────────────

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        int tick = server.getTickCount();
        long now = System.currentTimeMillis();
        for (Iterator<Chest> it = ACTIVE.values().iterator(); it.hasNext(); ) {
            Chest chest = it.next();
            animate(chest, now, tick);
            if (chest.phase != Phase.ACTIVE) {
                if (finishIfDone(chest, now)) it.remove();
                continue;
            }
            if (tick % 10 == 0) tickActive(server, chest, now, tick % 20 == 0);
        }
        if (tick % 10 != 0 || !ModConfig.DEEP_CHEST_ENABLED.get()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!isEligible(player) || ACTIVE.containsKey(player.getUUID())) continue;
            long next = NEXT_AT.computeIfAbsent(player.getUUID(), id -> now + FIRST_DELAY_MS);
            if (now < next) continue;
            if (PersonalSpotFinder.isNearOwnRaft(player.serverLevel(), player, PersonalSpotFinder.AT_HOME_BLOCKS)) {
                begin(server, player, now);
            } else {
                NEXT_AT.put(player.getUUID(), now + PersonalSpotFinder.AWAY_RETRY_MS);
            }
        }
    }

    private static void tickActive(MinecraftServer server, Chest chest, long now, boolean everySecond) {
        long left = chest.endsAt - now;
        if (left <= 0L) {
            startFizzle(server, chest, now);
            return;
        }
        ServerPlayer owner = server.getPlayerList().getPlayer(chest.owner);
        if (owner == null) return;
        if (!owner.level().dimension().equals(Level.OVERWORLD)) {
            chest.bar.removePlayer(owner);
            return;
        }
        double cx = chest.surface.getX() + 0.5;
        double cz = chest.surface.getZ() + 0.5;
        double baseY = chest.surface.getY() + 1.0;
        if (owner.distanceToSqr(cx, baseY, cz) <= PARTICLE_RANGE * PARTICLE_RANGE) {
            for (int dy = 0; dy <= BEAM_HEIGHT; dy += 2) {
                chest.level.sendParticles(owner, ParticleTypes.END_ROD, true, cx, baseY + dy, cz, 1, 0.05, 0.0, 0.05, 0.0);
            }
            chest.level.sendParticles(owner, ParticleTypes.FIREWORK, true, cx, baseY + 0.6, cz, 6, 0.5, 0.3, 0.5, 0.05);
            chest.level.sendParticles(owner, ParticleTypes.SPLASH, true, cx, baseY, cz, 12, 1.2, 0.1, 1.2, 0.1);
        }
        if (!everySecond) return;
        chest.bar.setProgress(DeepChestLogic.progress(left, chest.totalMs));
        chest.bar.setName(Component.literal("§6Твоё сокровище из глубин §7— осталось §e" + DeepChestLogic.timeLeft(left)));
        chest.bar.addPlayer(owner);
        double dx = cx - owner.getX();
        double dz = cz - owner.getZ();
        String text = Math.sqrt(dx * dx + dz * dz) <= NEAR_DISTANCE
                ? "§a§lТы у сундука! §fЖми по нему!"
                : "§6Сокровище: §e" + DeepChestLogic.distance(dx, dz) + " м §f" + DeepChestLogic.compass(dx, dz);
        owner.displayClientMessage(Component.literal(text), true);
    }

    /** Ушёл владелец: сундук ждать некому, убираем сразу, следующий придёт по обычному расписанию. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Chest chest = ACTIVE.remove(player.getUUID());
        NEXT_AT.put(player.getUUID(), System.currentTimeMillis() + nextDelay());
        if (chest != null) cleanup(chest);
    }

    // ─────────────────────────── анимация ───────────────────────────

    /** 0 далеко, 1 вплотную: насколько близко к сундуку его владелец. */
    private static double nearFactor(Chest chest) {
        ServerPlayer owner = chest.level.getServer().getPlayerList().getPlayer(chest.owner);
        if (owner == null || owner.isSpectator() || owner.level() != chest.level) return 0.0;
        double dx = owner.getX() - (chest.surface.getX() + 0.5);
        double dz = owner.getZ() - (chest.surface.getZ() + 0.5);
        double distance = Math.sqrt(dx * dx + dz * dz);
        return Math.max(0.0, Math.min(1.0, (ALIVE_FAR - distance) / (ALIVE_FAR - ALIVE_NEAR)));
    }

    private static void animate(Chest chest, long now, int tick) {
        long age = now - chest.spawnedAt;
        double near = chest.phase == Phase.ACTIVE ? nearFactor(chest) : 0.0;
        boolean rattling = chest.phase == Phase.ACTIVE && DeepChestAnimation.rattleDeg(age, near) != 0.0;
        int step = rattling ? RATTLE_STEP_TICKS : IDLE_STEP_TICKS;
        if (!DeepChestAnimation.stepDue(tick, chest.lastAnimTick, step)) return;
        chest.lastAnimTick = tick;

        double sink = 0.0;
        double lid;
        long sincePhase = now - chest.phaseStart;
        switch (chest.phase) {
            case WON -> {
                sink = DeepChestAnimation.winSink(sincePhase);
                lid = DeepChestAnimation.winLidDeg(sincePhase);
                fireworks(chest, sincePhase);
            }
            case FIZZLING -> {
                sink = DeepChestAnimation.fizzleSink(sincePhase);
                lid = DeepChestAnimation.fizzleLidDeg(sincePhase, chest.lidAtPhaseStart);
            }
            default -> {
                lid = DeepChestAnimation.lidAngleDeg(age, near) + DeepChestAnimation.rattleDeg(age, near);
                rattleEffects(chest, age, near);
            }
        }
        DeepChestAnimation.Pose body = DeepChestAnimation.bodyPose(age, MODEL_SCALE, sink);
        DeepChestAnimation.Pose lidPose = DeepChestAnimation.lidPose(body.translation(), body.rotation(), body.scale(), lid);
        int duration = step + 1;
        applyPose(chest.level.getEntity(chest.bodyId), body, duration);
        applyPose(chest.level.getEntity(chest.lidId), lidPose, duration);
        if (chest.phase == Phase.ACTIVE) chest.lidAtPhaseStart = lid;
    }

    /** На старте каждого окна дрожи: стук, золотые искры и монеты из щели. */
    private static void rattleEffects(Chest chest, long age, double near) {
        int window = DeepChestAnimation.rattleWindowIndex(age, near);
        if (window < 0 || window == chest.lastRattleWindow) return;
        chest.lastRattleWindow = window;
        double x = chest.surface.getX() + 0.5;
        double y = chest.surface.getY() + WATER_LEVEL_OFFSET + 0.9;
        double z = chest.surface.getZ() + 0.5;
        chest.level.playSound(null, x, y, z, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.9F, 0.7F);
        chest.level.playSound(null, x, y, z, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.7F, 0.8F);
        chest.level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.GOLD_NUGGET)),
                x, y, z, 14, 0.5, 0.15, 0.5, 0.12);
        chest.level.sendParticles(ParticleTypes.END_ROD, x, y, z, 10, 0.4, 0.3, 0.4, 0.04);
    }

    /** Три залпа: сразу, через 0.35 и через 0.7 секунды после победы. */
    private static void fireworks(Chest chest, long sincePhase) {
        long[] at = {0L, 350L, 700L};
        while (chest.fireworksShot < at.length && sincePhase >= at[chest.fireworksShot]) {
            double x = chest.surface.getX() + 0.5 + (chest.fireworksShot - 1) * 0.9;
            double y = chest.surface.getY() + WATER_LEVEL_OFFSET + 1.4;
            double z = chest.surface.getZ() + 0.5;
            burst(chest.level, x, y, z, FIREWORK_COLORS[chest.fireworksShot % FIREWORK_COLORS.length]);
            chest.fireworksShot++;
        }
    }

    private static void burst(ServerLevel level, double x, double y, double z, int color) {
        CompoundTag explosion = new CompoundTag();
        explosion.putByte("Type", (byte) 4);
        explosion.putIntArray("Colors", new int[]{color, 0xFFC93C});
        explosion.putIntArray("FadeColors", new int[]{0xFFFFFF});
        explosion.putByte("Trail", (byte) 1);
        explosion.putByte("Flicker", (byte) 1);
        ListTag explosions = new ListTag();
        explosions.add(explosion);
        CompoundTag fireworks = new CompoundTag();
        fireworks.put("Explosions", explosions);
        fireworks.putByte("Flight", (byte) 1);
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        stack.getOrCreateTag().put("Fireworks", fireworks);
        FireworkRocketEntity rocket = new FireworkRocketEntity(level, x, y, z, stack);
        level.addFreshEntity(rocket);
        level.broadcastEntityEvent(rocket, (byte) 17);
        rocket.discard();
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.GOLD_NUGGET)),
                x, y - 0.4, z, 30, 0.8, 0.5, 0.8, 0.25);
    }

    /** Новая поза уходит на клиент с интерполяцией: он сам плавно доводит дисплей до неё. */
    private static void applyPose(Entity entity, DeepChestAnimation.Pose pose, int durationTicks) {
        if (!(entity instanceof Display display)) return;
        CompoundTag tag = new CompoundTag();
        display.saveWithoutId(tag);
        tag.put("transformation", transformationOf(pose));
        tag.putInt("interpolation_duration", durationTicks);
        tag.putInt("start_interpolation", 0);
        display.load(tag);
    }

    private static CompoundTag transformationOf(DeepChestAnimation.Pose pose) {
        CompoundTag transformation = new CompoundTag();
        double[] t = pose.translation();
        double[] q = pose.rotation();
        float s = (float) pose.scale();
        transformation.put("translation", floats((float) t[0], (float) t[1], (float) t[2]));
        transformation.put("scale", floats(s, s, s));
        transformation.put("left_rotation", floats((float) q[0], (float) q[1], (float) q[2], (float) q[3]));
        transformation.put("right_rotation", floats(0.0f, 0.0f, 0.0f, 1.0f));
        return transformation;
    }

    // ─────────────────────────── старт ───────────────────────────

    private static boolean begin(MinecraftServer server, ServerPlayer owner, long now) {
        ServerLevel level = owner.serverLevel();
        BlockPos surface = findPlace(level, owner);
        if (surface == null) {
            NEXT_AT.put(owner.getUUID(), now + RETRY_NO_PLACE_MS);
            return false;
        }
        Display.ItemDisplay body = EntityType.ITEM_DISPLAY.create(level);
        Display.ItemDisplay lid = EntityType.ITEM_DISPLAY.create(level);
        Interaction hitbox = EntityType.INTERACTION.create(level);
        if (body == null || lid == null || hitbox == null) {
            NEXT_AT.put(owner.getUUID(), now + RETRY_NO_PLACE_MS);
            return false;
        }
        double x = surface.getX() + 0.5;
        double y = surface.getY() + WATER_LEVEL_OFFSET;
        double z = surface.getZ() + 0.5;
        float yaw = (float) Math.toDegrees(Math.atan2(-(owner.getX() - x), owner.getZ() - z)) + MODEL_YAW_OFFSET;
        DeepChestAnimation.Pose start = DeepChestAnimation.bodyPose(0L, MODEL_SCALE, 0.0);
        configureDisplay(body, BODY_ITEM, start);
        configureDisplay(lid, LID_ITEM, DeepChestAnimation.lidPose(start.translation(), start.rotation(), start.scale(), 0.0));
        configureHitbox(hitbox);
        body.moveTo(x, y, z, yaw, 0.0f);
        lid.moveTo(x, y, z, yaw, 0.0f);
        hitbox.moveTo(x, y, z);
        body.addTag(ENTITY_TAG);
        lid.addTag(ENTITY_TAG);
        hitbox.addTag(ENTITY_TAG);

        long totalMs = ModConfig.DEEP_CHEST_DURATION_MINUTES.get() * 60_000L;
        ServerBossEvent bar = new ServerBossEvent(Component.literal("§6Твоё сокровище из глубин"),
                BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        ACTIVE.put(owner.getUUID(), new Chest(owner.getUUID(), level, surface, now, totalMs, body.getUUID(), lid.getUUID(),
                hitbox.getUUID(), bar));
        level.addFreshEntity(body);
        level.addFreshEntity(lid);
        level.addFreshEntity(hitbox);
        level.sendParticles(ParticleTypes.SPLASH, x, y + 0.3, z, 120, 1.6, 0.4, 1.6, 0.4);
        level.sendParticles(ParticleTypes.BUBBLE, x, y - 0.4, z, 60, 1.2, 0.6, 1.2, 0.15);
        level.playSound(null, x, y, z, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.4F, 0.6F);
        strike(level, x, y, z);

        owner.sendSystemMessage(Component.literal("§6§l[Сокровище глубин] §eУ твоего плота поднялся личный пиратский сундук! "
                + "Забрать его можешь только ты: нажми по нему и получи §65 000–10 000 монет §fи с шансом §bключ кейса§f. "
                + "§7У тебя " + DeepChestLogic.timeLeft(totalMs) + ": следи за полосой вверху и подсказкой над хотбаром."));
        bar.addPlayer(owner);
        owner.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
        owner.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§6Сокровище из глубин")));
        owner.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§eТвой личный сундук ждёт!")));
        owner.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    /** Вокруг плота владельца; сундуки разных игроков держим подальше друг от друга. */
    private static BlockPos findPlace(ServerLevel level, ServerPlayer owner) {
        return PersonalSpotFinder.find(level, owner, ModConfig.DEEP_CHEST_MIN_DISTANCE.get(),
                ModConfig.DEEP_CHEST_MAX_DISTANCE.get(), surface -> !nearOtherChest(surface));
    }

    private static boolean nearOtherChest(BlockPos candidate) {
        for (Chest other : ACTIVE.values()) {
            if (other.surface.distSqr(candidate) < CHEST_GAP * CHEST_GAP) return true;
        }
        return false;
    }

    /** Данные дисплея задаются через NBT: публичного API для item и transformation в 1.20.1 нет. */
    private static void configureDisplay(Display.ItemDisplay display, String itemId, DeepChestAnimation.Pose pose) {
        CompoundTag tag = new CompoundTag();
        display.saveWithoutId(tag);
        CompoundTag item = new CompoundTag();
        item.putString("id", itemId);
        item.putByte("Count", (byte) 1);
        tag.put("item", item);
        tag.putString("item_display", "none");
        tag.putBoolean("Glowing", true);
        tag.putInt("glow_color_override", 0xFFC93C);
        CompoundTag brightness = new CompoundTag();
        brightness.putInt("sky", 15);
        brightness.putInt("block", 15);
        tag.put("brightness", brightness);
        tag.put("transformation", transformationOf(pose));
        display.load(tag);
    }

    private static void configureHitbox(Interaction hitbox) {
        CompoundTag tag = new CompoundTag();
        hitbox.saveWithoutId(tag);
        tag.putFloat("width", 2.6f);
        tag.putFloat("height", 1.8f);
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

    private static Chest byHitbox(UUID hitboxId) {
        for (Chest chest : ACTIVE.values()) {
            if (chest.hitboxId.equals(hitboxId)) return chest;
        }
        return null;
    }

    /** true, если цель это хитбокс сундука (событие поглощаем, даже когда награду не отдали). */
    private static boolean tryClaim(Entity target, net.minecraft.world.entity.player.Player who) {
        if (!(target instanceof Interaction)) return false;
        Chest chest = byHitbox(target.getUUID());
        if (chest == null || chest.phase != Phase.ACTIVE) return false;
        if (!(who instanceof ServerPlayer player)) return true;
        MinecraftServer server = player.getServer();
        if (server == null) return true;
        if (!player.getUUID().equals(chest.owner)) {
            player.displayClientMessage(Component.literal("§cЭто личное сокровище другого игрока."), true);
            return true;
        }
        DeepChestLogic.Reward reward = DeepChestLogic.rollReward(new Random());
        OceanEventsService.grantCoins(player, reward.coins());
        if (reward.caseId() != null) OceanEventsService.grantCaseKey(player, reward.caseId());
        if (DeepChestLogic.rollBooster(new Random())) OceanEventsService.grantBooster(player, "small");

        ServerLevel level = chest.level;
        double x = chest.surface.getX() + 0.5;
        double y = chest.surface.getY() + 1.5;
        double z = chest.surface.getZ() + 0.5;
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 140, 1.4, 1.6, 1.4, 0.5);
        level.playSound(null, chest.surface, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, chest.surface, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.2F, 0.6F);
        strike(level, x, y, z);

        player.sendSystemMessage(Component.literal("§6[Сокровище глубин] §aТвоя награда: §6+" + reward.coins()
                + " монет" + (reward.caseId() == null ? "" : " §aи §bключ Кейса " + TournamentLogic.caseNumeral(reward.caseId())) + "§a!"));

        chest.bar.removeAllPlayers();
        Entity hitbox = level.getEntity(chest.hitboxId);
        if (hitbox != null) hitbox.discard();
        chest.phase = Phase.WON;
        chest.phaseStart = System.currentTimeMillis();
        chest.lastAnimTick = DeepChestAnimation.NEVER_ANIMATED;
        NEXT_AT.put(chest.owner, chest.phaseStart + DeepChestAnimation.WON_MS + nextDelay());
        return true;
    }

    private static void startFizzle(MinecraftServer server, Chest chest, long now) {
        chest.level.sendParticles(ParticleTypes.SPLASH, chest.surface.getX() + 0.5, chest.surface.getY() + 1.0,
                chest.surface.getZ() + 0.5, 80, 1.4, 0.4, 1.4, 0.3);
        chest.level.playSound(null, chest.surface, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.3F, 0.7F);
        ServerPlayer owner = server.getPlayerList().getPlayer(chest.owner);
        if (owner != null) {
            owner.sendSystemMessage(Component.literal("§6[Сокровище глубин] §7Не успел: сундук ушёл на дно. Следующий поднимется позже."));
        }
        chest.bar.removeAllPlayers();
        Entity hitbox = chest.level.getEntity(chest.hitboxId);
        if (hitbox != null) hitbox.discard();
        chest.phase = Phase.FIZZLING;
        chest.phaseStart = now;
        chest.lastAnimTick = DeepChestAnimation.NEVER_ANIMATED;
        NEXT_AT.put(chest.owner, now + DeepChestAnimation.FIZZLE_MS + nextDelay());
    }

    /** Финальная анимация досмотрена: убираем сущности. true = сундук можно выкидывать из списка. */
    private static boolean finishIfDone(Chest chest, long now) {
        long limit = chest.phase == Phase.WON ? DeepChestAnimation.WON_MS : DeepChestAnimation.FIZZLE_MS;
        if (now - chest.phaseStart < limit) return false;
        cleanup(chest);
        return true;
    }

    private static void cleanup(Chest chest) {
        chest.bar.removeAllPlayers();
        for (UUID id : new UUID[]{chest.bodyId, chest.lidId, chest.hitboxId}) {
            Entity entity = chest.level.getEntity(id);
            if (entity != null) entity.discard();
        }
    }

    /** Сущности сундука сохраняются в чанках: после рестарта или выгрузки чанка лишние удаляем. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide() || !entity.getTags().contains(ENTITY_TAG)) return;
        UUID id = entity.getUUID();
        boolean known = false;
        for (Chest chest : ACTIVE.values()) {
            if (id.equals(chest.bodyId) || id.equals(chest.lidId) || id.equals(chest.hitboxId)) {
                known = true;
                break;
            }
        }
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
