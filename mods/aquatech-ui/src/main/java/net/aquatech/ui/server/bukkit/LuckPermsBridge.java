package net.aquatech.ui.server.bukkit;

import net.aquatech.ui.capability.AquaSkillCapability;
import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.horizon.HorizonRoute;
import net.aquatech.ui.server.PressureBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves player rank for TAB from LuckPerms (primary group + prefix meta).
 * Soft-depends via reflection — works on Mohist/Forge with or without LP.
 */
public final class LuckPermsBridge {
    private static final Map<UUID, CachedRank> CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_MS = 2500L;

    private static boolean reflectReady;
    private static boolean reflectFailed;
    private static Method lpProviderGet;
    private static Method queryNonContextual;
    private static Method legacySerializerDeserialize;
    private static Method latencyMethod;
    private static Field latencyField;
    private static boolean latencyResolved;

    private LuckPermsBridge() {
    }

    public record RankInfo(String id, String display, int weight, boolean staff) {
    }

    private record CachedRank(RankInfo info, long at) {
    }

    public static PlayerProfile fromServerPlayer(ServerPlayer player) {
        String name = player.getGameProfile().getName();
        UUID uuid = player.getUUID();
        int ping = readLatency(player);
        RankInfo rank = resolveRank(player);
        PressureBridge.PressureInfo pressure = PressureBridge.fromPlayer(player);
        return new PlayerProfile(
                uuid, name,
                rank.id(), rank.display(), rank.weight(),
                ping, rank.staff(), false,
                pressure.inWater(), pressure.depth(), pressure.effective()
        );
    }

    public static void invalidatePlayer(UUID uuid) {
        CACHE.remove(uuid);
    }

    public static void invalidateAllPlayers() {
        CACHE.clear();
    }

    private static RankInfo resolveRank(ServerPlayer player) {
        UUID uuid = player.getUUID();
        CachedRank cached = CACHE.get(uuid);
        long now = System.currentTimeMillis();
        if (cached != null && now - cached.at < CACHE_MS) {
            return cached.info();
        }

        RankInfo lp = tryLuckPerms(player);
        if (lp == null) {
            lp = tryLuckPermsFiles(player);
        }
        RankInfo result;
        if (lp != null && !"default".equals(lp.id())) {
            result = lp;
        } else {
            // Horizon fleet tier as soft fallback when LP says default
            RankInfo horizon = fromHorizon(player);
            if (horizon != null) {
                result = horizon;
            } else if (lp != null) {
                result = lp;
            } else {
                result = fromOpLevel(player);
            }
        }
        // Attach rank glyph from aquatech_rank_glyphs.properties when LP prefix is plain text
        result = withGlyph(result);
        CACHE.put(uuid, new CachedRank(result, now));
        return result;
    }

    private static RankInfo withGlyph(RankInfo info) {
        if (info == null) return null;
        String glyph = LuckPermsFiles.glyphForGroup(info.id());
        if (glyph == null || glyph.isBlank()) return info;
        String display = info.display();
        // If display already starts with this glyph, keep; else prefix glyph
        if (!display.isEmpty() && display.codePointAt(0) == glyph.codePointAt(0)) {
            return info;
        }
        // Strip old text-only prefix leftovers, show glyph (+ keep short text if no glyph-only)
        String cleaned = stripCodes(display).trim();
        if (cleaned.equalsIgnoreCase(info.id()) || cleaned.isBlank()) {
            return new RankInfo(info.id(), glyph + " ", info.weight(), info.staff());
        }
        return new RankInfo(info.id(), glyph + " " + cleaned, info.weight(), info.staff());
    }

    private static RankInfo tryLuckPermsFiles(ServerPlayer player) {
        try {
            String group = LuckPermsFiles.primaryGroup(player.getUUID());
            if (group == null || group.isBlank()) group = "default";
            group = group.toLowerCase(Locale.ROOT);
            String prefix = LuckPermsFiles.prefixForGroup(group);
            String display = prefix;
            if (display == null || display.isBlank()) {
                display = LuckPermsFiles.displayFor(player.getUUID(), group);
            }
            display = preserveGlyphsStripJunk(display);
            if (display.isBlank()) display = pretty(group);
            int weight = weightFor(group);
            boolean staff = weight >= weightFor("mod")
                    || "owner".equals(group) || "admin".equals(group)
                    || "mod".equals(group) || "developer".equals(group)
                    || "staff".equals(group) || "helper".equals(group);
            return new RankInfo(group, display, weight, staff);
        } catch (Throwable t) {
            return null;
        }
    }

    private static RankInfo fromHorizon(ServerPlayer player) {
        // LazyOptional.map() NPE's if the mapper returns null — never return null from map().
        return player.getCapability(AquaSkillCapability.INSTANCE)
                .filter(cap -> cap.getHorizonTier() > 0)
                .map(cap -> {
                    int tier = cap.getHorizonTier();
                    String group = HorizonRoute.lpGroup(tier);
                    String display = HorizonRoute.tierName(tier);
                    return new RankInfo(group, display, weightFor(group), false);
                })
                .orElse(null);
    }

    private static RankInfo fromOpLevel(ServerPlayer player) {
        if (player.hasPermissions(4)) return new RankInfo("owner", "Владелец", weightFor("owner"), true);
        if (player.hasPermissions(3)) return new RankInfo("admin", "Админ", weightFor("admin"), true);
        if (player.hasPermissions(2)) return new RankInfo("mod", "Модератор", weightFor("mod"), true);
        if (player.hasPermissions(1)) return new RankInfo("vip", "VIP", weightFor("vip"), false);
        return new RankInfo("default", "Игрок", weightFor("default"), false);
    }

    private static RankInfo tryLuckPerms(ServerPlayer player) {
        try {
            if (!ensureReflect()) return null;
            Object api = lpProviderGet.invoke(null);
            if (api == null) return null;

            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = userManager.getClass().getMethod("getUser", UUID.class)
                    .invoke(userManager, player.getUUID());
            if (user == null) return null;

            String primary = (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
            if (primary == null || primary.isBlank()) primary = "default";
            primary = primary.toLowerCase(Locale.ROOT);

            String display = primary;
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Class<?> queryClass = Class.forName("net.luckperms.api.query.QueryOptions");
            Object queryOpts = queryNonContextual.invoke(null);
            Object metaData = cachedData.getClass()
                    .getMethod("getMetaData", queryClass)
                    .invoke(cachedData, queryOpts);

            Object prefixObj = metaData.getClass().getMethod("getPrefix").invoke(metaData);
            if (prefixObj != null) {
                String prefix = preserveGlyphsStripJunk(String.valueOf(prefixObj));
                if (!prefix.isBlank()) {
                    display = prefix.trim();
                }
            }

            // Prefer group display name if prefix empty
            if (display.equals(primary)) {
                try {
                    Object groupManager = api.getClass().getMethod("getGroupManager").invoke(api);
                    Object group = groupManager.getClass().getMethod("getGroup", String.class)
                            .invoke(groupManager, primary);
                    if (group != null) {
                        Object displayName = group.getClass().getMethod("getDisplayName").invoke(group);
                        if (displayName != null && !String.valueOf(displayName).isBlank()) {
                            display = preserveGlyphsStripJunk(String.valueOf(displayName));
                        }
                    }
                } catch (Throwable ignored) {
                }
            }

            display = preserveGlyphsStripJunk(display);
            if (display.isBlank()) display = pretty(primary);

            int weight = weightFor(primary);
            // Also consider highest weighted inherited group
            try {
                @SuppressWarnings("unchecked")
                java.util.Collection<String> groups = (java.util.Collection<String>)
                        user.getClass().getMethod("getInheritedGroups", queryOpts.getClass())
                                .invoke(user, queryOpts);
                // getInheritedGroups returns Collection<Group> in LP API
            } catch (Throwable ignored) {
            }
            try {
                Object nodes = user.getClass().getMethod("getNodes").invoke(user);
                if (nodes instanceof Iterable<?> it) {
                    for (Object node : it) {
                        String key = String.valueOf(node.getClass().getMethod("getKey").invoke(node));
                        if (key.startsWith("group.")) {
                            String g = key.substring(6).toLowerCase(Locale.ROOT);
                            int w = weightFor(g);
                            if (w > weight) {
                                weight = w;
                                // Keep primary id for color, but bump weight for sort
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
            }

            boolean staff = weight >= weightFor("mod")
                    || "owner".equals(primary) || "admin".equals(primary)
                    || "mod".equals(primary) || "developer".equals(primary)
                    || "moderator".equals(primary);

            return new RankInfo(primary, display, weight, staff);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean ensureReflect() {
        if (reflectReady) return true;
        if (reflectFailed) return false;
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            lpProviderGet = provider.getMethod("get");
            Class<?> query = Class.forName("net.luckperms.api.query.QueryOptions");
            queryNonContextual = query.getMethod("nonContextual");
            try {
                Class<?> legacy = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
                Object serializer = legacy.getMethod("legacySection").invoke(null);
                legacySerializerDeserialize = serializer.getClass().getMethod("deserialize", String.class);
            } catch (Throwable ignored) {
            }
            reflectReady = true;
            return true;
        } catch (Throwable t) {
            reflectFailed = true;
            return false;
        }
    }

    public static int weightFor(String groupId) {
        String id = groupId == null ? "default" : groupId.toLowerCase(Locale.ROOT);
        for (String entry : ModConfig.RANK_WEIGHTS.get()) {
            int eq = entry.indexOf('=');
            if (eq <= 0) continue;
            String g = entry.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            if (!g.equals(id)) continue;
            try {
                return Integer.parseInt(entry.substring(eq + 1).trim());
            } catch (NumberFormatException ignored) {
                return 10;
            }
        }
        return "default".equals(id) ? 10 : 15;
    }

    private static String pretty(String id) {
        if (id == null || id.isBlank()) return "Игрок";
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    private static String stripAdventure(String s) {
        return preserveGlyphsStripJunk(s);
    }

    private static String stripCodes(String s) {
        return preserveGlyphsStripJunk(s);
    }

    /**
     * Strip Mojang/legacy color codes and MiniMessage tags, but keep Oraxen PUA glyphs (U+E000–U+F8FF).
     */
    private static String preserveGlyphsStripJunk(String s) {
        if (s == null) return "";
        StringBuilder keptGlyphs = new StringBuilder();
        StringBuilder rest = new StringBuilder();
        s.codePoints().forEach(cp -> {
            if (cp >= 0xE000 && cp <= 0xF8FF) {
                keptGlyphs.appendCodePoint(cp);
            } else {
                rest.appendCodePoint(cp);
            }
        });
        String text = rest.toString();
        text = text.replaceAll("(?i)[§&][0-9A-FK-OR]", "");
        // Remove mini-message / glyph tags but we already extracted PUA chars
        text = text.replaceAll("<[^>]+>", "");
        text = text.trim();
        if (keptGlyphs.length() > 0) {
            return text.isEmpty() ? keptGlyphs + " " : keptGlyphs + " " + text;
        }
        return text;
    }

    private static int readLatency(ServerPlayer player) {
        try {
            resolveLatencyAccess(player);
            if (latencyMethod != null) {
                Object v = latencyMethod.invoke(player.connection);
                if (v instanceof Integer i) return i;
            }
            if (latencyField != null) {
                return latencyField.getInt(player.connection);
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private static void resolveLatencyAccess(ServerPlayer player) {
        if (latencyResolved) return;
        latencyResolved = true;
        ServerGamePacketListenerImpl conn = player.connection;
        try {
            latencyMethod = conn.getClass().getMethod("getLatency");
            return;
        } catch (NoSuchMethodException ignored) {
        }
        Class<?> c = conn.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField("latency");
                f.setAccessible(true);
                latencyField = f;
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
    }
}
