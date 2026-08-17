package store.aquateche.aqualumen.common.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
            case "daily.claim", "store.buy", "case.open", "pass.claim" -> unavailable(player);
            default -> AquaLumenUI.LOGGER.debug("Unknown hub action '{}' from {}", action, player.getGameProfile().getName());
        }
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
