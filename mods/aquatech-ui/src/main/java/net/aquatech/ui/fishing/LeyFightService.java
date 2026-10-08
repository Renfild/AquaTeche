package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.OldLeyEntity;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.S2CLeyFightPacket;
import net.aquatech.ui.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.UUID;

/**
 * Схватка со Старым Леем на Удочке Лея: заброс, поклёвка, борьба за натяжение (правила в {@link LeyFight}).
 * Одновременно лесу держит один игрок. Сервер хозяин всего: клиент шлёт только «мотаю / тяну влево или вправо»,
 * положение Лея и исход считает сервис. Работает в основном потоке сервера.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID)
public final class LeyFightService {

    private static final double MAX_CAST_DISTANCE = 48.0D;
    private static final double MAX_FIGHT_DISTANCE = 70.0D;
    private static final int BITE_WAIT_MIN_TICKS = 40;
    private static final int BITE_WAIT_EXTRA_TICKS = 60;
    private static final int COOLDOWN_TICKS = 100;
    private static final int ROD_DAMAGE_ON_SNAP = 25;
    /** Если клиент молчит дольше, считаем, что игрок отпустил всё. */
    private static final int INPUT_TIMEOUT_TICKS = 10;
    /** Поворот Лея в сторону на рывке, рад за тик, и предел отклонения от исходного направления. */
    private static final double SWEEP_PER_TICK = 0.012D;
    private static final double SWEEP_LIMIT = 0.9D;

    private static Fight current;
    private static final java.util.Map<UUID, Long> COOLDOWN_UNTIL = new java.util.HashMap<>();

    private static final class Fight {
        final UUID player;
        final Random rng = new Random();
        final LeyFight.State state;
        int biteWait = BITE_WAIT_MIN_TICKS + rng.nextInt(BITE_WAIT_EXTRA_TICKS);
        boolean biting;
        boolean reeling;
        int steer;
        int lastInputTick;
        double bearing;
        double sweep;
        double lastX;
        double lastY;
        double lastZ;
        LeyFight.Phase lastPhase = LeyFight.Phase.CALM;

        Fight(UUID player) {
            this.player = player;
            this.state = LeyFight.start(rng);
        }
    }

    private LeyFightService() {
    }

    public static boolean isFighting(UUID player) {
        return current != null && current.player.equals(player);
    }

    /** ПКМ удочкой Лея: забросить леску в сторону Лея. */
    public static void tryStart(ServerPlayer player) {
        MinecraftServer server = player.server;
        if (current != null) {
            if (!current.player.equals(player.getUUID())) {
                ServerPlayer other = server.getPlayerList().getPlayer(current.player);
                say(player, "§6[Старый Лей] §7Его уже держит " + (other == null ? "другой игрок" : other.getGameProfile().getName()) + ". Подождите.");
            }
            return;
        }
        long now = player.level().getGameTime();
        Long until = COOLDOWN_UNTIL.get(player.getUUID());
        if (until != null && now < until) {
            say(player, "§6[Старый Лей] §7Леска ещё мокрая, смотайте её и закиньте через пару секунд.");
            return;
        }
        OldLeyEntity ley = LeyService.activeLey(server);
        if (ley == null) {
            say(player, "§6[Старый Лей] §7Сейчас Лея нет. Он поднимается раз в неделю, объявление придёт в чат.");
            return;
        }
        if (ley.level() != player.level()) {
            say(player, "§6[Старый Лей] §7Он в другом мире.");
            return;
        }
        double dx = ley.getX() - player.getX();
        double dz = ley.getZ() - player.getZ();
        double flat = Math.sqrt(dx * dx + dz * dz);
        if (flat > MAX_CAST_DISTANCE) {
            say(player, "§6[Старый Лей] §7Лей слишком далеко: подойдите ближе, чем на " + (int) MAX_CAST_DISTANCE + " блоков (сейчас " + (int) flat + ").");
            return;
        }
        Fight fight = new Fight(player.getUUID());
        fight.bearing = Math.atan2(dz, dx);
        fight.state.distance = Mth.clamp(flat, 12.0D, 30.0D);
        fight.lastX = ley.getX();
        fight.lastY = ley.getY();
        fight.lastZ = ley.getZ();
        fight.lastInputTick = server.getTickCount();
        current = fight;
        ley.hook(player.getId());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW,
                SoundSource.PLAYERS, 0.8F, 0.6F);
        sendState(player, S2CLeyFightPacket.MODE_WAITING);
        say(player, "§6[Старый Лей] §7Леска ушла в воду. Ждите поклёвку…");
    }

    /** Ввод игрока: мотает ли, куда тянет (-1 влево, 1 вправо). */
    public static void input(ServerPlayer player, boolean reeling, int steer) {
        if (current != null && current.player.equals(player.getUUID())) {
            current.reeling = reeling;
            current.steer = steer;
            current.lastInputTick = player.server.getTickCount();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || current == null) {
            return;
        }
        MinecraftServer server = event.getServer();
        Fight fight = current;
        ServerPlayer player = server.getPlayerList().getPlayer(fight.player);
        OldLeyEntity ley = LeyService.activeLey(server);
        if (player == null || !player.isAlive() || player.isSpectator()) {
            abort(player, ley, null);
            return;
        }
        if (ley == null) {
            abort(player, null, "§6[Старый Лей] §7Лей ушёл на глубину.");
            return;
        }
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.LEY_ROD.get())) {
            abort(player, ley, "§6[Старый Лей] §7Вы выпустили удочку, и Лей сорвался.");
            return;
        }
        if (ley.level() != player.level() || flatDistance(player, ley) > MAX_FIGHT_DISTANCE) {
            abort(player, ley, "§6[Старый Лей] §7Лей утащил леску слишком далеко.");
            return;
        }
        if (!fight.biting) {
            waitForBite(fight, player, ley);
            return;
        }
        if (server.getTickCount() - fight.lastInputTick > INPUT_TIMEOUT_TICKS) {
            fight.reeling = false;
            fight.steer = 0;
        }
        LeyFight.step(fight.state, fight.reeling, fight.steer, fight.rng);
        effects(fight, player, ley);
        placeLey(fight, player, ley);
        if (server.getTickCount() % 2 == 0) {
            sendState(player, S2CLeyFightPacket.MODE_FIGHT);
        }
        switch (fight.state.result) {
            case WON -> {
                end(player, ley);
                LeyService.award(player);
            }
            case SNAPPED -> {
                end(player, ley);
                damageRod(player);
                say(player, "§6[Старый Лей] §cЛеска лопнула! §7Натяжение вышло за красную черту.");
                cooldown(player);
            }
            case ESCAPED -> {
                end(player, ley);
                say(player, "§6[Старый Лей] §cЛей ушёл слишком далеко и сорвался. §7Мотайте, пока он спокоен.");
                cooldown(player);
            }
            default -> {
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (current != null && current.player.equals(event.getEntity().getUUID())) {
            OldLeyEntity ley = event.getEntity() instanceof ServerPlayer sp ? LeyService.activeLey(sp.server) : null;
            if (ley != null) {
                ley.release();
            }
            current = null;
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        current = null;
        COOLDOWN_UNTIL.clear();
    }

    private static void waitForBite(Fight fight, ServerPlayer player, OldLeyEntity ley) {
        if (--fight.biteWait > 0) {
            if (fight.biteWait % 8 == 0 && ley.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.BUBBLE, ley.getX(), ley.getY() + 0.6, ley.getZ(), 6, 0.5, 0.2, 0.5, 0.02);
            }
            return;
        }
        fight.biting = true;
        fight.lastInputTick = player.server.getTickCount();
        player.connection.send(new ClientboundSetTitlesAnimationPacket(2, 30, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§6§lЛЕЙ КЛЮНУЛ!")));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§eДержите ПКМ, пока леска в зелёной зоне")));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_SPLASH,
                SoundSource.PLAYERS, 1.2F, 0.5F);
        sendState(player, S2CLeyFightPacket.MODE_FIGHT);
    }

    /** Предупреждение о рывке всплеском и звуком, на самом рывке брызги и вода кипит. */
    private static void effects(Fight fight, ServerPlayer player, OldLeyEntity ley) {
        LeyFight.Phase phase = fight.state.phase;
        if (phase != fight.lastPhase) {
            if (phase == LeyFight.Phase.WARN) {
                player.level().playSound(null, ley.getX(), ley.getY(), ley.getZ(), SoundEvents.GENERIC_SPLASH,
                        SoundSource.NEUTRAL, 1.4F, 0.6F);
            }
            fight.lastPhase = phase;
        }
        if (phase != LeyFight.Phase.CALM && ley.level() instanceof ServerLevel level && ley.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.SPLASH, ley.getX(), ley.getY() + 0.7, ley.getZ(), 14, 1.0, 0.2, 1.0, 0.1);
        }
    }

    /** Ставит Лея на воду на нужном расстоянии; на рывке разворачивает его в сторону рывка. */
    private static void placeLey(Fight fight, ServerPlayer player, OldLeyEntity ley) {
        LeyFight.State s = fight.state;
        if (s.phase == LeyFight.Phase.SURGE) {
            boolean countered = fight.steer == -s.surgeDir;
            fight.sweep += s.surgeDir * (countered ? SWEEP_PER_TICK * 0.3D : SWEEP_PER_TICK);
        } else {
            fight.sweep *= 0.97D;
        }
        fight.sweep = Mth.clamp(fight.sweep, -SWEEP_LIMIT, SWEEP_LIMIT);
        double angle = fight.bearing + fight.sweep;
        ServerLevel level = (ServerLevel) ley.level();
        for (double d = Mth.clamp(s.distance, 4.0D, LeyFight.ESCAPE_DISTANCE); d >= 4.0D; d -= 2.0D) {
            double x = player.getX() + Math.cos(angle) * d;
            double z = player.getZ() + Math.sin(angle) * d;
            BlockPos surface = waterSurface(level, x, z, Mth.floor(fight.lastY));
            if (surface != null) {
                fight.lastX = x;
                fight.lastY = surface.getY() + 0.44D;
                fight.lastZ = z;
                break;
            }
        }
        float yaw = (float) (Mth.atan2(-Math.cos(angle), Math.sin(angle)) * Mth.RAD_TO_DEG);
        ley.controlAt(fight.lastX, fight.lastY, fight.lastZ, yaw);
    }

    /** Расстояние по земле: игрок может стоять высоко над водой, высота на схватку не влияет. */
    private static double flatDistance(ServerPlayer player, OldLeyEntity ley) {
        double dx = ley.getX() - player.getX();
        double dz = ley.getZ() - player.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static BlockPos waterSurface(ServerLevel level, double x, double z, int fromY) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        if (!level.hasChunkAt(new BlockPos(bx, fromY, bz))) {
            return null;
        }
        for (int y = fromY + 6; y >= fromY - 16; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            if (level.getFluidState(p).is(FluidTags.WATER) && !level.getFluidState(p.above()).is(FluidTags.WATER)) {
                return p;
            }
        }
        return null;
    }

    private static void end(ServerPlayer player, OldLeyEntity ley) {
        ley.release();
        sendState(player, S2CLeyFightPacket.MODE_END);
        current = null;
    }

    private static void abort(ServerPlayer player, OldLeyEntity ley, String message) {
        AquaTechUI.LOGGER.info("[ley] схватка прервана: {}", message == null ? "игрок вышел или умер" : message);
        if (ley != null) {
            ley.release();
        }
        if (player != null) {
            sendState(player, S2CLeyFightPacket.MODE_END);
            if (message != null) {
                say(player, message);
            }
            cooldown(player);
        }
        current = null;
    }

    private static void cooldown(ServerPlayer player) {
        COOLDOWN_UNTIL.put(player.getUUID(), player.level().getGameTime() + COOLDOWN_TICKS);
    }

    private static void damageRod(ServerPlayer player) {
        ItemStack rod = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (rod.is(ModItems.LEY_ROD.get())) {
            rod.hurtAndBreak(ROD_DAMAGE_ON_SNAP, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
    }

    private static void sendState(ServerPlayer player, byte mode) {
        Fight f = current;
        LeyFight.State s = f == null ? null : f.state;
        NetworkHandler.sendToPlayerWhenReady(new S2CLeyFightPacket(mode,
                s == null ? 0F : (float) s.tension,
                s == null ? 0F : (float) s.stamina,
                (byte) (s == null ? 0 : s.phase.ordinal()),
                (byte) (s == null ? 0 : s.surgeDir)), player);
    }

    private static void say(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), true);
    }
}
