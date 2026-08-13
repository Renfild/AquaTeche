package net.aquatech.ui.server.bukkit;

import net.aquatech.ui.AquaTechUI;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File-based LuckPerms reader for Mohist (Forge cannot see Bukkit LP API).
 * Also loads aquatech_rank_glyphs.properties for rank PUA glyph chars (resource-pack / mod font).
 */
public final class LuckPermsFiles {
    private static final Pattern PRIMARY_GROUP = Pattern.compile("(?m)^primary-group:\\s*(\\S+)");
    private static final Pattern PARENTS_GROUP = Pattern.compile("(?m)^-\\s+group\\.(\\S+)");
    private static final Pattern PREFIX_LINE = Pattern.compile("^\\s*-\\s*[\"']?(.+?)[\"']?:\\s*$");
    private static final Pattern PRIORITY = Pattern.compile("(?m)^\\s*priority:\\s*(\\d+)");

    private static final Map<String, String> GLYPH_BY_GROUP = new ConcurrentHashMap<>();
    private static volatile long glyphsLoadedAt;
    private static volatile long groupsLoadedAt;
    private static Map<String, String> groupPrefixCache = Collections.emptyMap();

    private LuckPermsFiles() {
    }

    public static String glyphForGroup(String groupId) {
        ensureGlyphs();
        if (groupId == null) return "";
        return GLYPH_BY_GROUP.getOrDefault(groupId.toLowerCase(Locale.ROOT), "");
    }

    public static String primaryGroup(UUID uuid) {
        Path user = lpRoot().resolve("yaml-storage").resolve("users").resolve(uuid + ".yml");
        if (!Files.isRegularFile(user)) return null;
        try {
            String text = Files.readString(user, StandardCharsets.UTF_8);
            Matcher m = PRIMARY_GROUP.matcher(text);
            if (m.find()) return m.group(1).toLowerCase(Locale.ROOT);
            // fallback: first parent group node
            Matcher p = PARENTS_GROUP.matcher(text);
            if (p.find()) return p.group(1).toLowerCase(Locale.ROOT);
        } catch (IOException ignored) {
        }
        return null;
    }

    public static String prefixForGroup(String groupId) {
        ensureGroupPrefixes();
        if (groupId == null) return "";
        return groupPrefixCache.getOrDefault(groupId.toLowerCase(Locale.ROOT), "");
    }

    public static String displayFor(UUID uuid, String fallbackGroup) {
        String group = primaryGroup(uuid);
        if (group == null || group.isBlank()) group = fallbackGroup;
        if (group == null) group = "default";
        group = group.toLowerCase(Locale.ROOT);

        String glyph = glyphForGroup(group);
        String prefix = prefixForGroup(group);
        if (!glyph.isEmpty()) {
            // Prefer mapped rank glyph (stable) over raw LP prefix text
            return glyph + " ";
        }
        if (prefix != null && !prefix.isBlank()) {
            return prefix;
        }
        return "";
    }

    private static void ensureGlyphs() {
        long now = System.currentTimeMillis();
        if (now - glyphsLoadedAt < 5000L && !GLYPH_BY_GROUP.isEmpty()) return;
        glyphsLoadedAt = now;
        Path props = configRoot().resolve("aquatech_rank_glyphs.properties");
        if (!Files.isRegularFile(props)) {
            // also try server/config relative to game dir parent
            props = FMLPaths.GAMEDIR.get().resolve("config").resolve("aquatech_rank_glyphs.properties");
        }
        if (!Files.isRegularFile(props)) return;
        try (BufferedReader br = Files.newBufferedReader(props, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String g = line.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String v = line.substring(eq + 1);
                if (!v.isEmpty()) GLYPH_BY_GROUP.put(g, v.substring(0, 1));
            }
        } catch (IOException e) {
            AquaTechUI.LOGGER.warn("[AquaTech] Failed reading rank glyphs: {}", e.toString());
        }
    }

    private static void ensureGroupPrefixes() {
        long now = System.currentTimeMillis();
        if (now - groupsLoadedAt < 5000L && !groupPrefixCache.isEmpty()) return;
        groupsLoadedAt = now;
        Path dir = lpRoot().resolve("yaml-storage").resolve("groups");
        if (!Files.isDirectory(dir)) return;
        Map<String, String> map = new HashMap<>();
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".yml")).forEach(p -> {
                try {
                    String text = Files.readString(p, StandardCharsets.UTF_8);
                    String name = p.getFileName().toString().replace(".yml", "").toLowerCase(Locale.ROOT);
                    String best = pickBestPrefix(text);
                    if (best != null) map.put(name, best);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
        groupPrefixCache = map;
    }

    private static String pickBestPrefix(String yaml) {
        String[] lines = yaml.split("\\R");
        boolean inPrefixes = false;
        String best = null;
        int bestPri = Integer.MIN_VALUE;
        String pending = null;
        for (String line : lines) {
            if (line.startsWith("prefixes:")) {
                inPrefixes = true;
                continue;
            }
            if (inPrefixes && line.matches("^[a-zA-Z].*") && !line.startsWith(" ")) {
                break;
            }
            if (!inPrefixes) continue;
            Matcher pm = PREFIX_LINE.matcher(line);
            if (pm.find()) {
                pending = pm.group(1).replace("\\\"", "\"").replace("\\'", "'");
                continue;
            }
            if (pending != null) {
                Matcher pri = PRIORITY.matcher(line);
                if (pri.find()) {
                    int p = Integer.parseInt(pri.group(1));
                    if (p >= bestPri) {
                        bestPri = p;
                        best = pending;
                    }
                    pending = null;
                }
            }
        }
        return best;
    }

    private static Path lpRoot() {
        // Mohist: plugins/LuckPerms next to server jar (gamedir)
        Path gamedir = FMLPaths.GAMEDIR.get();
        Path a = gamedir.resolve("plugins").resolve("LuckPerms");
        if (Files.isDirectory(a)) return a;
        return gamedir.resolve("server").resolve("plugins").resolve("LuckPerms");
    }

    private static Path configRoot() {
        Path gamedir = FMLPaths.GAMEDIR.get();
        Path a = gamedir.resolve("config");
        if (Files.isDirectory(a)) return a;
        return gamedir.resolve("server").resolve("config");
    }
}
