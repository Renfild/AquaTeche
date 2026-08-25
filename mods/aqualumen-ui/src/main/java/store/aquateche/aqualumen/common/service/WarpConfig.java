package store.aquateche.aqualumen.common.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import store.aquateche.aqualumen.AquaLumenUI;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Configurable warps for F4 menu (config/aqualumen/warps.json).
 */
public final class WarpConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/warps.json");

    private static Data cached;
    private static long cachedMtime;

    private WarpConfig() {
    }

    public static synchronized Data get() {
        try {
            if (Files.exists(FILE)) {
                long mtime = Files.getLastModifiedTime(FILE).toMillis();
                if (cached == null || mtime != cachedMtime) {
                    cached = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), Data.class);
                    cachedMtime = mtime;
                }
            } else {
                if (cached == null) {
                    Files.createDirectories(FILE.getParent());
                    Data def = defaults();
                    Files.writeString(FILE, GSON.toJson(def), StandardCharsets.UTF_8);
                    cached = def;
                    cachedMtime = Files.getLastModifiedTime(FILE).toMillis();
                }
            }
        } catch (IOException | RuntimeException error) {
            AquaLumenUI.LOGGER.warn("Warp config unreadable, using defaults: {}", error.toString());
            cached = defaults();
            cachedMtime = 0L;
        }
        return cached;
    }

    public static synchronized boolean save(Data data) {
        if (data == null) return false;
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(data), StandardCharsets.UTF_8);
            cached = data;
            cachedMtime = Files.getLastModifiedTime(FILE).toMillis();
            return true;
        } catch (IOException e) {
            AquaLumenUI.LOGGER.error("Failed to save warps.json", e);
            return false;
        }
    }

    public static synchronized boolean addOrUpdate(WarpDef def) {
        if (def == null || def.id == null || def.id.isBlank()) return false;
        Data data = get();
        data.warps.removeIf(w -> w.id.equalsIgnoreCase(def.id));
        data.warps.add(def);
        return save(data);
    }

    public static synchronized boolean remove(String id) {
        if (id == null || id.isBlank()) return false;
        Data data = get();
        boolean removed = data.warps.removeIf(w -> w.id.equalsIgnoreCase(id));
        if (removed) {
            save(data);
        }
        return removed;
    }

    public static synchronized void reload() {
        cached = null;
        cachedMtime = 0L;
        get();
    }

    public static WarpDef find(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (WarpDef def : get().warps) {
            if (id.equalsIgnoreCase(def.id)) {
                return def;
            }
        }
        return null;
    }

    public static boolean teleport(net.minecraft.server.level.ServerPlayer player, String warpId) {
        if (player == null || warpId == null) return false;
        WarpDef warp = find(warpId);
        String title = warp != null ? warp.title : warpId;
        String cmd = warp != null && warp.command != null && !warp.command.isBlank()
                ? warp.command.replace("{id}", warpId).replace("{player}", player.getGameProfile().getName())
                : "warp " + warpId.trim().replaceAll("[^a-zA-Z0-9_]", "");

        if (warpId.equalsIgnoreCase("spawn") || cmd.equalsIgnoreCase("spawn")) {
            player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "spawn");
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a7a[AquaTech] \u00a7f\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u043d\u0430 \u0442\u043e\u0447\u043a\u0443 \u00a7e\u00ab" + title + "\u00bb\u00a7f..."));
            return true;
        }

        if (warpId.equalsIgnoreCase("home") || warpId.equalsIgnoreCase("island") || cmd.equalsIgnoreCase("home")) {
            player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "home");
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a7a[AquaTech] \u00a7f\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u043d\u0430 \u0442\u043e\u0447\u043a\u0443 \u00a7e\u00ab" + title + "\u00bb\u00a7f..."));
            return true;
        }

        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), cmd);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("\u00a7a[AquaTech] \u00a7f\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u043d\u0430 \u0442\u043e\u0447\u043a\u0443 \u00a7e\u00ab" + title + "\u00bb\u00a7f..."));
        return true;
    }

    public static final class Data {
        public List<WarpDef> warps = new ArrayList<>();
    }

    public static final class WarpDef {
        public String id;
        public String title;
        public String description;
        public String tag;
        public String command;

        public WarpDef() {
        }

        public WarpDef(String id, String title, String description, String tag, String command) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.tag = tag;
            this.command = command;
        }
    }

    private static Data defaults() {
        Data d = new Data();
        d.warps.add(new WarpDef("spawn", "\u0421\u043f\u0430\u0432\u043d",
                "\u0426\u0435\u043d\u0442\u0440\u0430\u043b\u044c\u043d\u044b\u0439 \u043e\u043a\u0435\u0430\u043d\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0445\u0430\u0431, NPC \u0420\u044b\u0431\u0430\u043a, \u043f\u043e\u0440\u0442\u0430\u043b\u044b",
                "SPAWN", "spawn"));
        d.warps.add(new WarpDef("home", "\u041e\u0441\u0442\u0440\u043e\u0432 / \u041f\u043b\u043e\u0442",
                "\u0412\u0430\u0448 \u043b\u0438\u0447\u043d\u044b\u0439 \u043e\u0441\u0442\u0440\u043e\u0432, \u0433\u0435\u043d\u0435\u0440\u0430\u0442\u043e\u0440\u044b \u0438 \u0444\u0435\u0440\u043c\u044b",
                "ISLAND", "home"));
        d.warps.add(new WarpDef("fisher", "\u0420\u044b\u0431\u0430\u043a",
                "\u041f\u0438\u0440\u0441 \u0442\u043e\u0440\u0433\u043e\u0432\u0446\u0430 \u0440\u044b\u0431\u043e\u0439 \u0438 \u043c\u0430\u0433\u0430\u0437\u0438\u043d \u0441\u043d\u0430\u0441\u0442\u0435\u0439",
                "MARKET", "warp fisher"));
        d.warps.add(new WarpDef("abyss", "\u0411\u0435\u0437\u0434\u043d\u0430",
                "\u0413\u043b\u0443\u0431\u043e\u043a\u043e\u0432\u043e\u0434\u043d\u044b\u0439 \u043f\u043e\u0440\u0442\u0430\u043b \u0438 \u0434\u043e\u0431\u044b\u0447\u0430 \u0440\u0443\u0434",
                "ABYSS", "warp abyss"));
        return d;
    }
}
