package store.aquateche.aqualumen.common.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import store.aquateche.aqualumen.AquaLumenUI;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configurable kits for F4 menu with Forge-native item support and cooldowns.
 */
public final class KitConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/kits.json");
    private static final Path COOLDOWNS_FILE = FMLPaths.CONFIGDIR.get().resolve("aqualumen/kit_cooldowns.json");

    private static Data cached;
    private static long cachedMtime;
    private static final Map<String, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static boolean cooldownsLoaded = false;

    private KitConfig() {
    }

    private static void loadCooldowns() {
        if (cooldownsLoaded) return;
        cooldownsLoaded = true;
        try {
            if (Files.exists(COOLDOWNS_FILE)) {
                Type type = new TypeToken<Map<String, Long>>() {}.getType();
                Map<String, Long> map = GSON.fromJson(Files.readString(COOLDOWNS_FILE, StandardCharsets.UTF_8), type);
                if (map != null) {
                    COOLDOWNS.putAll(map);
                }
            }
        } catch (Throwable e) {
            AquaLumenUI.LOGGER.warn("Could not load kit cooldowns: {}", e.toString());
        }
    }

    private static void saveCooldowns() {
        try {
            Files.createDirectories(COOLDOWNS_FILE.getParent());
            Files.writeString(COOLDOWNS_FILE, GSON.toJson(COOLDOWNS), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {
        }
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
            AquaLumenUI.LOGGER.warn("Kit config unreadable, using defaults: {}", error.toString());
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
            AquaLumenUI.LOGGER.error("Failed to save kits.json", e);
            return false;
        }
    }

    public static synchronized boolean addOrUpdate(KitDef def) {
        if (def == null || def.id == null || def.id.isBlank()) return false;
        Data data = get();
        data.kits.removeIf(k -> k.id.equalsIgnoreCase(def.id));
        data.kits.add(def);
        return save(data);
    }

    public static synchronized boolean remove(String id) {
        if (id == null || id.isBlank()) return false;
        Data data = get();
        boolean removed = data.kits.removeIf(k -> k.id.equalsIgnoreCase(id));
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

    public static KitDef find(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (KitDef def : get().kits) {
            if (id.equalsIgnoreCase(def.id)) {
                return def;
            }
        }
        return null;
    }

    public static boolean grantKit(ServerPlayer player, String kitId) {
        if (player == null || kitId == null) return false;
        KitDef kit = find(kitId);
        if (kit == null) {
            player.sendSystemMessage(Component.literal("\u00a7c[AquaTech] \u041d\u0430\u0431\u043e\u0440 \u00ab" + kitId + "\u00bb \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d."));
            return false;
        }

        loadCooldowns();
        String cdKey = player.getUUID() + "_" + kit.id.toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        long nextAvailable = COOLDOWNS.getOrDefault(cdKey, 0L);

        if (now < nextAvailable) {
            long remainingSec = (nextAvailable - now) / 1000L;
            long hours = remainingSec / 3600L;
            long mins = (remainingSec % 3600L) / 60L;
            long secs = remainingSec % 60L;
            String timeStr = hours > 0 ? hours + " \u0447. " + mins + " \u043c\u0438\u043d." : (mins > 0 ? mins + " \u043c\u0438\u043d. " + secs + " \u0441\u0435\u043a." : secs + " \u0441\u0435\u043a.");
            player.sendSystemMessage(Component.literal("\u00a7e[AquaTech] \u00a7c\u0412\u044b \u0443\u0436\u0435 \u0431\u0440\u0430\u043b\u0438 \u044d\u0442\u043e\u0442 \u043d\u0430\u0431\u043e\u0440! \u0414\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0447\u0435\u0440\u0435\u0437: \u00a7e" + timeStr));
            return false;
        }

        int itemsGiven = 0;
        if (kit.items != null && !kit.items.isEmpty()) {
            for (KitItem ki : kit.items) {
                if (ki.item == null || ki.item.isBlank()) continue;
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(ki.item));
                if (item == null || BuiltInRegistries.ITEM.getKey(item) == null) continue;
                ItemStack stack = new ItemStack(item, Math.max(1, ki.count));
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                itemsGiven += Math.max(1, ki.count);
            }
        }

        if (kit.commands != null) {
            for (String cmd : kit.commands) {
                if (cmd == null || cmd.isBlank()) continue;
                String finalCmd = cmd.replace("%player%", player.getGameProfile().getName());
                player.server.getCommands().performPrefixedCommand(player.server.createCommandSourceStack(), finalCmd);
            }
        }

        if (kit.cooldownSeconds > 0) {
            COOLDOWNS.put(cdKey, now + (kit.cooldownSeconds * 1000L));
            saveCooldowns();
        }

        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.0F);
        player.sendSystemMessage(Component.literal("\u00a7a[AquaTech] \u00a7f\u041d\u0430\u0431\u043e\u0440 \u00a7e\u00ab" + kit.title + "\u00bb \u00a7f\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u043e\u043b\u0443\u0447\u0435\u043d!"));
        return true;
    }

    public static final class Data {
        public List<KitDef> kits = new ArrayList<>();
    }

    public static final class KitDef {
        public String id;
        public String title;
        public String description;
        public String badge;
        public String command;
        public long cooldownSeconds;
        public List<KitItem> items = new ArrayList<>();
        public List<String> commands = new ArrayList<>();

        public KitDef() {
        }

        public KitDef(String id, String title, String description, String badge, String command) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.badge = badge;
            this.command = command;
            this.cooldownSeconds = 0L;
            this.items = new ArrayList<>();
            this.commands = command != null && !command.isBlank() ? List.of(command) : new ArrayList<>();
        }

        public KitDef(String id, String title, String description, String badge, long cooldownSeconds, List<KitItem> items, List<String> commands) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.badge = badge;
            this.command = commands != null && !commands.isEmpty() ? commands.get(0) : "kit " + id;
            this.cooldownSeconds = cooldownSeconds;
            this.items = items != null ? items : new ArrayList<>();
            this.commands = commands != null ? commands : new ArrayList<>();
        }
    }

    public static final class KitItem {
        public String item;
        public int count;

        public KitItem() {
        }

        public KitItem(String item, int count) {
            this.item = item;
            this.count = count;
        }
    }

    private static Data defaults() {
        Data d = new Data();
        List<KitItem> startItems = new ArrayList<>();
        startItems.add(new KitItem("starcatcher:bamboo_rod", 1));
        startItems.add(new KitItem("aquatech_ui:rate_x2", 1));
        startItems.add(new KitItem("starcatcher:tackle_box", 1));
        startItems.add(new KitItem("minecraft:ladder", 16));
        startItems.add(new KitItem("minecraft:bread", 8));
        d.kits.add(new KitDef("start", "\u0421\u0442\u0430\u0440\u0442 (Start)",
                "\u0411\u0430\u043c\u0431\u0443\u043a\u043e\u0432\u0430\u044f \u0443\u0434\u043e\u0447\u043a\u0430, \u043c\u043d\u043e\u0436\u0438\u0442\u0435\u043b\u044c \u0443\u0434\u0430\u0447\u0438, \u044f\u0449\u0438\u043a \u0441\u043d\u0430\u0441\u0442\u0435\u0439, 16 \u043b\u0435\u0441\u0442\u043d\u0438\u0446, 8 \u0431\u0430\u0442\u043e\u043d\u0430",
                "24 \u0447", 86400L, startItems, List.of()));
        List<KitItem> starterItems = new ArrayList<>();
        starterItems.add(new KitItem("minecraft:oak_chest_boat", 1));
        starterItems.add(new KitItem("starcatcher:bamboo_rod", 1));
        starterItems.add(new KitItem("minecraft:bread", 12));
        starterItems.add(new KitItem("minecraft:oak_sapling", 4));
        starterItems.add(new KitItem("minecraft:bone_meal", 8));
        starterItems.add(new KitItem("minecraft:torch", 16));
        starterItems.add(new KitItem("ftbquests:book", 1));
        d.kits.add(new KitDef("starter", "\u0412\u044b\u0436\u0438\u0432\u0430\u043d\u0438\u0435 (Starter)",
                "\u041b\u043e\u0434\u043a\u0430, \u0445\u043b\u0435\u0431, \u0441\u0430\u0436\u0435\u043d\u0446\u044b, \u0444\u0430\u043a\u0435\u043b\u044b, \u043a\u0432\u0435\u0441\u0442\u0431\u0443\u043a",
                "24 \u0447", 86400L, starterItems, List.of()));
        List<KitItem> diverItems = new ArrayList<>();
        diverItems.add(new KitItem("starcatcher:aquamarine_pike", 1));
        diverItems.add(new KitItem("starcatcher:fisherman_hat_cyan", 1));
        diverItems.add(new KitItem("minecraft:cooked_salmon", 16));
        d.kits.add(new KitDef("diver", "\u0414\u0430\u0439\u0432\u0435\u0440 (Diver)",
                "\u0420\u0435\u0434\u043a\u0430\u044f \u0440\u044b\u0431\u0430, \u0448\u043b\u044f\u043f\u0430 \u0440\u044b\u0431\u0430\u043a\u0430 \u0438 \u043f\u0440\u0438\u043f\u0430\u0441\u044b",
                "\u0420\u0430\u043d\u0433 \u0414\u0430\u0439\u0432\u0435\u0440", 86400L, diverItems, List.of()));

        List<KitItem> vipItems = new ArrayList<>();
        vipItems.add(new KitItem("starcatcher:sharktooth_rod", 1));
        vipItems.add(new KitItem("aquatech_ui:rate_x4", 1));
        vipItems.add(new KitItem("starcatcher:tackle_box_purple", 1));
        vipItems.add(new KitItem("minecraft:golden_apple", 4));
        vipItems.add(new KitItem("minecraft:emerald", 32));
        d.kits.add(new KitDef("vip", "\u041f\u0440\u0435\u043c\u0438\u0443\u043c (VIP)",
                "\u0420\u0435\u0441\u0443\u0440\u0441\u043d\u044b\u0435 \u0443\u0434\u043e\u0447\u043a\u0438, \u044f\u0449\u0438\u043a \u0441\u043d\u0430\u0441\u0442\u0435\u0439, \u043c\u043e\u043d\u0435\u0442\u044b \u0438 \u044f\u0431\u043b\u043e\u043a\u0438",
                "VIP \u0441\u0442\u0430\u0442\u0443\u0441", 86400L, vipItems, List.of()));
        return d;
    }
}
