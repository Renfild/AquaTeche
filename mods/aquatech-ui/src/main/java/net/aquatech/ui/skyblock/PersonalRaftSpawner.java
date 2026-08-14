package net.aquatech.ui.skyblock;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.aquatech.ui.AquaTechUI;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Personal ocean rafts without SkyblockBuilder.
 * Dynamically integrated with WorldGuard 7 API for automatic claims, friend management,
 * and 1-time raft recreate limit with structure & claim cleanup.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PersonalRaftSpawner {
    public static final String TAG_READY = AquaTechUI.MOD_ID + ":raft_ready";
    public static final String TAG_X = AquaTechUI.MOD_ID + ":raft_x";
    public static final String TAG_Y = AquaTechUI.MOD_ID + ":raft_y";
    public static final String TAG_Z = AquaTechUI.MOD_ID + ":raft_z";
    public static final String TAG_RESET_COUNT = AquaTechUI.MOD_ID + ":raft_reset_count";

    /** Min distance between raft centers (must be > 2× claim radius). */
    public static final int RAFT_SPACING = 320;
    /** Soft claim: build/break only near your raft (80 blocks radius, full height -64 to 320). */
    public static final int CLAIM_RADIUS = 80;

    private static final int DECK_Y = 190;
    private static final int SPAWN_Y = 191;
    private static final int HUB_CLEAR = 320; // keep spawn hub free

    private PersonalRaftSpawner() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        var buildCmd = Commands.literal("raft")
                .executes(ctx -> handleRaftCommand(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("addfriend")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> handleAddFriend(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("removefriend")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> handleRemoveFriend(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("reset")
                        .executes(ctx -> handleResetRequest(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("confirm")
                                .executes(ctx -> handleResetRaftConfirm(ctx.getSource().getPlayerOrException()))))
                .then(Commands.literal("recreate")
                        .executes(ctx -> handleResetRequest(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("confirm")
                                .executes(ctx -> handleResetRaftConfirm(ctx.getSource().getPlayerOrException()))));

        var buildIs = Commands.literal("is")
                .executes(ctx -> handleRaftCommand(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("addfriend")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> handleAddFriend(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("removefriend")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> handleRemoveFriend(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("reset")
                        .executes(ctx -> handleResetRequest(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("confirm")
                                .executes(ctx -> handleResetRaftConfirm(ctx.getSource().getPlayerOrException()))))
                .then(Commands.literal("recreate")
                        .executes(ctx -> handleResetRequest(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("confirm")
                                .executes(ctx -> handleResetRaftConfirm(ctx.getSource().getPlayerOrException()))));

        dispatcher.register(buildCmd);
        dispatcher.register(buildIs);
    }

    public static int handleAddFriend(ServerPlayer player, String friendName) {
        String regionId = raftRegionId(player);
        try {
            Object regionManager = getWGRegionManager();
            if (regionManager != null) {
                Method getRegion = findMethod(regionManager.getClass(), "getRegion", 1);
                if (getRegion != null) {
                    Object region = getRegion.invoke(regionManager, regionId);
                    if (region != null) {
                        Method getMembers = region.getClass().getMethod("getMembers");
                        Object members = getMembers.invoke(region);
                        try {
                            members.getClass().getMethod("addPlayer", String.class).invoke(members, friendName);
                        } catch (Throwable t) {
                            members.getClass().getMethod("addPlayer", UUID.class).invoke(members, UUID.nameUUIDFromBytes(("OfflinePlayer:" + friendName).getBytes()));
                        }
                        saveRegionManager(regionManager);
                        player.sendSystemMessage(Component.literal("§a⚓ Игрок §f" + friendName + " §aдобавлен в приват вашего плота!"));
                        return 1;
                    }
                }
            }
        } catch (Throwable t) {
            AquaTechUI.LOGGER.error("Add friend error: ", t);
        }
        player.sendSystemMessage(Component.literal("§c⚓ Не удалось добавить игрока. Проверьте имя!"));
        return 0;
    }

    public static int handleRemoveFriend(ServerPlayer player, String friendName) {
        String regionId = raftRegionId(player);
        try {
            Object regionManager = getWGRegionManager();
            if (regionManager != null) {
                Method getRegion = findMethod(regionManager.getClass(), "getRegion", 1);
                if (getRegion != null) {
                    Object region = getRegion.invoke(regionManager, regionId);
                    if (region != null) {
                        Method getMembers = region.getClass().getMethod("getMembers");
                        Object members = getMembers.invoke(region);
                        try {
                            members.getClass().getMethod("removePlayer", String.class).invoke(members, friendName);
                        } catch (Throwable t) {
                            members.getClass().getMethod("removePlayer", UUID.class).invoke(members, UUID.nameUUIDFromBytes(("OfflinePlayer:" + friendName).getBytes()));
                        }
                        saveRegionManager(regionManager);
                        player.sendSystemMessage(Component.literal("§c⚓ Игрок §f" + friendName + " §cудален из вашего привата!"));
                        return 1;
                    }
                }
            }
        } catch (Throwable t) {
            AquaTechUI.LOGGER.error("Remove friend error: ", t);
        }
        player.sendSystemMessage(Component.literal("§c⚓ Не удалось удалить игрока из привата."));
        return 0;
    }

    public static int handleResetRequest(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int resetCount = data.getInt(TAG_RESET_COUNT);
        if (resetCount >= 1) {
            player.sendSystemMessage(Component.literal("§c⚓ Вы уже использовали единственный лимит на пересоздание плота (1/1)!"));
            return 0;
        }

        player.sendSystemMessage(Component.literal("§c⚠️ ВНИМАНИЕ: Пересоздание плота полностью УДАЛИТ ваш текущий плот, приват и все постройки и вещи!"));
        player.sendSystemMessage(Component.literal("§eУ вас осталось пересозданий: §f1/1§e. Для подтверждения введите: §a/raft reset confirm"));
        return 1;
    }

    public static int handleResetRaftConfirm(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(ServerLevel.OVERWORLD);
        if (level == null) return 0;

        CompoundTag data = player.getPersistentData();
        int resetCount = data.getInt(TAG_RESET_COUNT);
        if (resetCount >= 1) {
            player.sendSystemMessage(Component.literal("§c⚓ Вы уже использовали единственный лимит на пересоздание плота (1/1)!"));
            return 0;
        }

        // 1. Fetch old raft coordinates to destroy old structures
        RaftRegistry registry = RaftRegistry.get(level);
        RaftRegistry.Entry oldEntry = registry.getEntry(player.getUUID());

        int oldX = oldEntry != null ? oldEntry.x() : data.getInt(TAG_X);
        int oldY = oldEntry != null ? oldEntry.y() : data.getInt(TAG_Y);
        int oldZ = oldEntry != null ? oldEntry.z() : data.getInt(TAG_Z);

        if (oldX != 0 || oldZ != 0) {
            clearOldRaftArea(level, oldX, DECK_Y, oldZ);
        }

        // 2. Delete old WorldGuard region
        String regionId = raftRegionId(player);
        try {
            Object regionManager = getWGRegionManager();
            if (regionManager != null) {
                Method removeRegion = findMethod(regionManager.getClass(), "removeRegion", 1);
                if (removeRegion != null) {
                    removeRegion.invoke(regionManager, regionId);
                    saveRegionManager(regionManager);
                }
            }
        } catch (Throwable ignored) {}

        // 3. Mark reset count as 1 used
        data.putInt(TAG_RESET_COUNT, 1);
        data.remove(TAG_READY);
        data.remove(TAG_X);
        data.remove(TAG_Y);
        data.remove(TAG_Z);

        registry.entries.removeIf(e -> e.owner().equals(player.getUUID()));
        registry.setDirty();

        // 4. Spawn new raft at fresh coordinates
        trySpawnRaft(player);
        player.sendSystemMessage(Component.literal("§a⚓ Твой плот и приват успешно пересозданы на новых координатах!"));
        player.sendSystemMessage(Component.literal("§e⚠️ Лимит пересоздания плота исчерпан (1/1)."));
        return 1;
    }

    private static void clearOldRaftArea(ServerLevel level, int cx, int DECK_Y, int cz) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int dx = -25; dx <= 25; dx++) {
            for (int dz = -25; dz <= 25; dz++) {
                for (int dy = -10; dy <= 40; dy++) {
                    int y = DECK_Y + dy;
                    if (y < -64 || y > 319) continue;
                    mpos.set(cx + dx, y, cz + dz);
                    if (y < 190) {
                        level.setBlock(mpos, Blocks.WATER.defaultBlockState(), 2);
                    } else {
                        level.setBlock(mpos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (ModList.get().isLoaded("skyblockbuilder")) {
            return;
        }
        player.getServer().execute(() -> trySpawnRaft(player));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();

        if (oldData.contains(TAG_READY)) {
            newData.putBoolean(TAG_READY, oldData.getBoolean(TAG_READY));
            newData.putInt(TAG_X, oldData.getInt(TAG_X));
            newData.putInt(TAG_Y, oldData.getInt(TAG_Y));
            newData.putInt(TAG_Z, oldData.getInt(TAG_Z));
        }
        if (oldData.contains(TAG_RESET_COUNT)) {
            newData.putInt(TAG_RESET_COUNT, oldData.getInt(TAG_RESET_COUNT));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.server.getLevel(ServerLevel.OVERWORLD);
        if (level == null) return;

        RaftRegistry registry = RaftRegistry.get(level);
        RaftRegistry.Entry entry = registry.getEntry(player.getUUID());
        if (entry != null) {
            BlockPos raftSpawn = new BlockPos(entry.x(), SPAWN_Y, entry.z());
            player.setRespawnPosition(ServerLevel.OVERWORLD, raftSpawn, 180.0f, true, false);

            BlockPos current = player.blockPosition();
            if (Math.abs(current.getX() - entry.x()) > CLAIM_RADIUS || Math.abs(current.getZ() - entry.z()) > CLAIM_RADIUS) {
                player.teleportTo(level, entry.x() + 0.5, SPAWN_Y + 0.1, entry.z() + 0.5, 180.0f, 0.0f);
                player.sendSystemMessage(Component.literal("§a⚓ Вы возродились на своем плоту!"));
            }
        }
    }

    public static int handleRaftCommand(ServerPlayer player) {
        if (!player.isAlive() || player.hasDisconnected()) {
            return 0;
        }
        ServerLevel level = player.server.getLevel(ServerLevel.OVERWORLD);
        if (level == null) return 0;

        CompoundTag data = player.getPersistentData();
        RaftRegistry registry = RaftRegistry.get(level);

        RaftRegistry.Entry entry = registry.getEntry(player.getUUID());
        if (entry != null || data.getBoolean(TAG_READY)) {
            int x = entry != null ? entry.x() : data.getInt(TAG_X);
            int y = entry != null ? entry.y() : data.getInt(TAG_Y);
            int z = entry != null ? entry.z() : data.getInt(TAG_Z);

            data.putBoolean(TAG_READY, true);
            data.putInt(TAG_X, x);
            data.putInt(TAG_Y, y);
            data.putInt(TAG_Z, z);

            ensureWorldGuardClaim(player, new BlockPos(x, y, z));
            player.teleportTo(level, x + 0.5, SPAWN_Y + 0.1, z + 0.5, player.getYRot(), player.getXRot());

            String regionId = raftRegionId(player);
            player.sendSystemMessage(Component.literal(
                    "§a⚓ Плот. §7Приват: §f/rg info " + regionId + " §7· границы: §f/rg select " + regionId));
            return 1;
        }

        trySpawnRaft(player);
        return 1;
    }

    private static void trySpawnRaft(ServerPlayer player) {
        if (!player.isAlive() || player.hasDisconnected()) {
            return;
        }
        ServerLevel level = player.server.getLevel(ServerLevel.OVERWORLD);
        if (level == null) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        RaftRegistry registry = RaftRegistry.get(level);

        RaftRegistry.Entry existing = registry.getEntry(player.getUUID());
        if (existing != null) {
            data.putBoolean(TAG_READY, true);
            data.putInt(TAG_X, existing.x());
            data.putInt(TAG_Y, existing.y());
            data.putInt(TAG_Z, existing.z());

            BlockPos raftSpawn = new BlockPos(existing.x(), SPAWN_Y, existing.z());
            player.setRespawnPosition(ServerLevel.OVERWORLD, raftSpawn, 180.0f, true, false);
            ensureWorldGuardClaim(player, new BlockPos(existing.x(), existing.y(), existing.z()));
            // Do NOT call autoSetHome on login if raft already exists!
            return;
        }

        if (data.getBoolean(TAG_READY)) {
            BlockPos pos = new BlockPos(data.getInt(TAG_X), SPAWN_Y, data.getInt(TAG_Z));
            registry.register(player.getUUID(), pos);
            player.setRespawnPosition(ServerLevel.OVERWORLD, pos, 180.0f, true, false);
            ensureWorldGuardClaim(player, new BlockPos(data.getInt(TAG_X), data.getInt(TAG_Y), data.getInt(TAG_Z)));
            // Do NOT call autoSetHome on login if raft already exists!
            return;
        }

        BlockPos center = findFreeRaftCenter(level, registry, player);
        placeRaft(level, center);
        registry.register(player.getUUID(), center);
        ensureWorldGuardClaim(player, center);

        data.putBoolean(TAG_READY, true);
        data.putInt(TAG_X, center.getX());
        data.putInt(TAG_Y, DECK_Y);
        data.putInt(TAG_Z, center.getZ());

        player.setRespawnPosition(ServerLevel.OVERWORLD, center.above(), 180.0f, true, false);
        autoSetHome(player);

        String regionId = raftRegionId(player);
        player.sendSystemMessage(Component.literal(
                "§a⚓ Плот 4×4 на Y190. Приват §f" + regionId + " §a(±" + CLAIM_RADIUS + " блоков)."));
        player.sendSystemMessage(Component.literal(
                "§7Инфо: §f/rg info " + regionId + " §8| §7границы: §f/rg select " + regionId));
        player.sendSystemMessage(Component.literal("§a🏠 Точка /home автоматически установлена!"));
        AquaTechUI.LOGGER.info("Personal raft 4x4 at {},{},{} for {}",
                center.getX(), DECK_Y, center.getZ(), player.getGameProfile().getName());
    }

    private static void autoSetHome(ServerPlayer player) {
        try {
            player.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withPermission(4), "sethome home");
        } catch (Throwable t) {
            AquaTechUI.LOGGER.debug("Auto sethome via command stack failed: {}", t.toString());
        }
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method dispatch = bukkitClass.getMethod("dispatchCommand", Class.forName("org.bukkit.command.CommandSender"), String.class);
            Object console = bukkitClass.getMethod("getConsoleSender").invoke(null);
            dispatch.invoke(null, console, "sethome " + player.getGameProfile().getName() + " home");
        } catch (Throwable ignored) {}
    }

    public static String raftRegionId(ServerPlayer player) {
        String ownerName = player.getGameProfile().getName();
        return (ownerName + "_raft").toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
    }

    /** Create or refresh the cuboid WG claim around a raft center. */
    public static void ensureWorldGuardClaim(ServerPlayer player, BlockPos center) {
        try {
            createWorldGuardClaim(player, center);
        } catch (Throwable t) {
            AquaTechUI.LOGGER.error("WorldGuard raft claim failed for {}: {}",
                    player.getGameProfile().getName(), t.toString(), t);
        }
    }

    private static ClassLoader getPluginClassLoader() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object pm = bukkitClass.getMethod("getPluginManager").invoke(null);
            Object wgPlugin = pm.getClass().getMethod("getPlugin", String.class).invoke(pm, "WorldGuard");
            if (wgPlugin != null) {
                return wgPlugin.getClass().getClassLoader();
            }
        } catch (Throwable t) {
            AquaTechUI.LOGGER.debug("Could not resolve WorldGuard PluginClassLoader: {}", t.toString());
        }
        return PersonalRaftSpawner.class.getClassLoader();
    }

    private static void createWorldGuardClaim(ServerPlayer player, BlockPos center) throws Exception {
        ClassLoader cl = getPluginClassLoader();

        Object regionManager = getWGRegionManager(cl);
        if (regionManager == null) {
            AquaTechUI.LOGGER.warn("WorldGuard RegionManager null.");
            return;
        }

        String regionId = raftRegionId(player);
        int minX = center.getX() - CLAIM_RADIUS;
        int maxX = center.getX() + CLAIM_RADIUS;
        int minY = -64;
        int maxY = 320;
        int minZ = center.getZ() - CLAIM_RADIUS;
        int maxZ = center.getZ() + CLAIM_RADIUS;

        Class<?> blockVector3 = Class.forName("com.sk89q.worldedit.math.BlockVector3", true, cl);
        Method at = blockVector3.getMethod("at", int.class, int.class, int.class);
        Object min = at.invoke(null, minX, minY, minZ);
        Object max = at.invoke(null, maxX, maxY, maxZ);

        Class<?> protectedCuboid = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion", true, cl);
        Object region = protectedCuboid
                .getConstructor(String.class, blockVector3, blockVector3)
                .newInstance(regionId, min, max);

        Class<?> defaultDomain = Class.forName("com.sk89q.worldguard.domains.DefaultDomain", true, cl);
        Object owners = defaultDomain.getConstructor().newInstance();
        try {
            owners.getClass().getMethod("addPlayer", UUID.class).invoke(owners, player.getUUID());
        } catch (Throwable ignored) {
            owners.getClass().getMethod("addPlayer", String.class).invoke(owners, player.getGameProfile().getName());
        }
        region.getClass().getMethod("setOwners", defaultDomain).invoke(region, owners);

        Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.Flags", true, cl);
        Class<?> stateEnum = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag$State", true, cl);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Object deny = Enum.valueOf((Class<Enum>) stateEnum, "DENY");
        Method setFlag = findMethod(region.getClass(), "setFlag", 2);
        if (setFlag != null) {
            for (String flagName : new String[]{"PVP", "CREEPER_EXPLOSION", "TNT", "OTHER_EXPLOSION", "MOB_DAMAGE"}) {
                try {
                    Object flag = flagsClass.getField(flagName).get(null);
                    setFlag.invoke(region, flag, deny);
                } catch (Throwable ignored) {}
            }
            try {
                Object greeting = flagsClass.getField("GREET_MESSAGE").get(null);
                setFlag.invoke(region, greeting, "§aПлот " + player.getGameProfile().getName());
            } catch (Throwable ignored) {}
        }

        Method removeRegion = findMethod(regionManager.getClass(), "removeRegion", 1);
        if (removeRegion != null) {
            try {
                removeRegion.invoke(regionManager, regionId);
            } catch (Throwable ignored) {}
        }

        Method addRegion = findMethod(regionManager.getClass(), "addRegion", 1);
        if (addRegion != null) {
            addRegion.invoke(regionManager, region);
            saveRegionManager(regionManager);
            AquaTechUI.LOGGER.info("WorldGuard region '{}' created and saved dynamically via WG API for {}", regionId, player.getGameProfile().getName());
        } else {
            AquaTechUI.LOGGER.error("addRegion method not found on RegionManager!");
        }
    }

    private static Object getWGRegionManager(ClassLoader cl) {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object bukkitWorld = bukkitClass.getMethod("getWorld", String.class).invoke(null, "world");
            if (bukkitWorld == null) {
                Object worldsList = bukkitClass.getMethod("getWorlds").invoke(null);
                if (worldsList instanceof List<?> list && !list.isEmpty()) {
                    bukkitWorld = list.get(0);
                }
            }
            if (bukkitWorld == null) return null;

            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard", true, cl);
            Object wg = wgClass.getMethod("getInstance").invoke(null);
            Object platform = wg.getClass().getMethod("getPlatform").invoke(wg);
            Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);

            Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter", true, cl);
            Object weWorld = bukkitAdapter.getMethod("adapt", Class.forName("org.bukkit.World")).invoke(null, bukkitWorld);

            Method getManager = findMethod(container.getClass(), "get", 1);
            if (getManager != null) {
                return getManager.invoke(container, weWorld);
            }
        } catch (Throwable t) {
            AquaTechUI.LOGGER.error("getWGRegionManager error: ", t);
        }
        return null;
    }

    private static Object getWGRegionManager() {
        return getWGRegionManager(getPluginClassLoader());
    }

    private static void saveRegionManager(Object regionManager) {
        try {
            regionManager.getClass().getMethod("save").invoke(regionManager);
        } catch (Throwable ignored) {
            try {
                regionManager.getClass().getMethod("saveChanges").invoke(regionManager);
            } catch (Throwable ignored2) {}
        }
    }

    private static Method findMethod(Class<?> clazz, String name, int paramCount) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                return m;
            }
        }
        return null;
    }

    private static BlockPos findFreeRaftCenter(ServerLevel level, RaftRegistry registry, ServerPlayer player) {
        int preferred = Math.floorMod(player.getUUID().hashCode(), 10_000) + 1;
        for (int attempt = 0; attempt < 400; attempt++) {
            int slot = preferred + attempt;
            BlockPos candidate = slotToPos(slot);
            if (isTooCloseToHub(candidate)) {
                continue;
            }
            if (!registry.isFree(candidate, player.getUUID())) {
                continue;
            }
            if (looksOccupied(level, candidate)) {
                continue;
            }
            return candidate;
        }
        long bits = player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits();
        int x = (int) ((bits & 0xFFFF) - 0x7FFF) * RAFT_SPACING;
        int z = (int) (((bits >>> 16) & 0xFFFF) - 0x7FFF) * RAFT_SPACING;
        if (x == 0 && z == 0) {
            x = RAFT_SPACING;
        }
        return new BlockPos(x, DECK_Y, z);
    }

    private static BlockPos slotToPos(int slot) {
        int s = Math.max(1, slot);
        int gx = s % 100;
        int gz = s / 100;
        if (gx == 0 && gz == 0) {
            gx = 1;
        }
        return new BlockPos(gx * RAFT_SPACING, DECK_Y, gz * RAFT_SPACING);
    }

    private static boolean isTooCloseToHub(BlockPos pos) {
        long dx = pos.getX();
        long dz = pos.getZ();
        return dx * dx + dz * dz < (long) HUB_CLEAR * HUB_CLEAR;
    }

    private static boolean looksOccupied(ServerLevel level, BlockPos center) {
        int solids = 0;
        for (int dx = -2; dx <= 3; dx++) {
            for (int dz = -2; dz <= 3; dz++) {
                BlockPos p = new BlockPos(center.getX() + dx, DECK_Y, center.getZ() + dz);
                BlockState st = level.getBlockState(p);
                if (st.isAir() || st.getFluidState().isSource() || !st.getFluidState().isEmpty()) {
                    continue;
                }
                if (st.blocksMotion() || st.is(Blocks.CHEST) || st.is(Blocks.OAK_PLANKS) || st.is(Blocks.OAK_LOG)) {
                    solids++;
                }
            }
        }
        return solids >= 3;
    }

    public static BlockPos raftCenterFromPlayerData(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(TAG_READY)) {
            return null;
        }
        return new BlockPos(data.getInt(TAG_X), data.getInt(TAG_Y), data.getInt(TAG_Z));
    }

    private static void placeRaft(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cy = DECK_Y; // Y = 190
        int cz = center.getZ();

        level.getChunk(cx >> 4, cz >> 4);

        for (int dx = -2; dx <= 3; dx++) {
            for (int dz = -2; dz <= 3; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    level.setBlock(new BlockPos(cx + dx, cy + dy, cz + dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        BlockState logX = Blocks.OAK_LOG.defaultBlockState()
                .setValue(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.X);
        BlockState logZ = Blocks.OAK_LOG.defaultBlockState()
                .setValue(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.Z);
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();

        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                boolean rim = dx == -1 || dx == 2 || dz == -1 || dz == 2;
                BlockPos pos = new BlockPos(cx + dx, cy, cz + dz);
                level.setBlock(pos, rim ? ((dz == -1 || dz == 2) ? logX : logZ) : planks, 3);
            }
        }

        BlockPos chestPos = new BlockPos(cx, cy + 1, cz);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            chest.setCustomName(Component.literal("Плот"));
        }
    }

    /** World-saved list of raft claims so new spawns never overlap. */
    public static final class RaftRegistry extends SavedData {
        private static final String DATA_NAME = AquaTechUI.MOD_ID + "_rafts";
        private final List<Entry> entries = new ArrayList<>();

        public record Entry(UUID owner, int x, int y, int z) {
        }

        public static RaftRegistry get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(RaftRegistry::load, RaftRegistry::new, DATA_NAME);
        }

        public Entry getEntry(UUID owner) {
            for (Entry e : entries) {
                if (e.owner.equals(owner)) {
                    return e;
                }
            }
            return null;
        }

        public void register(UUID owner, BlockPos pos) {
            entries.removeIf(e -> e.owner.equals(owner));
            entries.add(new Entry(owner, pos.getX(), pos.getY(), pos.getZ()));
            setDirty();
        }

        public boolean isFree(BlockPos candidate, UUID self) {
            long minSq = (long) RAFT_SPACING * RAFT_SPACING;
            for (Entry e : entries) {
                if (e.owner.equals(self)) {
                    continue;
                }
                long dx = (long) candidate.getX() - e.x;
                long dz = (long) candidate.getZ() - e.z;
                if (dx * dx + dz * dz < minSq) {
                    return false;
                }
            }
            return true;
        }

        public boolean isFreeFromAny(BlockPos pos) {
            return ownerAt(pos) == null;
        }

        public UUID ownerAt(BlockPos pos) {
            if (pos == null) {
                return null;
            }
            long minSq = (long) CLAIM_RADIUS * CLAIM_RADIUS;
            for (Entry e : entries) {
                long dx = (long) pos.getX() - e.x;
                long dz = (long) pos.getZ() - e.z;
                if (dx * dx + dz * dz <= minSq) {
                    return e.owner;
                }
            }
            return null;
        }

        public static RaftRegistry load(CompoundTag tag) {
            RaftRegistry reg = new RaftRegistry();
            ListTag list = tag.getList("rafts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                try {
                    UUID id = t.getUUID("owner");
                    reg.entries.add(new Entry(id, t.getInt("x"), t.getInt("y"), t.getInt("z")));
                } catch (Exception ignored) {
                }
            }
            return reg;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag list = new ListTag();
            for (Entry e : entries) {
                CompoundTag t = new CompoundTag();
                t.putUUID("owner", e.owner);
                t.putInt("x", e.x);
                t.putInt("y", e.y);
                t.putInt("z", e.z);
                list.add(t);
            }
            tag.put("rafts", list);
            return tag;
        }
    }
}
