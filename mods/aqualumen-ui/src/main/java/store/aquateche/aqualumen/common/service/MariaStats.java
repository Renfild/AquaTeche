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
import java.util.Properties;
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
