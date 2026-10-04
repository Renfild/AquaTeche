package net.aquatech.ui.entity;

import net.aquatech.ui.client.FishMerchantClient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Скупщик рыбы: стоит на месте, по ПКМ открывает у игрока вкладку «Рыбалка» в F4.
 * Простой idle крутится на клиенте, случайные «подёргивания» и «спасибо» запускает сервер.
 */
public class FishMerchantEntity extends PathfinderMob implements GeoEntity {

    private static final String BODY = "body";
    private static final String FACE = "face";
    private static final String TRIG_FIDGET = "fidget";
    private static final String TRIG_TUG = "tug";
    private static final String TRIG_THANKS = "thanks";
    private static final String TRIG_HAPPY = "happy";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(FishMerchantAnims.IDLE);
    private static final RawAnimation FIDGET = RawAnimation.begin().thenPlay(FishMerchantAnims.IDLE_FIDGET);
    private static final RawAnimation TUG = RawAnimation.begin().thenPlay(FishMerchantAnims.IDLE_TUG);
    private static final RawAnimation THANKS = RawAnimation.begin().thenPlay(FishMerchantAnims.THANKS);
    private static final RawAnimation FACE_IDLE = RawAnimation.begin().thenLoop(FishMerchantAnims.FACE_IDLE);
    private static final RawAnimation FACE_HAPPY = RawAnimation.begin().thenPlay(FishMerchantAnims.FACE_HAPPY);

    /** Самая длинная idle-вставка (idle_fidget) идёт 5 с, поэтому пауза начинается с 10 с. */
    private static final int MIN_IDLE_GAP_TICKS = 200;
    private static final int EXTRA_IDLE_GAP_TICKS = 300;

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private int ticksToNextIdle = MIN_IDLE_GAP_TICKS;

    public FishMerchantEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoAi(true);
        setInvulnerable(true);
        setPersistenceRequired();
        setCustomName(Component.translatable("entity.aquatech_ui.fish_merchant").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
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
        if (!level().isClientSide && --ticksToNextIdle <= 0) {
            ticksToNextIdle = MIN_IDLE_GAP_TICKS + random.nextInt(EXTRA_IDLE_GAP_TICKS);
            triggerAnim(BODY, random.nextInt(3) == 0 ? TRIG_TUG : TRIG_FIDGET);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> FishMerchantClient::openFishMenu);
            return InteractionResult.SUCCESS;
        }
        turnTowards(player);
        triggerAnim(BODY, TRIG_THANKS);
        triggerAnim(FACE, TRIG_HAPPY);
        ticksToNextIdle = MIN_IDLE_GAP_TICKS + random.nextInt(EXTRA_IDLE_GAP_TICKS);
        return InteractionResult.CONSUME;
    }

    private void turnTowards(Player player) {
        double dx = player.getX() - getX();
        double dz = player.getZ() - getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        setYRot(yaw);
        setYHeadRot(yaw);
        setYBodyRot(yaw);
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
        // лицо регистрируется последним: на общих костях (глаза, брови, челюсть) его ключи перекрывают телесные
        controllers.add(
                new AnimationController<>(this, BODY, 6, state -> state.setAndContinue(IDLE))
                        .triggerableAnim(TRIG_FIDGET, FIDGET)
                        .triggerableAnim(TRIG_TUG, TUG)
                        .triggerableAnim(TRIG_THANKS, THANKS),
                new AnimationController<>(this, FACE, 4, state -> state.setAndContinue(FACE_IDLE))
                        .triggerableAnim(TRIG_HAPPY, FACE_HAPPY));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }
}
