package net.aquatech.ui.entity;

import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.packet.C2SNeighborTalkPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Рыбак-сосед: сидит на краю пирса и ловит рыбу. Не торгует и заданий не даёт: по ПКМ рассказывает историю,
 * сам комментирует рекорды стены славы и тяжёлый улов рядом. Модель смотрит туда, куда повёрнута сущность.
 */
public class FishNeighborEntity extends PathfinderMob implements GeoEntity {

    public static final String CHAT_PREFIX = "§e[Рыбак] §f";

    private static final String BODY = "body";
    private static final String TRIG_FIDGET = "fidget";
    private static final String TRIG_TALK = "talk";
    private static final String TRIG_REACT = "react";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(FishNeighborAnims.IDLE);
    private static final RawAnimation FIDGET = RawAnimation.begin().thenPlay(FishNeighborAnims.IDLE_FIDGET);
    private static final RawAnimation REACT = RawAnimation.begin().thenPlay(FishNeighborAnims.REACT);
    private static final RawAnimation TALK = RawAnimation.begin().thenPlay(FishNeighborAnims.TALK);

    /** Самая длинная вставка (глоток) идёт 5 с; пауза между ними от 20 до 50 с. */
    private static final int MIN_FIDGET_GAP_TICKS = 400;
    private static final int EXTRA_FIDGET_GAP_TICKS = 600;
    private static final int TALK_COOLDOWN_TICKS = 50;
    private static final int COMMENT_COOLDOWN_TICKS = 100;
    private static final int LOOK_HOLD_TICKS = 100;
    private static final float MAX_HEAD_TURN = 60.0F;
    private static final double COMMENT_RANGE = 40.0D;
    /** На каком расстоянии сосед замечает подошедшего и как часто здоровается с одним и тем же игроком. */
    private static final double GREET_RANGE = 6.0D;
    private static final int GREET_CHECK_TICKS = 20;
    private static final long GREET_REPEAT_TICKS = 6000L;

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private int ticksToNextFidget = MIN_FIDGET_GAP_TICKS;
    private int ticksToLookReset;
    private int lastStory = -1;
    private long nextTalkAt;
    private long nextCommentAt;
    private final java.util.Map<java.util.UUID, Long> greeted = new java.util.HashMap<>();

    public FishNeighborEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoAi(true);
        setInvulnerable(true);
        setPersistenceRequired();
        setCustomName(Component.translatable("entity.aquatech_ui.fish_neighbor"));
        setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (--ticksToNextFidget <= 0) {
            ticksToNextFidget = MIN_FIDGET_GAP_TICKS + random.nextInt(EXTRA_FIDGET_GAP_TICKS);
            triggerAnim(BODY, TRIG_FIDGET);
        }
        if (ticksToLookReset > 0 && --ticksToLookReset == 0) {
            setYHeadRot(getYRot());
        }
        if (tickCount % GREET_CHECK_TICKS == 0) {
            greetNewcomers();
        }
    }

    /** Здоровается с игроком, который подошёл ближе 6 блоков: поворачивает голову и подсказывает про ПКМ. */
    private void greetNewcomers() {
        long now = level().getGameTime();
        for (Player nearby : level().players()) {
            if (!(nearby instanceof ServerPlayer player) || player.isSpectator() || distanceToSqr(player) > GREET_RANGE * GREET_RANGE) {
                continue;
            }
            Long last = greeted.get(player.getUUID());
            if (last != null && now - last < GREET_REPEAT_TICKS) {
                continue;
            }
            if (greeted.size() > 64) {
                greeted.clear();
            }
            greeted.put(player.getUUID(), now);
            lookAt(player);
            triggerAnim(BODY, TRIG_TALK);
            player.sendSystemMessage(Component.literal(CHAT_PREFIX
                    + FishNeighborLines.greeting(player.getGameProfile().getName(), random.nextInt(3))));
            return;
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide) {
            // сервер может не получить сам клик (защита региона), поэтому сообщаем о разговоре отдельным пакетом
            NetworkHandler.CHANNEL.sendToServer(new C2SNeighborTalkPacket(getId()));
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer target) {
            talkTo(target);
        }
        return InteractionResult.CONSUME;
    }

    /** Рассказывает игроку историю: голова к нему, анимация разговора, реплика в чат. Не чаще раза в 2.5 с. */
    public void talkTo(ServerPlayer target) {
        long now = level().getGameTime();
        if (now < nextTalkAt) {
            return;
        }
        nextTalkAt = now + TALK_COOLDOWN_TICKS;
        lookAt(target);
        triggerAnim(BODY, TRIG_TALK);
        lastStory = FishNeighborLines.pickStory(FishNeighborLines.STORIES.size(), lastStory, random::nextInt);
        target.sendSystemMessage(Component.literal(CHAT_PREFIX + FishNeighborLines.STORIES.get(lastStory)));
        ticksToNextFidget = Math.max(ticksToNextFidget, MIN_FIDGET_GAP_TICKS / 2);
    }

    /** Реакция на событие: анимация и реплика всем игрокам рядом. Слишком частые вызовы игнорируются. */
    public void comment(String line) {
        if (!(level() instanceof ServerLevel serverLevel) || line == null || line.isEmpty()) {
            return;
        }
        long now = serverLevel.getGameTime();
        if (now < nextCommentAt) {
            return;
        }
        nextCommentAt = now + COMMENT_COOLDOWN_TICKS;
        triggerAnim(BODY, TRIG_REACT);
        Component message = Component.literal(CHAT_PREFIX + line);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(this) <= COMMENT_RANGE * COMMENT_RANGE) {
                player.sendSystemMessage(message);
            }
        }
    }

    /** Поворачивает голову к игроку в пределах {@link #MAX_HEAD_TURN} от направления тела; через 5 с вернёт назад. */
    private void lookAt(ServerPlayer player) {
        double dx = player.getX() - getX();
        double dz = player.getZ() - getZ();
        float toPlayer = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float relative = Mth.clamp(Mth.wrapDegrees(toPlayer - getYRot()), -MAX_HEAD_TURN, MAX_HEAD_TURN);
        setYHeadRot(getYRot() + relative);
        ticksToLookReset = LOOK_HOLD_TICKS;
    }

    /** Тело не должно подворачиваться вслед за головой: сосед сидит лицом к воде. */
    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
            }
        };
    }

    /** Удочка и леска выходят далеко за хитбокс, поэтому для отсечения по кадру берём коробку пошире. */
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(3.5D, 2.5D, 3.5D);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, BODY, 6, state -> state.setAndContinue(IDLE))
                .triggerableAnim(TRIG_FIDGET, FIDGET)
                .triggerableAnim(TRIG_TALK, TALK)
                .triggerableAnim(TRIG_REACT, REACT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }
}
