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
            case "daily.claim" -> claimDaily(player);
            case "store.buy", "pass.claim" -> unavailable(player);
            default -> AquaLumenUI.LOGGER.debug("Unknown hub action '{}' from {}", action, player.getGameProfile().getName());
        }
    }

    private static void openCase(ServerPlayer player, String caseId) {
        CaseConfig.CaseDef def = CaseConfig.find(caseId);
        if (def == null || def.loot.isEmpty()) {
            player.sendSystemMessage(Component.literal("Кейс не найден").withStyle(ChatFormatting.RED));
            return;
        }
        if (!HubEconomy.trySpendCoins(player, def.costCoins)) {
            player.sendSystemMessage(Component.literal("Недостаточно монет: нужно " + def.costCoins
                    + ", есть " + HubEconomy.coins(player)).withStyle(ChatFormatting.RED));
            return;
        }

        RandomSource random = RandomSource.create();
        CaseConfig.LootDef loot = CaseConfig.roll(def, random);
        int amount = CaseConfig.rollAmount(loot, random);
        String label = loot.label == null || loot.label.isBlank() ? loot.item : loot.label;

        switch (loot.type == null ? "item" : loot.type) {
            case "coins" -> {
                HubEconomy.grantCoins(player, amount);
                player.sendSystemMessage(Component.literal("Кейс \u00ab" + def.title + "\u00bb: " + label
                        + " \u00d7" + amount).withStyle(ChatFormatting.GOLD));
            }
            case "gems" -> {
                HubEconomy.grantGems(player, amount);
                player.sendSystemMessage(Component.literal("Кейс \u00ab" + def.title + "\u00bb: " + label
                        + " \u00d7" + amount).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            default -> {
                ItemStack stack = itemStack(loot.item, amount);
                HubEconomy.giveItem(player, stack);
                player.sendSystemMessage(Component.literal("Кейс \u00ab" + def.title + "\u00bb: ")
                        .withStyle(ChatFormatting.AQUA)
                        .append(stack.getHoverName())
                        .append(Component.literal(" \u00d7" + amount).withStyle(ChatFormatting.AQUA)));
            }
        }
        HubDataService.push(player);
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

    private static void unavailable(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("msg.aqualumen.unavailable").withStyle(ChatFormatting.YELLOW));
    }
}
