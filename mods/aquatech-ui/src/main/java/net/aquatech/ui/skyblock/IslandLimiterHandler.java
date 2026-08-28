package net.aquatech.ui.skyblock;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IslandLimiterHandler {

    /** Caps in IslandLimiterRules: IU/AE2/Botania/DE, no Create. */
    private static final boolean ENABLED = true;

    private IslandLimiterHandler() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onPlaceCheck(BlockEvent.EntityPlaceEvent event) {
        if (!ENABLED || event.getLevel().isClientSide()) {
            return;
        }
        String key = IslandLimiterRules.keyFor(event.getPlacedBlock());
        if (key == null) {
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
        if (tracker.canPlace(owner, key)) {
            return;
        }
        event.setCanceled(true);
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal(
                    "§cЛимит на острове (/is): §f" + IslandLimiterRules.title(key)
                            + " §c" + tracker.count(owner, key) + "/" + IslandLimiterRules.max(key)), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlaceCommit(BlockEvent.EntityPlaceEvent event) {
        if (!ENABLED || event.isCanceled() || event.getLevel().isClientSide()) {
            return;
        }
        String key = IslandLimiterRules.keyFor(event.getPlacedBlock());
        if (key == null || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        UUID owner = ownerAt(level, event.getPos());
        if (owner == null) {
            return;
        }
        IslandLimiterTracker tracker = IslandLimiterTracker.get(level);
        tracker.increment(owner, key);
        Entity placer = event.getEntity();
        syncWatchers(level, owner, placer instanceof ServerPlayer sp ? sp : null);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!ENABLED || event.isCanceled() || event.getLevel().isClientSide()) {
            return;
        }
        BlockState state = event.getState();
        String key = IslandLimiterRules.keyFor(state);
        if (key == null || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        UUID owner = ownerAt(level, event.getPos());
        if (owner == null) {
            return;
        }
        IslandLimiterTracker tracker = IslandLimiterTracker.get(level);
        tracker.decrement(owner, key);
        ServerPlayer breaker = event.getPlayer() instanceof ServerPlayer sp ? sp : null;
        syncWatchers(level, owner, breaker);
    }

    public static UUID ownerAt(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null) {
            return null;
        }
        UUID raft = PersonalRaftSpawner.RaftRegistry.get(serverLevel).ownerAt(pos);
        if (raft != null) {
            return raft;
        }
        return WorldGuardIslandLookup.ownerAt(serverLevel, pos);
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
}
