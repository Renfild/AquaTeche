package net.aquatech.ui.item;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Жемчужина Пламени: ПКМ — Ад ↔ точка входа. Кулдаун 10 мин.
 * Также глушит ванильные порталы в Ад и клики по выведенным машинам AquaTech.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InfernalPearlHandler {

    private static final ResourceLocation PEARL_ID = new ResourceLocation("kubejs", "infernal_pearl");
    private static final String RETURN_KEY = "AquaInfernalReturn";
    private static final int COOLDOWN_TICKS = 20 * 60 * 10;

    private InfernalPearlHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        tryPearl(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (tryPearl(event)) {
            return;
        }
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (!isRetiredMachine(block)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.PASS);
    }

    @SubscribeEvent
    public static void onNetherPortal(BlockEvent.PortalSpawnEvent event) {
        event.setCanceled(true);
    }

    private static boolean tryPearl(PlayerInteractEvent event) {
        ItemStack stack = event.getItemStack();
        Item pearl = ForgeRegistries.ITEMS.getValue(PEARL_ID);
        if (pearl == null || stack.isEmpty() || stack.getItem() != pearl) {
            return false;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        apply(event.getEntity(), event.getLevel(), pearl);
        return true;
    }

    static void apply(Player player, Level level, Item pearl) {
        if (level.isClientSide) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel fromLevel)) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(pearl)) {
            float pct = player.getCooldowns().getCooldownPercent(pearl, 0.0F);
            int leftMin = Math.max(1, (int) Math.ceil(pct * COOLDOWN_TICKS / 20.0 / 60.0));
            player.displayClientMessage(Component.literal(
                    "§7[Жемчужина Пламени] Перезарядка: ещё ~" + leftMin + " мин."), true);
            return;
        }

        boolean inNether = fromLevel.dimension().equals(Level.NETHER);
        ServerLevel target = fromLevel.getServer().getLevel(inNether ? Level.OVERWORLD : Level.NETHER);
        if (target == null) {
            player.displayClientMessage(Component.literal("§c[Жемчужина Пламени] Измерение недоступно."), true);
            return;
        }

        Vec3 dest;
        if (inNether) {
            dest = returnPoint(serverPlayer, target);
            player.displayClientMessage(Component.literal("§6[Жемчужина Пламени] §fВозвращение завершено."), true);
        } else {
            BlockPos here = serverPlayer.blockPosition();
            serverPlayer.getPersistentData().putIntArray(RETURN_KEY,
                    new int[]{here.getX(), here.getY(), here.getZ()});
            dest = netherPlatform(target);
            player.displayClientMessage(Component.literal(
                    "§6[Жемчужина Пламени] §fТы в Аду. Обратно — та же жемчужина."), true);
        }
        teleport(serverPlayer, target, dest);
        player.getCooldowns().addCooldown(pearl, COOLDOWN_TICKS);
    }

    private static boolean isRetiredMachine(Block block) {
        return block == ModBlocks.AUTO_FISHER.get()
                || block == ModBlocks.OCEAN_FILTER.get()
                || block == ModBlocks.SEABED_DREDGER.get()
                || block == ModBlocks.FISH_SMOKER.get()
                || block == ModBlocks.OCEAN_ALTAR.get()
                || block == ModBlocks.ABYSSAL_PORTAL.get();
    }

    private static Vec3 netherPlatform(ServerLevel nether) {
        BlockPos center = new BlockPos(0, 70, 0);
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos floor = center.offset(x, -1, z);
                nether.setBlock(floor, Blocks.OBSIDIAN.defaultBlockState(), 3);
                for (int y = 0; y <= 2; y++) {
                    BlockPos clear = center.offset(x, y, z);
                    if (!nether.getBlockState(clear).isAir()) {
                        nether.setBlock(clear, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        return new Vec3(0.5, 70.1, 0.5);
    }

    private static Vec3 returnPoint(ServerPlayer player, ServerLevel overworld) {
        int[] saved = player.getPersistentData().getIntArray(RETURN_KEY);
        player.getPersistentData().remove(RETURN_KEY);
        BlockPos pos;
        if (saved.length == 3) {
            pos = new BlockPos(saved[0], saved[1], saved[2]);
        } else {
            pos = overworld.getSharedSpawnPos();
        }
        BlockPos safe = findSafe(overworld, pos);
        return new Vec3(safe.getX() + 0.5, safe.getY() + 0.1, safe.getZ() + 0.5);
    }

    private static BlockPos findSafe(ServerLevel level, BlockPos pos) {
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

    private static void teleport(ServerPlayer player, ServerLevel target, Vec3 dest) {
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
