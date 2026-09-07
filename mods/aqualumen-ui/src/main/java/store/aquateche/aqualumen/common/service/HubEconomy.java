package store.aquateche.aqualumen.common.service;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import store.aquateche.aqualumen.config.LumenConfig;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

/**
 * Coins / gems movement and the daily reward state, kept consistent with what
 * {@link HubDataService} displays: scoreboard objective first, Lightman's coin
 * items in the inventory as the fallback wallet.
 */
public final class HubEconomy {

    private static final String DAILY_TAG = "aqualumen_daily";
    public static final String FISH_OBJECTIVE = "aquatech_fish";

    private HubEconomy() {
    }

    public static long coins(ServerPlayer player) {
        String objective = LumenConfig.COMMON.coinsObjective.get();
        long vault = vaultBalance(player);
        if (vault >= 0L) {
            if (vault != score(player, objective)) {
                setScore(player, objective, vault);
            }
            return vault;
        }
        long board = score(player, objective);
        if (board > 0L) {
            return board;
        }
        // Legacy wallet (old casesmod balance) — import into the scoreboard once,
        // so portal sync and the hub read the same number.
        long legacy = player.getPersistentData().contains("coins")
                ? player.getPersistentData().getLong("coins")
                : 0L;
        if (legacy > 0L) {
            setScore(player, objective, legacy);
            return legacy;
        }
        long inv = inventoryCoins(player);
        return inv > 0L ? inv : 0L;
    }

    public static int fishCaught(ServerPlayer player) {
        int board = (int) Math.min(Integer.MAX_VALUE, score(player, FISH_OBJECTIVE));
        if (board > 0) {
            return board;
        }
        try {
            return player.getStats().getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT));
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static boolean trySpendCoins(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        String objective = LumenConfig.COMMON.coinsObjective.get();
        long vault = vaultBalance(player);
        if (vault >= 0L) {
            if (vault < amount) {
                return false;
            }
            ecoCommand(player, "take", amount);
            long after = vaultBalance(player);
            setScore(player, objective, after >= 0L ? after : Math.max(0L, vault - amount));
            return true;
        }
        if (coins(player) < amount) {
            return false;
        }
        long remaining = amount;
        long board = score(player, objective);
        if (board > 0L) {
            long fromScore = Math.min(board, remaining);
            adjustScore(player, objective, -(int) fromScore);
            remaining -= fromScore;
        }
        if (remaining > 0L) {
            drainInventoryCoins(player, (int) remaining);
        }
        ecoCommand(player, "take", amount);
        return true;
    }

    public static void grantCoins(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return;
        }
        String objective = LumenConfig.COMMON.coinsObjective.get();
        long vault = vaultBalance(player);
        if (vault >= 0L) {
            ecoCommand(player, "give", amount);
            long after = vaultBalance(player);
            setScore(player, objective, after >= 0L ? after : vault + amount);
            return;
        }
        adjustScore(player, objective, (int) Math.min(amount, Integer.MAX_VALUE));
        ecoCommand(player, "give", amount);
    }

    public static ServerPlayer findOnline(MinecraftServer server, String name) {
        if (server == null || name == null || name.isBlank()) {
            return null;
        }
        ServerPlayer exact = server.getPlayerList().getPlayerByName(name);
        if (exact != null) {
            return exact;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getGameProfile().getName().equalsIgnoreCase(name.trim())) {
                return player;
            }
        }
        return null;
    }

    /**
     * Credits {@code name} in the same wallet {@link #trySpendCoins} uses.
     * Online: Vault/scoreboard. Offline: {@code eco give} plus scoreboard by nick.
     */
    public static boolean grantCoinsToName(MinecraftServer server, String name, long amount) {
        if (server == null || name == null || name.isBlank() || amount <= 0L) {
            return false;
        }
        String nick = name.trim();
        ServerPlayer online = findOnline(server, nick);
        if (online != null) {
            grantCoins(online, amount);
            return true;
        }
        ecoCommand(server, nick, "give", amount);
        adjustScoreByName(server, nick, (int) Math.min(amount, Integer.MAX_VALUE));
        return true;
    }

    /**
     * Player-to-player pay on the hub wallet. Returns false when the sender was not charged.
     */
    public static boolean transferCoins(ServerPlayer from, String toName, long amount) {
        if (from == null || from.getServer() == null) {
            return false;
        }
        if (amount <= 0L) {
            from.sendSystemMessage(Component.literal("§eСумма должна быть больше нуля."));
            return false;
        }
        String nick = toName == null ? "" : toName.trim();
        if (nick.isBlank() || nick.length() > 16) {
            from.sendSystemMessage(Component.literal("§eУкажите ник: /pay <ник> <сумма>"));
            return false;
        }
        if (nick.equalsIgnoreCase(from.getGameProfile().getName())) {
            from.sendSystemMessage(Component.literal("§eНельзя перевести монеты самому себе."));
            return false;
        }
        if (!playerKnown(from.getServer(), nick)) {
            from.sendSystemMessage(Component.literal("§cИгрок «" + nick + "» не найден."));
            return false;
        }
        if (!trySpendCoins(from, amount)) {
            from.sendSystemMessage(Component.literal("§cНе хватает монет: нужно " + formatCoins(amount)
                    + " ¤, есть " + formatCoins(coins(from)) + " ¤."));
            return false;
        }
        if (!grantCoinsToName(from.getServer(), nick, amount)) {
            grantCoins(from, amount);
            from.sendSystemMessage(Component.literal("§cНе удалось отправить монеты. Сумма возвращена."));
            return false;
        }
        String pretty = formatCoins(amount);
        from.sendSystemMessage(Component.literal("§aОтправлено " + pretty + " ¤ игроку " + nick + "."));
        ServerPlayer to = findOnline(from.getServer(), nick);
        if (to != null) {
            to.sendSystemMessage(Component.literal("§aПолучено " + pretty + " ¤ от "
                    + from.getGameProfile().getName() + "."));
            HubDataService.push(to);
            HubDataService.syncPlayerToWebAsync(to);
        }
        HubDataService.push(from);
        HubDataService.syncPlayerToWebAsync(from);
        return true;
    }

    public static String formatCoins(long amount) {
        return NumberFormat.getIntegerInstance(new Locale("ru", "RU")).format(Math.max(0L, amount));
    }

    public static void grantGems(ServerPlayer player, int amount) {
        if (amount > 0) {
            adjustScore(player, LumenConfig.COMMON.gemsObjective.get(), amount);
        }
    }

    public static boolean trySpendGems(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        String objective = LumenConfig.COMMON.gemsObjective.get();
        long have = score(player, objective);
        if (have < amount) {
            return false;
        }
        adjustScore(player, objective, -(int) Math.min(amount, Integer.MAX_VALUE));
        return true;
    }

    public static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack.copy())) {
            player.drop(stack.copy(), false);
        }
    }

    public static boolean dailyAvailable(ServerPlayer player) {
        return dailyTag(player).getLong("lastDay") != today();
    }

    public static int dailyStreak(ServerPlayer player) {
        return dailyTag(player).getInt("streak");
    }

    /** @return reward size in coins, or -1 when already claimed today. */
    public static long claimDaily(ServerPlayer player) {
        CompoundTag tag = dailyTag(player);
        long today = today();
        if (tag.getLong("lastDay") == today) {
            return -1L;
        }
        int streak = tag.getLong("lastDay") == today - 1L
                ? Math.min(tag.getInt("streak") + 1, CaseConfig.get().daily.maxStreak)
                : 1;
        CaseConfig.Daily daily = CaseConfig.get().daily;
        long reward = daily.baseCoins + (long) (streak - 1) * daily.streakBonusCoins;

        tag.putLong("lastDay", today);
        tag.putInt("streak", streak);
        player.getPersistentData().put(DAILY_TAG, tag);
        grantCoins(player, reward);
        return reward;
    }

    private static long today() {
        return LocalDate.now(ZoneId.systemDefault()).toEpochDay();
    }

    private static CompoundTag dailyTag(ServerPlayer player) {
        return player.getPersistentData().getCompound(DAILY_TAG);
    }

    private static Scoreboard board(ServerPlayer player) {
        return player.getServer() != null ? player.getServer().getScoreboard() : player.getScoreboard();
    }

    private static long score(ServerPlayer player, String objectiveName) {
        Scoreboard scoreboard = board(player);
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            return 0L;
        }
        return scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective).getScore();
    }

    private static void adjustScore(ServerPlayer player, String objectiveName, int delta) {
        Scoreboard scoreboard = board(player);
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            objective = scoreboard.addObjective(objectiveName, ObjectiveCriteria.DUMMY,
                    Component.literal(objectiveName), ObjectiveCriteria.DUMMY.getDefaultRenderType());
        }
        Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective);
        score.setScore(score.getScore() + delta);
    }

    private static void setScore(ServerPlayer player, String objectiveName, long value) {
        Scoreboard scoreboard = board(player);
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            objective = scoreboard.addObjective(objectiveName, ObjectiveCriteria.DUMMY,
                    Component.literal(objectiveName), ObjectiveCriteria.DUMMY.getDefaultRenderType());
        }
        Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective);
        int clamped = (int) Math.max(0L, Math.min(value, Integer.MAX_VALUE));
        score.setScore(clamped);
    }

    /** Vault/Essentials balance, or -1 when the economy plugin is missing. */
    private static long vaultBalance(ServerPlayer player) {
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            Object services = bukkit.getMethod("getServicesManager").invoke(null);
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            Object registration = services.getClass()
                    .getMethod("getRegistration", Class.class)
                    .invoke(services, economyClass);
            if (registration == null) {
                return -1L;
            }
            Object economy = registration.getClass().getMethod("getProvider").invoke(registration);
            if (economy == null) {
                return -1L;
            }
            Class<?> offlineClass = Class.forName("org.bukkit.OfflinePlayer");
            Object offline = bukkit.getMethod("getOfflinePlayer", UUID.class).invoke(null, player.getUUID());
            Object bal = economy.getClass().getMethod("getBalance", offlineClass).invoke(economy, offline);
            if (!(bal instanceof Number number) || number.doubleValue() < 0.0d) {
                return -1L;
            }
            return (long) Math.floor(number.doubleValue());
        } catch (Throwable ignored) {
            return -1L;
        }
    }

    private static void ecoCommand(ServerPlayer player, String op, long amount) {
        if (player == null || player.getServer() == null) {
            return;
        }
        ecoCommand(player.getServer(), player.getGameProfile().getName(), op, amount);
    }

    private static void ecoCommand(MinecraftServer server, String name, String op, long amount) {
        if (server == null || name == null || name.isBlank()) {
            return;
        }
        try {
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(),
                    "eco " + op + " " + name + " " + amount
            );
        } catch (Throwable ignored) {
        }
    }

    private static void adjustScoreByName(MinecraftServer server, String name, int delta) {
        if (server == null || name == null || name.isBlank() || delta == 0) {
            return;
        }
        String objectiveName = LumenConfig.COMMON.coinsObjective.get();
        Scoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            objective = scoreboard.addObjective(objectiveName, ObjectiveCriteria.DUMMY,
                    Component.literal(objectiveName), ObjectiveCriteria.DUMMY.getDefaultRenderType());
        }
        Score score = scoreboard.getOrCreatePlayerScore(name, objective);
        long next = (long) score.getScore() + delta;
        int clamped = (int) Math.max(0L, Math.min(next, Integer.MAX_VALUE));
        score.setScore(clamped);
    }

    private static boolean playerKnown(MinecraftServer server, String name) {
        if (findOnline(server, name) != null) {
            return true;
        }
        try {
            if (server.getProfileCache() != null && server.getProfileCache().get(name).isPresent()) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            Object offline = bukkit.getMethod("getOfflinePlayer", String.class).invoke(null, name);
            Boolean played = (Boolean) offline.getClass().getMethod("hasPlayedBefore").invoke(offline);
            if (Boolean.TRUE.equals(played)) {
                return true;
            }
            Boolean onlineFlag = (Boolean) offline.getClass().getMethod("isOnline").invoke(offline);
            return Boolean.TRUE.equals(onlineFlag);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static long inventoryCoins(ServerPlayer player) {
        long total = 0L;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null && "lightmanscurrency".equals(id.getNamespace()) && id.getPath().startsWith("coin_")) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void drainInventoryCoins(ServerPlayer player, int amount) {
        for (int slot = 0; slot < player.getInventory().getContainerSize() && amount > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id == null || !"lightmanscurrency".equals(id.getNamespace()) || !id.getPath().startsWith("coin_")) {
                continue;
            }
            int take = Math.min(amount, stack.getCount());
            stack.shrink(take);
            amount -= take;
        }
    }
}
