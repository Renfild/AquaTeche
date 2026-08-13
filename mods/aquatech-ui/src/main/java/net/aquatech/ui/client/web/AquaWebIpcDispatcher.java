package net.aquatech.ui.client.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.aquatech.ui.AquaTechUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * Two-way IPC Message Dispatcher between JavaScript (window.AquaTechBridge)
 * and Minecraft Forge 1.20.1 client runtime.
 */
public final class AquaWebIpcDispatcher {

    private AquaWebIpcDispatcher() {
    }

    /**
     * Processes an incoming raw JSON string sent from the embedded CEF web page.
     * Example format: {"action": "BUY_ITEM", "payload": {"id": "vip_group", "cost": 150}}
     */
    public static void dispatch(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return;

        try {
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
            if (!root.has("action")) return;

            String action = root.get("action").getAsString();
            JsonObject payload = root.has("payload") ? root.getAsJsonObject("payload") : new JsonObject();

            Minecraft mc = Minecraft.getInstance();

            switch (action.toUpperCase()) {
                case "CLOSE_GUI" -> mc.execute(() -> {
                    if (mc.screen != null) {
                        mc.setScreen(null);
                    }
                });
                case "PLAY_SOUND" -> mc.execute(() -> {
                    mc.getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.5f, 0.25f)
                    );
                });
                case "TELEPORT_WARP" -> mc.execute(() -> {
                    if (mc.player != null && payload.has("warp")) {
                        String warpName = payload.get("warp").getAsString();
                        mc.player.connection.sendUnsignedCommand("warp " + warpName);
                    }
                });
                case "TELEPORT_SPAWN" -> mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.connection.sendUnsignedCommand("spawn");
                    }
                });
                default -> {
                    // Custom action handler
                }
            }
        } catch (Exception e) {
            AquaTechUI.LOGGER.debug("[ipc] drop: {}", e.toString());
        }
    }
}
