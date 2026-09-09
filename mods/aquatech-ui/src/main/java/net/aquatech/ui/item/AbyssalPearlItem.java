package net.aquatech.ui.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Жемчужина Разлома: ПКМ — телепорт в Энд (платформа у (100, 49, 0)); в Энде — обратно
 * в Overworld (точка респавна, иначе спавн мира). Кулдаун 10 минут на предмете.
 */
public class AbyssalPearlItem extends Item {

    private static final int COOLDOWN_TICKS = 20 * 60 * 10; // 10 минут

    public AbyssalPearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!(level instanceof ServerLevel fromLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        boolean toEnd = !level.dimension().equals(Level.END);
        ServerLevel target = level.getServer().getLevel(toEnd ? Level.END : Level.OVERWORLD);
        if (target == null) {
            return InteractionResultHolder.fail(stack);
        }

        Vec3 dest;
        if (toEnd) {
            dest = endPlatform(target);
            // запоминаем точку выхода из Энда — возвращаемся ровно туда же, а не в шахту
            serverPlayer.getPersistentData().putLongArray("AquaRiftReturn",
                    new long[]{serverPlayer.blockPosition().asLong()});
        } else {
            dest = returnPoint(serverPlayer, target);
        }
        teleport(serverPlayer, target, dest);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    /** Платформа как у ванильного входа в Энд: обсидиан (100, 48, 0) + воздух над ней. */
    private Vec3 endPlatform(ServerLevel end) {
        BlockPos center = new BlockPos(100, 48, 0);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos floor = center.offset(x, -1, z);
                if (!end.getBlockState(floor).isSolidRender(end, floor)) {
                    end.setBlock(floor, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
                for (int y = 0; y <= 2; y++) {
                    BlockPos clear = center.offset(x, y, z);
                    if (!end.getBlockState(clear).isAir()) {
                        end.setBlock(clear, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        return new Vec3(100.5, 48.0, 0.5);
    }

    private Vec3 returnPoint(ServerPlayer player, ServerLevel overworld) {
        long[] saved = player.getPersistentData().getLongArray("AquaRiftReturn");
        if (saved.length == 3) {
            player.getPersistentData().remove("AquaRiftReturn");
            BlockPos pos = new BlockPos((int) saved[0], (int) saved[1], (int) saved[2]);
            BlockPos safe = findSafe(overworld, pos);
            return new Vec3(safe.getX() + 0.5, safe.getY() + 0.1, safe.getZ() + 0.5);
        }
        BlockPos spawn = overworld.getSharedSpawnPos();
        BlockPos safe = findSafe(overworld, spawn);
        return new Vec3(safe.getX() + 0.5, safe.getY() + 0.1, safe.getZ() + 0.5);
    }

    private BlockPos findSafe(ServerLevel level, BlockPos pos) {
        BlockPos cur = pos;
        int max = level.getMaxBuildHeight() - 2;
        while (cur.getY() < max) {
            boolean solid = level.getBlockState(cur).isSolidRender(level, cur);
            boolean air1 = level.getBlockState(cur.above()).isAir();
            boolean air2 = level.getBlockState(cur.above(2)).isAir();
            if (solid && air1 && air2) {
                return cur.above();
            }
            cur = cur.above();
        }
        return pos;
    }

    private void teleport(ServerPlayer player, ServerLevel target, Vec3 dest) {
        player.teleportTo(target, dest.x, dest.y, dest.z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        target.playSound(null, dest.x, dest.y, dest.z,
                SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.5F, 1.4F);
        for (int i = 0; i < 40; i++) {
            double dx = (player.getRandom().nextDouble() - 0.5) * 2.0;
            double dy = player.getRandom().nextDouble() * 2.0;
            double dz = (player.getRandom().nextDouble() - 0.5) * 2.0;
            target.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    dest.x + dx, dest.y + dy, dest.z + dz, 1, 0, 0, 0, 0.05);
        }
    }
}
