package store.aquateche.aqualumen.common.service;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import store.aquateche.aqualumen.config.LumenConfig;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Coins / gems movement and the daily reward state, kept consistent with what
 * {@link HubDataService} displays: scoreboard objective first, Lightman's coin
 * items in the inventory as the fallback wallet.
 */
public final class HubEconomy {

    private static final String DAILY_TAG = "aqualumen_daily";

    private HubEconomy() {
    }

    public static long coins(ServerPlayer player) {
        String objective = LumenConfig.COMMON.coinsObjective.get();
        long score = score(player, objective);
        if (score > 0L) {
            return score;
        }
        // Legacy wallet (old casesmod balance) — import into the scoreboard once,
        // so KubeJS portal sync and the hub read the same number.
        long legacy = player.getPersistentData().contains("coins")
                ? player.getPersistentData().getLong("coins")
                : 0L;
        if (legacy > 0L) {
            adjustScore(player, objective, (int) legacy);
            return legacy;
        }
        long inv = inventoryCoins(player);
        return inv > 0L ? inv : 0L;
    }

    public static boolean trySpendCoins(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        String objective = LumenConfig.COMMON.coinsObjective.get();
        if (coins(player) < amount) {
            return false;
        }
        long remaining = amount;
        long score = score(player, objective);
        if (score > 0L) {
            long fromScore = Math.min(score, remaining);
            adjustScore(player, objective, -(int) fromScore);
            remaining -= fromScore;
        }
        if (remaining > 0L) {
            drainInventoryCoins(player, (int) remaining);
        }
        if (player.getServer() != null) {
            try {
                player.getServer().getCommands().performPrefixedCommand(
                        player.getServer().createCommandSourceStack(),
                        "eco take " + player.getGameProfile().getName() + " " + amount
                );
            } catch (Throwable ignored) {
            }
        }
        return true;
    }

    public static void grantCoins(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return;
        }
        adjustScore(player, LumenConfig.COMMON.coinsObjective.get(), (int) amount);
        if (player.getServer() != null) {
            try {
                player.getServer().getCommands().performPrefixedCommand(
                        player.getServer().createCommandSourceStack(),
                        "eco give " + player.getGameProfile().getName() + " " + amount
                );
            } catch (Throwable ignored) {
            }
        }
    }

    public static void grantGems(ServerPlayer player, int amount) {
        if (amount > 0) {
            adjustScore(player, LumenConfig.COMMON.gemsObjective.get(), amount);
        }
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

    private static long score(ServerPlayer player, String objectiveName) {
        Objective objective = player.getScoreboard().getObjective(objectiveName);
        if (objective == null) {
            return 0L;
        }
        return player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), objective).getScore();
    }

    private static void adjustScore(ServerPlayer player, String objectiveName, int delta) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            objective = scoreboard.addObjective(objectiveName, ObjectiveCriteria.DUMMY,
                    Component.literal(objectiveName), ObjectiveCriteria.DUMMY.getDefaultRenderType());
        }
        Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective);
        score.setScore(score.getScore() + delta);
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
