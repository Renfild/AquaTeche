package net.aquatech.ui.skyblock;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;

/**
 * Protects personal rafts / islands.
 * Uses SkyblockBuilder when present; otherwise aquatech PersonalRaftSpawner claims.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IslandGuardHandler {

    private static final Component DENIED = Component.literal("§cЭто не ваш плот.");

    private IslandGuardHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!guard(player, event.getPos(), event.getLevel())) return;
        event.setCanceled(true);
        player.displayClientMessage(DENIED, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        if (!guard(player, event.getPos(), event.getLevel())) return;
        event.setCanceled(true);
        player.displayClientMessage(DENIED, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getLevel().isClientSide()) return;
        if (!guard(player, event.getPos(), event.getLevel())) return;
        event.setCanceled(true);
        player.displayClientMessage(DENIED, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPvp(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer victim && event.getSource().getEntity() instanceof ServerPlayer attacker) {
            if (!victim.level().isClientSide()) {
                event.setCanceled(true);
                attacker.displayClientMessage(Component.literal("§cPvP отключено в районе личных плотов!"), true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onExplosionDetonate(net.minecraftforge.event.level.ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(ServerLevel.OVERWORLD)) return;

        PersonalRaftSpawner.RaftRegistry reg = PersonalRaftSpawner.RaftRegistry.get(level);
        event.getAffectedBlocks().removeIf(pos -> !reg.isFreeFromAny(pos));
    }

    /** Block Botania Garden-of-Glass island teleports if the command exists. */
    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        String raw = event.getParseResults().getReader().getString();
        if (raw == null) return;
        String lower = raw.toLowerCase().trim();
        if (lower.startsWith("gardenofglass") || lower.startsWith("/gardenofglass")
                || lower.contains(" gardenofglass ")) {
            event.setCanceled(true);
            if (event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal(
                        "§cGarden of Glass отключён. Личный плот выдаётся при входе."));
            }
        }
    }

    /**
     * @return true if the action should be denied
     */
    private static boolean guard(ServerPlayer player, BlockPos pos, LevelAccessor level) {
        if (level.isClientSide()) return false;
        if (canBypass(player)) return false;
        if (!(level instanceof ServerLevel serverLevel)) return false;
        if (!serverLevel.dimension().equals(ServerLevel.OVERWORLD)) return false;

        if (ModList.get().isLoaded("skyblockbuilder")) {
            try {
                return !isOnOwnIsland(player, pos, serverLevel);
            } catch (Throwable t) {
                AquaTechUI.LOGGER.debug("IslandGuard reflection failed: {}", t.toString());
                return false;
            }
        }

        // Personal raft claims (no SkyblockBuilder).
        BlockPos raft = PersonalRaftSpawner.raftCenterFromPlayerData(player);
        if (raft == null) {
            // Not assigned yet — allow near world spawn hub only.
            BlockPos spawn = serverLevel.getSharedSpawnPos();
            return horizontalDistSq(pos, spawn) > (long) PersonalRaftSpawner.CLAIM_RADIUS * PersonalRaftSpawner.CLAIM_RADIUS;
        }
        return horizontalDistSq(pos, raft) > (long) PersonalRaftSpawner.CLAIM_RADIUS * PersonalRaftSpawner.CLAIM_RADIUS;
    }

    private static boolean canBypass(ServerPlayer player) {
        return player.isCreative() || player.hasPermissions(2);
    }

    private static boolean isOnOwnIsland(ServerPlayer player, BlockPos pos, ServerLevel level) throws Exception {
        Class<?> dataClass = Class.forName("de.melanx.skyblockbuilder.data.SkyblockSavedData");
        Method get = dataClass.getMethod("get", ServerLevel.class);
        Object data = get.invoke(null, level);
        if (data == null) return false;

        Method hasTeam = dataClass.getMethod("hasPlayerTeam", Player.class);
        if (!Boolean.TRUE.equals(hasTeam.invoke(data, player))) {
            // No team yet — only allow near world spawn hub (first island / lobby)
            BlockPos spawn = level.getSharedSpawnPos();
            return horizontalDistSq(pos, spawn) <= (long) PersonalRaftSpawner.CLAIM_RADIUS * PersonalRaftSpawner.CLAIM_RADIUS;
        }

        Method getTeam = dataClass.getMethod("getTeamFromPlayer", Player.class);
        Object team = getTeam.invoke(data, player);
        if (team == null) {
            Method byUuid = dataClass.getMethod("getTeamFromPlayer", java.util.UUID.class);
            team = byUuid.invoke(data, player.getUUID());
        }
        if (team == null) return false;

        BlockPos islandCenter = resolveIslandCenter(team, data, player);
        if (islandCenter == null) return false;
        return horizontalDistSq(pos, islandCenter) <= (long) PersonalRaftSpawner.CLAIM_RADIUS * PersonalRaftSpawner.CLAIM_RADIUS;
    }

    private static BlockPos resolveIslandCenter(Object team, Object data, ServerPlayer player) throws Exception {
        // Team#getIsland / getIslandPos variants
        for (String name : new String[]{"getIsland", "getIslandPos", "islandPos"}) {
            try {
                Method m = team.getClass().getMethod(name);
                Object island = m.invoke(team);
                BlockPos pos = toBlockPos(island);
                if (pos != null) return pos;
            } catch (NoSuchMethodException ignored) {
            }
        }
        // SkyblockSavedData#getTeamIsland(UUID)
        try {
            Method m = data.getClass().getMethod("getTeamIsland", java.util.UUID.class);
            Object island = m.invoke(data, player.getUUID());
            BlockPos pos = toBlockPos(island);
            if (pos != null) return pos;
        } catch (NoSuchMethodException ignored) {
        }
        // Team might expose getCenter / getSpawn
        for (String name : new String[]{"getCenter", "getSpawn", "getIslandSpawn"}) {
            try {
                Method m = team.getClass().getMethod(name);
                Object o = m.invoke(team);
                BlockPos pos = toBlockPos(o);
                if (pos != null) return pos;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static BlockPos toBlockPos(Object o) throws Exception {
        if (o == null) return null;
        if (o instanceof BlockPos bp) return bp;
        // IslandPos usually has getX/getZ or toBlockPos / getCenter
        for (String name : new String[]{"getCenter", "toBlockPos", "getPos", "getMinimum", "getIslandPos"}) {
            try {
                Method m = o.getClass().getMethod(name);
                Object r = m.invoke(o);
                if (r instanceof BlockPos bp) return bp;
            } catch (NoSuchMethodException ignored) {
            }
        }
        try {
            Method gx = o.getClass().getMethod("getX");
            Method gz = o.getClass().getMethod("getZ");
            int x = ((Number) gx.invoke(o)).intValue();
            int z = ((Number) gz.invoke(o)).intValue();
            int y = 190;
            try {
                Method gy = o.getClass().getMethod("getY");
                y = ((Number) gy.invoke(o)).intValue();
            } catch (NoSuchMethodException ignored) {
            }
            return new BlockPos(x, y, z);
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    private static long horizontalDistSq(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }
}
