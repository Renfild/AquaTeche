package store.aquateche.aqualumen.common.service;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.config.LumenConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every button in the hub sends an action id, never a result. The server validates the id,
 * applies rate limiting and performs the change, then pushes a fresh snapshot.
 */
public final class HubActionHandler {

    private static final Map<String, Long> LAST_ACTION = new ConcurrentHashMap<>();

    private HubActionHandler() {
    }

    private record PendingCaseReward(CaseConfig.CaseDef def, CaseConfig.LootDef loot, int amount, String type, long timestamp) {
    }

    private static final Map<UUID, PendingCaseReward> PENDING_CASE_REWARDS = new ConcurrentHashMap<>();
    private static final java.util.concurrent.ScheduledExecutorService REWARD_SCHEDULER = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AquaLumen-CaseRewardScheduler");
        t.setDaemon(true);
        return t;
    });

    public static void handle(ServerPlayer player, String action, String argument) {
        if (!LumenConfig.COMMON.hubEnabled.get()) {
            return;
        }
        store.aquateche.aqualumen.common.ServerEvents.markModded(player);
        if (isOnCooldown(player, action)) {
            player.sendSystemMessage(Component.translatable("msg.aqualumen.cooldown").withStyle(ChatFormatting.YELLOW));
            return;
        }

        switch (action) {
            case "hub.open" -> HubDataService.open(player);
            case "hub.refresh" -> HubDataService.push(player);
            case "hub.close" -> HubDataService.closeFor(player.getUUID());
            case "case.open" -> openCase(player, argument);
            case "case.claim" -> claimCaseReward(player, true);
            case "daily.claim" -> claimDaily(player);
            case "events.claim" -> handleEventClaim(player, argument);
            case "events.reroll" -> handleEventReroll(player, argument);
            case "pass.claim" -> claimPass(player, argument);
            case "store.buy" -> StoreCatalog.buy(player, argument);
            case "hub.kit" -> handleKit(player, argument);
            case "hub.warp" -> handleWarp(player, argument);
            case "auction.cancel" -> {
                int cid;
                try {
                    cid = Integer.parseInt(argument.trim());
                } catch (NumberFormatException e) {
                    cid = -1;
                }
                if (cid > 0) {
                    MarketService.cancel(player, cid);
                }
            }
            case "auction.buy" -> {
                int id;
                try {
                    id = Integer.parseInt(argument.trim());
                } catch (NumberFormatException e) {
                    id = -1;
                }
                if (id > 0) {
                    MarketService.buy(player, id);
                }
            }
            case "fish.sell_all" -> FishShopConfig.sellAll(player);
            case "fish.sell" -> FishShopConfig.sellSingle(player, argument);
            default -> AquaLumenUI.LOGGER.debug("Unknown hub action '{}' from {}", action, player.getGameProfile().getName());
        }
    }

    private static void handleKit(ServerPlayer player, String kitId) {
        if (player != null && kitId != null) {
            KitConfig.grantKit(player, kitId);
            HubDataService.push(player);
        }
    }

    private static void handleWarp(ServerPlayer player, String warpId) {
        if (player != null && warpId != null) {
            WarpConfig.teleport(player, warpId);
        }
    }

    private static void claimPass(ServerPlayer player, String argument) {
        int tier;
        try {
            tier = Integer.parseInt(argument.trim());
        } catch (NumberFormatException e) {
            tier = 1;
        }

        net.minecraft.nbt.CompoundTag claimedTag = player.getPersistentData().getCompound("aqualumen_pass_claimed");
        String key = "t_" + tier;
        if (claimedTag.getBoolean(key)) {
            player.sendSystemMessage(Component.literal("Награда за уровень " + tier + " уже получена").withStyle(ChatFormatting.YELLOW));
            return;
        }

        // Grant real coin and item rewards for pass level
        long rewardCoins = 100L + (long) tier * 35L;
        HubEconomy.grantCoins(player, rewardCoins);

        claimedTag.putBoolean(key, true);
        player.getPersistentData().put("aqualumen_pass_claimed", claimedTag);

        player.sendSystemMessage(Component.literal("Сезонный пропуск: Награда уровня " + tier
                + " получена (+" + rewardCoins + " монет)").withStyle(ChatFormatting.GREEN));
        HubDataService.push(player);
    }

    static void openCasePublic(ServerPlayer player, String argument) {
        openCase(player, argument);
    }

    private static void openCase(ServerPlayer player, String argument) {
        String caseId = argument;
        int count = 1;
        if (argument != null && argument.contains(":")) {
            String[] parts = argument.split(":");
            caseId = parts[0];
            try {
                count = Math.max(1, Math.min(10, Integer.parseInt(parts[1])));
            } catch (NumberFormatException ignored) {}
        }

        CaseConfig.CaseDef def = CaseConfig.find(caseId);
        if (def == null || def.loot.isEmpty()) {
            player.sendSystemMessage(Component.literal("Кейс не найден").withStyle(ChatFormatting.RED));
            return;
        }

        long totalCost = (long) def.costCoins * count;
        if (!HubEconomy.trySpendCoins(player, totalCost)) {
            player.sendSystemMessage(Component.literal("Недостаточно монет: нужно " + totalCost
                    + ", есть " + HubEconomy.coins(player)).withStyle(ChatFormatting.RED));
            return;
        }

        RandomSource random = RandomSource.create();
        if (count <= 1) {
            CaseConfig.LootDef loot = CaseConfig.roll(def, random);
            int amount = CaseConfig.rollAmount(loot, random);
            String label = loot.label == null || loot.label.isBlank() ? loot.item : loot.label;
            String type = loot.type == null ? "item" : loot.type;

            // Stage result for client animation
            HubDataService.stageCaseResult(player.getUUID(), new HubSnapshot.CaseResult(
                    def.id, label,
                    HubDataService.rarityForWeight(loot.weight, HubDataService.totalWeight(def)),
                    amount, type));

            // Store pending reward so item is granted AFTER animation completes
            PENDING_CASE_REWARDS.put(player.getUUID(), new PendingCaseReward(def, loot, amount, type, System.currentTimeMillis()));

            // Schedule fallback delivery after 5.5s in case client doesn't send case.claim
            REWARD_SCHEDULER.schedule(() -> claimCaseReward(player, false), 5500, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            // Multi-roll: award all rewards and print clean summary
            StringBuilder summary = new StringBuilder();
            summary.append("§6[AquaTech] §fОткрыто §e×").append(count).append(" §fкейсов §b«").append(def.title).append("»:\n");
            CaseConfig.LootDef firstLoot = null;
            int firstAmount = 1;

            for (int i = 0; i < count; i++) {
                CaseConfig.LootDef loot = CaseConfig.roll(def, random);
                int amount = CaseConfig.rollAmount(loot, random);
                if (firstLoot == null) {
                    firstLoot = loot;
                    firstAmount = amount;
                }
                String type = loot.type == null ? "item" : loot.type;
                switch (type) {
                    case "coins" -> {
                        HubEconomy.grantCoins(player, amount);
                        summary.append("  §7• §e+").append(amount).append(" AquaCoins\n");
                    }
                    case "gems" -> {
                        HubEconomy.grantGems(player, amount);
                        summary.append("  §7• §d+").append(amount).append(" Гемов\n");
                    }
                    default -> {
                        ItemStack stack = itemStack(loot.item, amount);
                        HubEconomy.giveItem(player, stack);
                        summary.append("  §7• §b").append(stack.getHoverName().getString()).append(" §f×").append(amount).append("\n");
                    }
                }
            }
            player.sendSystemMessage(Component.literal(summary.toString().trim()));
            if (firstLoot != null) {
                String firstLabel = firstLoot.label == null || firstLoot.label.isBlank() ? firstLoot.item : firstLoot.label;
                String firstType = firstLoot.type == null ? "item" : firstLoot.type;
                HubDataService.stageCaseResult(player.getUUID(), new HubSnapshot.CaseResult(
                        def.id, firstLabel + " (+ещё " + (count - 1) + ")",
                        HubDataService.rarityForWeight(firstLoot.weight, HubDataService.totalWeight(def)),
                        firstAmount, firstType));
            }
        }

        HubDataService.push(player);
    }

    public static void claimCaseReward(ServerPlayer player, boolean push) {
        if (player == null) return;
        PendingCaseReward pending = PENDING_CASE_REWARDS.remove(player.getUUID());
        if (pending == null) return;

        player.server.execute(() -> {
            switch (pending.type()) {
                case "coins" -> {
                    HubEconomy.grantCoins(player, pending.amount());
                    player.sendSystemMessage(Component.literal("Кейс \u00ab" + pending.def().title + "\u00bb: "
                            + (pending.loot().label == null || pending.loot().label.isBlank() ? "AquaCoins" : pending.loot().label)
                            + " \u00d7" + pending.amount()).withStyle(ChatFormatting.GOLD));
                }
                case "gems" -> {
                    HubEconomy.grantGems(player, pending.amount());
                    player.sendSystemMessage(Component.literal("Кейс \u00ab" + pending.def().title + "\u00bb: "
                            + (pending.loot().label == null || pending.loot().label.isBlank() ? "Гемы" : pending.loot().label)
                            + " \u00d7" + pending.amount()).withStyle(ChatFormatting.LIGHT_PURPLE));
                }
                default -> {
                    ItemStack stack = itemStack(pending.loot().item, pending.amount());
                    HubEconomy.giveItem(player, stack);
                    player.sendSystemMessage(Component.literal("Кейс \u00ab" + pending.def().title + "\u00bb: ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(stack.getHoverName())
                            .append(Component.literal(" \u00d7" + pending.amount()).withStyle(ChatFormatting.AQUA)));
                }
            }
            if (push) {
                HubDataService.push(player);
            }
        });
    }

    private static void claimDaily(ServerPlayer player) {
        long reward = HubEconomy.claimDaily(player);
        if (reward < 0L) {
            player.sendSystemMessage(Component.literal("Ежедневная награда уже получена")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        player.sendSystemMessage(Component.literal("Ежедневная награда: +" + reward + " монет (серия "
                + HubEconomy.dailyStreak(player) + ")").withStyle(ChatFormatting.GREEN));
        HubDataService.push(player);
    }

    private static ItemStack itemStack(String itemId, int count) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
        if (item == null || "minecraft:air".equals(BuiltInRegistries.ITEM.getKey(item).toString())) {
            AquaLumenUI.LOGGER.error("Unknown case item ID: '{}' -> falling back to prismarine shard", itemId);
            return new ItemStack(Items.PRISMARINE_SHARD, Math.max(1, count));
        }
        return new ItemStack(item, Math.max(1, count));
    }

    public static void forget(UUID id) {
        LAST_ACTION.keySet().removeIf(key -> key.startsWith(id + ":"));
    }

    private static boolean isOnCooldown(ServerPlayer player, String action) {
        if ("hub.open".equals(action) || "hub.refresh".equals(action) || "hub.close".equals(action)) {
            return false;
        }
        long now = System.currentTimeMillis();
        long cooldown = LumenConfig.COMMON.actionCooldownMs.get();
        String key = player.getUUID() + ":" + action;
        Long previous = LAST_ACTION.put(key, now);
        return previous != null && now - previous < cooldown;
    }


    /**
     * Контракты дня исполняются в aquatech_ui (OceanEventsService);
     * вызываем reflection'ом, чтобы не тянуть compile-зависимость.
     */
    private static boolean eventCall(ServerPlayer player, String method, int index) {
        try {
            Class<?> svc = Class.forName("net.aquatech.ui.fishing.OceanEventsService");
            Object result = svc.getMethod(method, ServerPlayer.class, int.class)
                    .invoke(null, player, index);
            return Boolean.TRUE.equals(result);
        } catch (Throwable t) {
            return false;
        }
    }

    private static void handleEventClaim(ServerPlayer player, String argument) {
        int index;
        try {
            index = Integer.parseInt(argument.trim());
        } catch (NumberFormatException e) {
            return;
        }
        if (!eventCall(player, "claimQuest", index)) {
            player.sendSystemMessage(Component.literal("\u00a76[\u041a\u043e\u043d\u0442\u0440\u0430\u043a\u0442] \u00a77\u041a\u043e\u043d\u0442\u0440\u0430\u043a\u0442 \u0435\u0449\u0451 \u043d\u0435 \u0433\u043e\u0442\u043e\u0432 \u0438\u043b\u0438 \u043d\u0430\u0433\u0440\u0430\u0434\u0430 \u0443\u0436\u0435 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0430."));
        }
    }

    private static void handleEventReroll(ServerPlayer player, String argument) {
        int index;
        try {
            index = Integer.parseInt(argument.trim());
        } catch (NumberFormatException e) {
            return;
        }
        if (!eventCall(player, "rerollQuest", index)) {
            player.sendSystemMessage(Component.literal("\u00a76[\u041a\u043e\u043d\u0442\u0440\u0430\u043a\u0442] \u00a77\u0420\u0435\u0440\u043e\u043b\u043b \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d (\u043d\u0443\u0436\u043d\u043e 100 \u043c\u043e\u043d\u0435\u0442 \u0438\u043b\u0438 \u0437\u0430\u043f\u0430\u0441 \u043a\u043e\u043d\u0442\u0440\u0430\u043a\u0442\u043e\u0432 \u0438\u0441\u0447\u0435\u0440\u043f\u0430\u043d)."));
        }
    }}
