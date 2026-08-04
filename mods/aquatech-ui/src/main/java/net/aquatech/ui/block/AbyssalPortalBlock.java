package net.aquatech.ui.block;

import net.aquatech.ui.fishing.FishingRodCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Decorative shrine block. Not a machine (no BlockEntity): right-clicking with an
 * AquaTech fishing rod in hand grants a temporary abyssal dive buff, on a per-block cooldown.
 */
public class AbyssalPortalBlock extends Block {

    private static final int COOLDOWN_TICKS = 20 * 60 * 3; // 3 minutes per shrine

    public AbyssalPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(net.minecraft.world.level.block.state.BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        boolean holdingAquaRod = FishingRodCompat.isSupportedRod(player.getMainHandItem())
                || FishingRodCompat.isSupportedRod(player.getOffhandItem());
        if (!holdingAquaRod) {
            player.displayClientMessage(Component.literal("§7Абиссальное ядро отзывается только на поддерживаемые удочки."), true);
            return InteractionResult.SUCCESS;
        }
        long lastUse = player.getPersistentData().getLong("aquatech_ui_abyssal_shrine_cd");
        long now = level.getGameTime();
        if (now - lastUse < COOLDOWN_TICKS) {
            long remaining = (COOLDOWN_TICKS - (now - lastUse)) / 20;
            player.displayClientMessage(Component.literal("§bЯдро ещё восстанавливается: §f" + remaining + "с"), true);
            return InteractionResult.SUCCESS;
        }
        player.getPersistentData().putLong("aquatech_ui_abyssal_shrine_cd", now);
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 90, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 20 * 90, 1));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 90, 0));
        level.playSound(null, pos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
        player.displayClientMessage(Component.literal("§3Абиссальное ядро благословляет твой спуск."), true);
        return InteractionResult.CONSUME;
    }
}
