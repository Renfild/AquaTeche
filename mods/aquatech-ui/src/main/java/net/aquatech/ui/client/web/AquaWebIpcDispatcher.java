package net.aquatech.ui.client.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.client.gui.AquaWebScreen;
import net.aquatech.ui.client.gui.OceanSkillTreeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * Two-way IPC Message Dispatcher between JavaScript (window.AquaTechBridge)
 * and Minecraft Forge 1.20.1 client runtime.
 * Seamlessly integrates CasesMod GUIs (KitsScreen, WarpsScreen, CasesScreen).
 */
public final class AquaWebIpcDispatcher {

    private AquaWebIpcDispatcher() {
    }

    /**
     * Processes an incoming raw JSON string sent from the embedded CEF web page.
     */
    public static void dispatch(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return;

        try {
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
            if (!root.has("action")) return;

            String action = root.get("action").getAsString();
            JsonObject payload = root.has("payload") && root.get("payload").isJsonObject()
                    ? root.getAsJsonObject("payload")
                    : new JsonObject();

            Minecraft mc = Minecraft.getInstance();

            switch (action.toUpperCase()) {
                case "CLOSE_GUI", "CLOSE" -> mc.execute(() -> {
                    if (mc.screen != null) {
                        mc.setScreen(null);
                    }
                });
                case "PLAY_SOUND" -> mc.execute(() -> {
                    mc.getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.5f, 0.25f)
                    );
                });
                case "OPEN_KITS" -> mc.execute(() -> {
                    try {
                        Class<?> clazz = Class.forName("com.casesmod.client.gui.KitsScreen");
                        Screen screen = (Screen) clazz.getConstructor().newInstance();
                        mc.setScreen(screen);
                    } catch (Throwable t) {
                        mc.setScreen(null);
                        if (mc.player != null) mc.player.connection.sendUnsignedCommand("kit");
                    }
                });
                case "OPEN_WARPS", "TELEPORT_WARP" -> mc.execute(() -> {
                    String warp = root.has("warp") ? root.get("warp").getAsString() : payload.has("warp") ? payload.get("warp").getAsString() : "";
                    if (!warp.isBlank()) {
                        mc.setScreen(null);
                        if (mc.player != null) mc.player.connection.sendUnsignedCommand("warp " + warp);
                        return;
                    }
                    try {
                        Class<?> clazz = Class.forName("com.casesmod.client.gui.WarpsScreen");
                        Screen screen = (Screen) clazz.getConstructor().newInstance();
                        mc.setScreen(screen);
                    } catch (Throwable t) {
                        mc.setScreen(null);
                        if (mc.player != null) mc.player.connection.sendUnsignedCommand("warp");
                    }
                });
                case "OPEN_CASES" -> mc.execute(() -> {
                    try {
                        Class<?> clazz = Class.forName("com.casesmod.client.gui.CasesScreen");
                        Screen screen = (Screen) clazz.getConstructor().newInstance();
                        mc.setScreen(screen);
                    } catch (Throwable t) {
                        mc.setScreen(null);
                        if (mc.player != null) mc.player.connection.sendUnsignedCommand("cases");
                    }
                });
                case "TELEPORT_SPAWN" -> mc.execute(() -> {
                    mc.setScreen(null);
                    if (mc.player != null) {
                        mc.player.connection.sendUnsignedCommand("spawn");
                    }
                });
                case "NAVIGATE" -> mc.execute(() -> {
                    String to = root.has("to") ? root.get("to").getAsString() : payload.has("to") ? payload.get("to").getAsString() : "";
                    if ("donate".equalsIgnoreCase(to)) {
                        AquaWebScreen.openEmbed("Донат", "donate");
                    } else if ("cabinet".equalsIgnoreCase(to)) {
                        AquaWebScreen.openEmbed("Кабинет", "cabinet");
                    } else if ("skills".equalsIgnoreCase(to)) {
                        AquaWebScreen.openEmbed("Созвездия Океана", "skills");
                    } else if ("cases".equalsIgnoreCase(to)) {
                        try {
                            Class<?> clazz = Class.forName("com.casesmod.client.gui.CasesScreen");
                            Screen screen = (Screen) clazz.getConstructor().newInstance();
                            mc.setScreen(screen);
                        } catch (Throwable t) {
                            mc.setScreen(null);
                            if (mc.player != null) mc.player.connection.sendUnsignedCommand("cases");
                        }
                    } else if ("kits".equalsIgnoreCase(to)) {
                        try {
                            Class<?> clazz = Class.forName("com.casesmod.client.gui.KitsScreen");
                            Screen screen = (Screen) clazz.getConstructor().newInstance();
                            mc.setScreen(screen);
                        } catch (Throwable t) {
                            mc.setScreen(null);
                            if (mc.player != null) mc.player.connection.sendUnsignedCommand("kit");
                        }
                    } else if ("warps".equalsIgnoreCase(to)) {
                        try {
                            Class<?> clazz = Class.forName("com.casesmod.client.gui.WarpsScreen");
                            Screen screen = (Screen) clazz.getConstructor().newInstance();
                            mc.setScreen(screen);
                        } catch (Throwable t) {
                            mc.setScreen(null);
                            if (mc.player != null) mc.player.connection.sendUnsignedCommand("warp");
                        }
                    } else if ("menu".equalsIgnoreCase(to)) {
                        AquaWebScreen.openEmbed("Меню", "menu");
                    }
                });
                case "OPEN_QUESTS" -> mc.execute(() -> {
                    mc.setScreen(null);
                    try {
                        Class<?> clazz = Class.forName("dev.ftb.mods.ftbquests.client.ClientQuestFile");
                        Object instance = clazz.getField("INSTANCE").get(null);
                        clazz.getMethod("openQuestGui").invoke(instance);
                    } catch (Throwable t) {
                        try {
                            Class<?> clientClass = Class.forName("dev.ftb.mods.ftbquests.client.FTBQuestsClient");
                            clientClass.getMethod("openQuestGui").invoke(null);
                        } catch (Throwable ignored) {}
                    }
                });
                case "OPEN_VAULT" -> mc.execute(() -> {
                    mc.setScreen(null);
                    if (mc.player != null) mc.player.connection.sendUnsignedCommand("aquatech vault");
                });
                case "OPEN_LIMITERS" -> mc.execute(() -> {
                    mc.setScreen(null);
                    if (mc.player != null) mc.player.connection.sendUnsignedCommand("aquatech limiters");
                });
                case "OPEN_LOOK" -> mc.execute(() -> {
                    mc.setScreen(null);
                    if (mc.player != null) mc.player.connection.sendUnsignedCommand("aquatech look");
                });
                case "BUY_DONATE", "BUY_ITEM" -> mc.execute(() -> {
                    String slug = root.has("slug") ? root.get("slug").getAsString() : payload.has("slug") ? payload.get("slug").getAsString() : "";
                    if (mc.player != null && !slug.isBlank()) {
                        mc.player.connection.sendUnsignedCommand("donate " + slug);
                    }
                });
                case "LEARN_SKILL" -> mc.execute(() -> {
                    String skillId = root.has("skillId") ? root.get("skillId").getAsString() : payload.has("skillId") ? payload.get("skillId").getAsString() : "";
                    if (mc.player != null && !skillId.isBlank()) {
                        mc.player.connection.sendUnsignedCommand("skills learn " + skillId);
                    }
                });
                case "RESET_SKILLS" -> mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.connection.sendUnsignedCommand("skills reset");
                    }
                });
                case "EXEC_COMMAND" -> mc.execute(() -> {
                    String cmd = root.has("cmd") ? root.get("cmd").getAsString() : payload.has("cmd") ? payload.get("cmd").getAsString() : "";
                    if (cmd.isBlank()) return;
                    String cleanCmd = cmd.startsWith("/") ? cmd.substring(1).trim() : cmd.trim();
                    if ("kit".equalsIgnoreCase(cleanCmd) || "kits".equalsIgnoreCase(cleanCmd)) {
                        try {
                            Class<?> clazz = Class.forName("com.casesmod.client.gui.KitsScreen");
                            Screen screen = (Screen) clazz.getConstructor().newInstance();
                            mc.setScreen(screen);
                            return;
                        } catch (Throwable ignored) {}
                    }
                    if ("warp".equalsIgnoreCase(cleanCmd) || "warps".equalsIgnoreCase(cleanCmd)) {
                        try {
                            Class<?> clazz = Class.forName("com.casesmod.client.gui.WarpsScreen");
                            Screen screen = (Screen) clazz.getConstructor().newInstance();
                            mc.setScreen(screen);
                            return;
                        } catch (Throwable ignored) {}
                    }
                    if ("cases".equalsIgnoreCase(cleanCmd) || "case".equalsIgnoreCase(cleanCmd)) {
                        try {
                            Class<?> clazz = Class.forName("com.casesmod.client.gui.CasesScreen");
                            Screen screen = (Screen) clazz.getConstructor().newInstance();
                            mc.setScreen(screen);
                            return;
                        } catch (Throwable ignored) {}
                    }
                    mc.setScreen(null);
                    if (mc.player != null) {
                        mc.player.connection.sendUnsignedCommand(cleanCmd);
                    }
                });
                default -> {
                }
            }
        } catch (Exception e) {
            AquaTechUI.LOGGER.debug("[ipc] drop: {}", e.toString());
        }
    }
}
