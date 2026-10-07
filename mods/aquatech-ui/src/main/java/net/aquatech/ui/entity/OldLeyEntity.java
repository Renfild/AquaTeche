package net.aquatech.ui.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
 * Старый Лей в воде: пока идёт событие, кружит по окружности вокруг точки появления. Только зрелище: ударить его или
 * упереться в него нельзя, поймать его можно только удочкой (см. {@code LeyService}). После поимки делает прыжок
 * и исчезает. На диск не сохраняется.
 */
public class OldLeyEntity extends PathfinderMob implements GeoEntity {

    /** Id игрока, который держит Лея на леске, или 0: по нему клиенты рисуют леску. */
    private static final EntityDataAccessor<Integer> HOOKED_BY = SynchedEntityData.defineId(OldLeyEntity.class, EntityDataSerializers.INT);

    private static final String CTRL = "ley";
    private static final String TRIG_LEAP = "leap";
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop(OldLeyAnims.SWIM);
    private static final RawAnimation LEAP = RawAnimation.begin().thenPlay(OldLeyAnims.LEAP);

    /** Радиус кружения в блоках и время одного оборота в тиках. */
    private static final double CIRCLE_RADIUS = 5.0D;
    private static final int CIRCLE_TICKS = 900;
    private static final int LEAP_TICKS = 50;

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private double centerX;
    private double centerY;
    private double centerZ;
    private int phase;
    private int leapTicksLeft = -1;
    /** Пока идёт схватка, место Лея задаёт сервис схватки, а не окружность. */
    private boolean fightControlled;

    public OldLeyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoAi(true);
        setNoGravity(true);
        setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HOOKED_BY, 0);
    }

    public int hookedBy() {
        return entityData.get(HOOKED_BY);
    }

    /** Лей на крючке: двигает его сервис схватки. */
    public void hook(int playerId) {
        fightControlled = true;
        entityData.set(HOOKED_BY, playerId);
    }

    /** Ставит Лея в точку схватки и поворачивает мордой по ходу рывка. */
    public void controlAt(double x, double y, double z, float yaw) {
        setPos(x, y, z);
        setYRot(yaw);
        setYBodyRot(yaw);
        setYHeadRot(yaw);
        yRotO = yaw;
    }

    /** Леска порвалась или игрок бросил схватку: Лей продолжает кружить с того места, где он сейчас. */
    public void release() {
        fightControlled = false;
        entityData.set(HOOKED_BY, 0);
        double angle = phase * (2.0D * Math.PI / CIRCLE_TICKS);
        centerX = getX() - CIRCLE_RADIUS * Math.cos(angle);
        centerZ = getZ() - CIRCLE_RADIUS * Math.sin(angle);
        centerY = getY();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    /** Центр кружения; entity ставится на окружность, чтобы не прыгать при первом тике. */
    public void anchorAt(double x, double y, double z) {
        centerX = x;
        centerY = y;
        centerZ = z;
        moveAlongCircle();
    }

    /** Прыжок на прощание: через 2.5 с сущность исчезает. */
    public void leapAndLeave() {
        if (leapTicksLeft < 0) {
            leapTicksLeft = LEAP_TICKS;
            triggerAnim(CTRL, TRIG_LEAP);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (leapTicksLeft >= 0) {
            if (--leapTicksLeft == 0) {
                discard();
            }
            return;
        }
        if (fightControlled) {
            return;
        }
        phase = (phase + 1) % CIRCLE_TICKS;
        moveAlongCircle();
    }

    /** Идёт по окружности, но только там, где вода: у берега и в маленьком пруду радиус сужается, иначе рыба плыла бы по суше. */
    private void moveAlongCircle() {
        double angle = phase * (2.0D * Math.PI / CIRCLE_TICKS);
        double vx = -Math.sin(angle);
        double vz = Math.cos(angle);
        double x = centerX;
        double z = centerZ;
        for (double radius = CIRCLE_RADIUS; radius > 0.0D; radius -= 1.0D) {
            double tx = centerX + radius * Math.cos(angle);
            double tz = centerZ + radius * Math.sin(angle);
            if (isWater(tx, tz)) {
                x = tx;
                z = tz;
                break;
            }
        }
        float yaw = (float) (Mth.atan2(-vx, vz) * Mth.RAD_TO_DEG);
        setPos(x, centerY, z);
        setYRot(yaw);
        setYBodyRot(yaw);
        setYHeadRot(yaw);
        yRotO = yaw;
    }

    private boolean isWater(double x, double z) {
        return level().getFluidState(BlockPos.containing(x, centerY, z)).is(FluidTags.WATER);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /** Модель длиной около 3 блоков в кружащем движении, поэтому для отсечения по кадру берём коробку пошире. */
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(4.0D, 3.0D, 4.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CTRL, 5, state -> state.setAndContinue(SWIM))
                .triggerableAnim(TRIG_LEAP, LEAP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }
}
