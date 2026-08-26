package net.aquatech.ui.skyblock;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

/**
 * Resolves the owner key for a WorldGuard {@code island_*} / {@code *_raft} region at a block.
 */
public final class WorldGuardIslandLookup {

    private WorldGuardIslandLookup() {
    }

    public static UUID ownerAt(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        if (!level.dimension().equals(ServerLevel.OVERWORLD)) {
            return null;
        }
        try {
            return lookup(pos);
        } catch (Throwable t) {
            AquaTechUI.LOGGER.debug("WorldGuard island lookup failed: {}", t.toString());
            return null;
        }
    }

    static boolean isIslandRegionId(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        String lower = id.toLowerCase(Locale.ROOT);
        return lower.startsWith("island_") || lower.startsWith("is_") || lower.endsWith("_raft");
    }

    private static UUID lookup(BlockPos pos) throws Exception {
        ClassLoader cl = pluginClassLoader();
        Object manager = regionManager(cl);
        if (manager == null) {
            return null;
        }
        Class<?> blockVector3 = Class.forName("com.sk89q.worldedit.math.BlockVector3", true, cl);
        Object vec = blockVector3.getMethod("at", int.class, int.class, int.class)
                .invoke(null, pos.getX(), pos.getY(), pos.getZ());
        Method getApplicable = findMethod(manager.getClass(), "getApplicableRegions", 1);
        if (getApplicable == null) {
            return null;
        }
        Object set = getApplicable.invoke(manager, vec);
        if (!(set instanceof Iterable<?> regions)) {
            return null;
        }

        int bestPri = Integer.MIN_VALUE;
        UUID best = null;
        for (Object region : regions) {
            if (region == null) {
                continue;
            }
            String id = String.valueOf(region.getClass().getMethod("getId").invoke(region));
            if (!isIslandRegionId(id)) {
                continue;
            }
            int pri = 0;
            try {
                Object p = region.getClass().getMethod("getPriority").invoke(region);
                if (p instanceof Number n) {
                    pri = n.intValue();
                }
            } catch (NoSuchMethodException ignored) {
            }
            if (best != null && pri < bestPri) {
                continue;
            }
            bestPri = pri;
            best = ownerKey(region, id);
        }
        return best;
    }

    private static UUID ownerKey(Object region, String regionId) throws Exception {
        Object owners = region.getClass().getMethod("getOwners").invoke(region);
        try {
            Object ids = owners.getClass().getMethod("getUniqueIds").invoke(owners);
            if (ids instanceof Collection<?> col) {
                for (Object id : col) {
                    if (id instanceof UUID uuid) {
                        return uuid;
                    }
                }
            }
        } catch (NoSuchMethodException ignored) {
        }
        return UUID.nameUUIDFromBytes(("wg-island:" + regionId.toLowerCase(Locale.ROOT))
                .getBytes(StandardCharsets.UTF_8));
    }

    private static ClassLoader pluginClassLoader() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object pm = bukkitClass.getMethod("getPluginManager").invoke(null);
            Object wgPlugin = pm.getClass().getMethod("getPlugin", String.class).invoke(pm, "WorldGuard");
            if (wgPlugin != null) {
                return wgPlugin.getClass().getClassLoader();
            }
        } catch (Throwable ignored) {
        }
        return WorldGuardIslandLookup.class.getClassLoader();
    }

    private static Object regionManager(ClassLoader cl) throws Exception {
        Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
        Object bukkitWorld = bukkitClass.getMethod("getWorld", String.class).invoke(null, "world");
        if (bukkitWorld == null) {
            return null;
        }
        Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard", true, cl);
        Object wg = wgClass.getMethod("getInstance").invoke(null);
        Object platform = wg.getClass().getMethod("getPlatform").invoke(wg);
        Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
        Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter", true, cl);
        Object weWorld = bukkitAdapter.getMethod("adapt", Class.forName("org.bukkit.World"))
                .invoke(null, bukkitWorld);
        Method get = findMethod(container.getClass(), "get", 1);
        if (get == null) {
            return null;
        }
        return get.invoke(container, weWorld);
    }

    private static Method findMethod(Class<?> clazz, String name, int paramCount) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                return m;
            }
        }
        return null;
    }
}
