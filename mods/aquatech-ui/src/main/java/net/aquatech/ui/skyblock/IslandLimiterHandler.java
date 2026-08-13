package net.aquatech.ui.skyblock;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IslandLimiterHandler {

    private IslandLimiterHandler() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onPlaceCheck(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        String id = blockId(event.getPlacedBlock().getBlock());
        if (!IslandLimiterRules.isLimited(id)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        UUID owner = ownerAt(level, event.getPos());
        if (owner == null) {
            return;
        }
        IslandLimiterTracker tracker = IslandLimiterTracker.get(level);
        if (tracker.canPlace(owner, id)) {
            return;
        }
        event.setCanceled(true);
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal(
                    "§cЛимит на острове: §f" + IslandLimiterRules.title(id)
                            + " §c" + tracker.count(owner, id) + "/" + IslandLimiterRules.max(id)), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlaceCommit(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || event.getLevel().isClientSide()) {
            return;
        }
        String id = blockId(event.getPlacedBlock().getBlock());
        if (!IslandLimiterRules.isLimited(id) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        UUID owner = ownerAt(level, event.getPos());
        if (owner == null) {
            return;
        }
        IslandLimiterTracker tracker = IslandLimiterTracker.get(level);
        tracker.increment(owner, id);
        Entity placer = event.getEntity();
        syncWatchers(level, owner, placer instanceof ServerPlayer sp ? sp : null);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || event.getLevel().isClientSide()) {
            return;
        }
        String id = blockId(event.getState().getBlock());
        if (!IslandLimiterRules.isLimited(id) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        UUID owner = ownerAt(level, event.getPos());
        if (owner == null) {
            return;
        }
        IslandLimiterTracker tracker = IslandLimiterTracker.get(level);
        tracker.decrement(owner, id);
        ServerPlayer breaker = event.getPlayer() instanceof ServerPlayer sp ? sp : null;
        syncWatchers(level, owner, breaker);
    }

    static UUID ownerAt(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null) {
            return null;
        }
        return PersonalRaftSpawner.RaftRegistry.get(serverLevel).ownerAt(pos);
    }

    private static void syncWatchers(ServerLevel level, UUID owner, ServerPlayer actor) {
        IslandLimiterTracker tracker = IslandLimiterTracker.get(level);
        if (actor != null) {
            tracker.syncTo(actor, owner);
        }
        if (level.getServer() == null) {
            return;
        }
        ServerPlayer ownerPlayer = level.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer != null && (actor == null || !ownerPlayer.getUUID().equals(actor.getUUID()))) {
            tracker.syncTo(ownerPlayer, owner);
        }
    }

    static String blockId(Block block) {
        ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(block);
        return loc == null ? "" : loc.toString();
    }
}
