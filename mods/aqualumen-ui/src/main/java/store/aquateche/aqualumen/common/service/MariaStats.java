package store.aquateche.aqualumen.common.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import store.aquateche.aqualumen.AquaLumenUI;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Mirrors hub coins/fish onto Apex MariaDB ({@code aquatech_player_stats}).
 * Driver is stolen from LuckPerms/ajLeaderboards when Forge has no JDBC jar.
 */
public final class MariaStats {

    private static final String[] DRIVER_NAMES = {
            "org.mariadb.jdbc.Driver",
            "com.mysql.cj.jdbc.Driver",
            "com.mysql.jdbc.Driver"
    };
    private static final String[] PLUGIN_LOADERS = {"LuckPerms", "ajLeaderboards", "SkinsRestorer"};

    private static volatile Driver driver;
    private static volatile JsonObject creds;
    private static volatile boolean credsLoaded;
    private static volatile boolean schemaReady;
    private static volatile boolean loggedSkip;

    private MariaStats() {
    }

    public static void upsert(UUID uuid, String nick, long coins, int fish, long playtimeHours,
                              int quests, String privilege) {
        JsonObject cfg = creds();
        Driver jdbc = driver();
        if (cfg == null || jdbc == null) {
            if (!loggedSkip) {
                loggedSkip = true;
                AquaLumenUI.LOGGER.debug("MariaDB stats skip: no config/aquatech_mysql.json or JDBC driver");
            }
            return;
        }
        String jdbcUrl = "jdbc:mysql://" + cfg.get("host").getAsString() + ":"
                + cfg.get("port").getAsInt() + "/" + cfg.get("database").getAsString()
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8";
        Properties props = new Properties();
        props.setProperty("user", cfg.get("username").getAsString());
        props.setProperty("password", cfg.get("password").getAsString());
        try (Connection conn = jdbc.connect(jdbcUrl, props)) {
            if (conn == null) {
                return;
            }
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO aquatech_player_stats"
                            + " (uuid, nick, coins, fish, playtime_hours, quests_done, privilege)"
                            + " VALUES (?,?,?,?,?,?,?)"
                            + " ON DUPLICATE KEY UPDATE nick=VALUES(nick), coins=VALUES(coins),"
                            + " fish=GREATEST(fish, VALUES(fish)),"
                            + " playtime_hours=GREATEST(playtime_hours, VALUES(playtime_hours)),"
                            + " quests_done=VALUES(quests_done), privilege=VALUES(privilege)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, nick);
                ps.setLong(3, coins);
                ps.setLong(4, fish);
                ps.setLong(5, playtimeHours);
                ps.setInt(6, quests);
                ps.setString(7, privilege == null ? "" : privilege);
                ps.executeUpdate();
            }
        } catch (Throwable t) {
            AquaLumenUI.LOGGER.debug("MariaDB stats upsert failed: {}", t.toString());
        }
    }

    public record PlayerRewards(long dailyLastDay, int dailyStreak, Set<Integer> passClaimedTiers) {
        public boolean isTierClaimed(int tier) {
            return passClaimedTiers != null && passClaimedTiers.contains(tier);
        }
    }

    private static final Map<UUID, PlayerRewards> REWARDS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Path REWARDS_DIR = Path.of("config/aqualumen_rewards");

    public static PlayerRewards getRewards(UUID uuid) {
        if (uuid == null) return new PlayerRewards(0, 0, java.util.Collections.emptySet());
        PlayerRewards cached = REWARDS_CACHE.get(uuid);
        if (cached != null) return cached;

        // Try reading local file cache
        PlayerRewards fromFile = loadFromFile(uuid);
        if (fromFile != null) {
            REWARDS_CACHE.put(uuid, fromFile);
            return fromFile;
        }

        PlayerRewards empty = new PlayerRewards(0, 0, new java.util.concurrent.ConcurrentHashMap<Integer, Boolean>().keySet());
        REWARDS_CACHE.put(uuid, empty);
        syncRewardsFromDbAsync(uuid);
        return empty;
    }

    public static void saveDaily(UUID uuid, long day, int streak) {
        if (uuid == null) return;
        PlayerRewards current = getRewards(uuid);
        Set<Integer> tiers = new java.util.HashSet<>(current.passClaimedTiers());
        PlayerRewards updated = new PlayerRewards(day, streak, tiers);
        REWARDS_CACHE.put(uuid, updated);
        saveToFile(uuid, updated);
        persistRewardsAsync(uuid, day, streak, tiers);
    }

    public static void savePassClaimed(UUID uuid, int tier) {
        if (uuid == null) return;
        PlayerRewards current = getRewards(uuid);
        Set<Integer> tiers = new java.util.HashSet<>(current.passClaimedTiers());
        tiers.add(tier);
        PlayerRewards updated = new PlayerRewards(current.dailyLastDay(), current.dailyStreak(), tiers);
        REWARDS_CACHE.put(uuid, updated);
        saveToFile(uuid, updated);
        persistRewardsAsync(uuid, current.dailyLastDay(), current.dailyStreak(), tiers);
    }

    public static void syncRewardsFromDbAsync(UUID uuid) {
        if (uuid == null) return;
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            JsonObject cfg = creds();
            Driver jdbc = driver();
            if (cfg == null || jdbc == null) return;
            String jdbcUrl = "jdbc:mysql://" + cfg.get("host").getAsString() + ":"
                    + cfg.get("port").getAsInt() + "/" + cfg.get("database").getAsString()
                    + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8";
            Properties props = new Properties();
            props.setProperty("user", cfg.get("username").getAsString());
            props.setProperty("password", cfg.get("password").getAsString());
            try (Connection conn = jdbc.connect(jdbcUrl, props)) {
                if (conn == null) return;
                ensureSchema(conn);
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT daily_last_day, daily_streak, pass_claimed FROM aquatech_player_rewards WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            long lastDay = rs.getLong("daily_last_day");
                            int streak = rs.getInt("daily_streak");
                            String rawTiers = rs.getString("pass_claimed");
                            Set<Integer> tiers = parseTiers(rawTiers);
                            PlayerRewards fromDb = new PlayerRewards(lastDay, streak, tiers);
                            REWARDS_CACHE.put(uuid, fromDb);
                            saveToFile(uuid, fromDb);
                        }
                    }
                }
            } catch (Throwable t) {
                AquaLumenUI.LOGGER.debug("MariaDB rewards sync failed for {}: {}", uuid, t.toString());
            }
        });
    }

    private static void persistRewardsAsync(UUID uuid, long dailyLastDay, int dailyStreak, Set<Integer> tiers) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            JsonObject cfg = creds();
            Driver jdbc = driver();
            if (cfg == null || jdbc == null) return;
            String jdbcUrl = "jdbc:mysql://" + cfg.get("host").getAsString() + ":"
                    + cfg.get("port").getAsInt() + "/" + cfg.get("database").getAsString()
                    + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8";
            Properties props = new Properties();
            props.setProperty("user", cfg.get("username").getAsString());
            props.setProperty("password", cfg.get("password").getAsString());
            try (Connection conn = jdbc.connect(jdbcUrl, props)) {
                if (conn == null) return;
                ensureSchema(conn);
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO aquatech_player_rewards (uuid, daily_last_day, daily_streak, pass_claimed)"
                                + " VALUES (?,?,?,?)"
                                + " ON DUPLICATE KEY UPDATE daily_last_day=VALUES(daily_last_day),"
                                + " daily_streak=VALUES(daily_streak), pass_claimed=VALUES(pass_claimed)")) {
                    ps.setString(1, uuid.toString());
                    ps.setLong(2, dailyLastDay);
                    ps.setInt(3, dailyStreak);
                    ps.setString(4, formatTiers(tiers));
                    ps.executeUpdate();
                }
            } catch (Throwable t) {
                AquaLumenUI.LOGGER.debug("MariaDB rewards persist failed for {}: {}", uuid, t.toString());
            }
        });
    }

    private static Set<Integer> parseTiers(String raw) {
        Set<Integer> set = new java.util.HashSet<>();
        if (raw == null || raw.isBlank()) return set;
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    set.add(Integer.parseInt(trimmed));
                } catch (NumberFormatException ignored) {}
            }
        }
        return set;
    }

    private static String formatTiers(Set<Integer> set) {
        if (set == null || set.isEmpty()) return "";
        java.util.List<Integer> list = new java.util.ArrayList<>(set);
        java.util.Collections.sort(list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private static PlayerRewards loadFromFile(UUID uuid) {
        try {
            Path file = REWARDS_DIR.resolve(uuid + ".json");
            if (!Files.isRegularFile(file)) return null;
            String text = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(text).getAsJsonObject();
            long day = obj.has("daily_last_day") ? obj.get("daily_last_day").getAsLong() : 0L;
            int streak = obj.has("daily_streak") ? obj.get("daily_streak").getAsInt() : 0;
            Set<Integer> tiers = new java.util.HashSet<>();
            if (obj.has("pass_claimed") && obj.get("pass_claimed").isJsonArray()) {
                for (var el : obj.getAsJsonArray("pass_claimed")) {
                    tiers.add(el.getAsInt());
                }
            }
            return new PlayerRewards(day, streak, tiers);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void saveToFile(UUID uuid, PlayerRewards rewards) {
        try {
            Files.createDirectories(REWARDS_DIR);
            Path file = REWARDS_DIR.resolve(uuid + ".json");
            JsonObject obj = new JsonObject();
            obj.addProperty("daily_last_day", rewards.dailyLastDay());
            obj.addProperty("daily_streak", rewards.dailyStreak());
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            java.util.List<Integer> sorted = new java.util.ArrayList<>(rewards.passClaimedTiers());
            java.util.Collections.sort(sorted);
            for (int t : sorted) arr.add(t);
            obj.add("pass_claimed", arr);
            Files.writeString(file, obj.toString(), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {}
    }

    private static void ensureSchema(Connection conn) {
        if (schemaReady) {
            return;
        }
        try (var st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS aquatech_player_stats ("
                    + "uuid CHAR(36) NOT NULL PRIMARY KEY,"
                    + "nick VARCHAR(32) NOT NULL,"
                    + "coins BIGINT NOT NULL DEFAULT 0,"
                    + "fish BIGINT NOT NULL DEFAULT 0,"
                    + "playtime_hours BIGINT NOT NULL DEFAULT 0,"
                    + "quests_done INT NOT NULL DEFAULT 0,"
                    + "privilege VARCHAR(64) NOT NULL DEFAULT '',"
                    + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + " ON UPDATE CURRENT_TIMESTAMP,"
                    + "UNIQUE KEY uq_stats_nick (nick)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.execute("CREATE TABLE IF NOT EXISTS aquatech_player_rewards ("
                    + "uuid CHAR(36) NOT NULL PRIMARY KEY,"
                    + "daily_last_day BIGINT NOT NULL DEFAULT 0,"
                    + "daily_streak INT NOT NULL DEFAULT 0,"
                    + "pass_claimed TEXT NOT NULL,"
                    + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + " ON UPDATE CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            schemaReady = true;
        } catch (Throwable t) {
            AquaLumenUI.LOGGER.debug("MariaDB stats schema failed: {}", t.toString());
        }
    }

    private static JsonObject creds() {
        if (credsLoaded) {
            return creds;
        }
        synchronized (MariaStats.class) {
            if (credsLoaded) {
                return creds;
            }
            credsLoaded = true;
            try {
                Path file = Path.of("config/aquatech_mysql.json");
                if (!Files.isRegularFile(file)) {
                    return null;
                }
                JsonObject obj = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8))
                        .getAsJsonObject();
                if (!obj.has("host") || !obj.has("database") || !obj.has("username") || !obj.has("password")) {
                    return null;
                }
                if (!obj.has("port")) {
                    obj.addProperty("port", 3306);
                }
                creds = obj;
            } catch (Throwable ignored) {
                creds = null;
            }
            return creds;
        }
    }

    private static Driver driver() {
        if (driver != null) {
            return driver;
        }
        synchronized (MariaStats.class) {
            if (driver != null) {
                return driver;
            }
            driver = loadDriver(MariaStats.class.getClassLoader());
            if (driver != null) {
                return driver;
            }
            try {
                Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
                Object pm = bukkit.getMethod("getPluginManager").invoke(null);
                for (String pluginName : PLUGIN_LOADERS) {
                    Object plugin = pm.getClass().getMethod("getPlugin", String.class).invoke(pm, pluginName);
                    if (plugin == null) {
                        continue;
                    }
                    driver = loadDriver(plugin.getClass().getClassLoader());
                    if (driver != null) {
                        return driver;
                    }
                }
            } catch (Throwable ignored) {
            }
            return null;
        }
    }

    private static Driver loadDriver(ClassLoader loader) {
        for (String name : DRIVER_NAMES) {
            try {
                Class<?> clazz = Class.forName(name, true, loader);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                if (instance instanceof Driver found) {
                    return found;
                }
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}
