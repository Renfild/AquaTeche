package net.aquatech.ui.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.client.bubble.ChatBubbleManager;
import net.aquatech.ui.client.gui.OceanSkillTreeScreen;
import net.aquatech.ui.client.hud.RhythmHookOverlay;
import net.aquatech.ui.client.tab.OceanTabOverlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientEvents {

    public static final KeyMapping KEY_SKILL_TREE = new KeyMapping(
            "key.aquatech_ui.skill_tree",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.aquatech_ui"
    );

    public static final KeyMapping KEY_MARKET = new KeyMapping(
            "key.aquatech_ui.market",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F4,
            "key.categories.aquatech_ui"
    );

    private ClientEvents() {
    }

    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                "ocean_hud",
                (gui, graphics, partialTick, screenWidth, screenHeight) ->
                        net.aquatech.ui.client.hud.OceanHudOverlay.render(graphics, partialTick)
        );
        event.registerAboveAll(
                "ocean_tab",
                (gui, graphics, partialTick, screenWidth, screenHeight) -> {
                    if (ClientUiState.tabOpen()) {
                        OceanTabOverlay.render(graphics, partialTick);
                    }
                }
        );
        event.registerAboveAll(
                "rhythm_hook",
                (gui, graphics, partialTick, screenWidth, screenHeight) ->
                        net.aquatech.ui.client.hud.RhythmHookOverlay.render(graphics, partialTick)
        );
        event.registerAboveAll(
                "aqua_chat",
                (gui, graphics, partialTick, screenWidth, screenHeight) ->
                        net.aquatech.ui.client.chat.AquaChatOverlay.render(graphics, partialTick)
        );
    }

    private static boolean welcomed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientUiState.tick();
        ChatBubbleManager.tick();
        net.aquatech.ui.client.hud.RhythmHookOverlay.tick();
        StarCatcherToastSuppressor.tick();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options == null) {
            ClientUiState.setTabOpen(false);
            welcomed = false;
            return;
        }
        if (!welcomed && mc.player.tickCount > 20) {
            welcomed = true;
            ClientUiState.resetSessionTimer();
            mc.player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§b[AquaTech] §fСборка загружена. §7(F4 — Меню сервера)"));
        }
        net.aquatech.ui.client.cache.ResourceCacheManager.getInstance().prefetchPlayerAvatar(mc.player.getUUID());

        if (RhythmHookOverlay.isActive()) {
            ClientUiState.setTabOpen(false);
            return;
        }

        boolean wasOpen = ClientUiState.tabOpen();
        int tabKey = mc.options.keyPlayerList.getKey().getValue();
        boolean tabHeld = mc.screen == null
                && InputConstants.isKeyDown(mc.getWindow().getWindow(), tabKey);
        ClientUiState.setTabOpen(tabHeld);

        mc.options.keyPlayerList.setDown(false);
        if (wasOpen && !tabHeld) {
            OceanTabOverlay.resetScroll();
        }

        while (KEY_MARKET.consumeClick()) {
            if (mc.player != null) {
                try {
                    Class<?> client = Class.forName("store.aquateche.aqualumen.client.LumenClient");
                    client.getMethod("openScreen", String.class).invoke(null, "fishing");
                } catch (Throwable t) {
                    if (mc.player.connection != null) {
                        mc.player.connection.sendCommand("shop");
                    }
                }
            }
        }
    }

    // InputEvent.Key is NOT cancelable on Forge 1.20.1 — keys are suppressed in
    // RhythmHookOverlay.suppressVanillaInput() + onMovementInput instead.

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (!RhythmHookOverlay.isActive()) return;
        // Cancel all mouse events so vanilla doesn't use/attack; overlay reads RMB via GLFW.
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (RhythmHookOverlay.isActive()) {
            event.setCanceled(true);
            return;
        }
        if (!ClientUiState.tabOpen()) {
            return;
        }
        OceanTabOverlay.scroll(event.getScrollDelta());
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!RhythmHookOverlay.isActive()) return;
        event.getInput().up = false;
        event.getInput().down = false;
        event.getInput().left = false;
        event.getInput().right = false;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
        event.getInput().forwardImpulse = 0f;
        event.getInput().leftImpulse = 0f;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
        ResourceLocation id = event.getOverlay().id();
        if (VanillaGuiOverlay.PLAYER_LIST.id().equals(id)) {
            Minecraft.getInstance().gui.getTabList().setVisible(false);
            event.setCanceled(true);
        }
        if (VanillaGuiOverlay.SCOREBOARD.id().equals(id)) {
            event.setCanceled(true);
        }
        if (VanillaGuiOverlay.CHAT_PANEL.id().equals(id)) {
            event.setCanceled(true);
        }
        // Мини-игра: нижние тексты (имя предмета / record overlay) налезают на UI
        if (RhythmHookOverlay.isActive()
                && (VanillaGuiOverlay.ITEM_NAME.id().equals(id)
                || VanillaGuiOverlay.RECORD_OVERLAY.id().equals(id))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onClientChatReceived(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        if (event.getMessage() == null) return;
        if (event instanceof net.minecraftforge.client.event.ClientChatReceivedEvent.System sys && sys.isOverlay()) {
            return; // Don't intercept Action Bar messages
        }

        String text = event.getMessage().getString().toLowerCase();
        if (text.contains("промышленная модернизация")
                || text.contains("industrial upgrade")
                || text.contains("нажмите i")
                || text.contains("press i to open")
                || text.contains("руководство industrial")
                || text.contains("industrialupgrade")) {
            event.setCanceled(true);
            return;
        }
        net.aquatech.ui.client.chat.AquaChatManager.addMessage(event.getMessage());
        event.setCanceled(true);
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpen(net.minecraftforge.client.event.ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof net.minecraft.client.gui.screens.ChatScreen vanillaChat
                && !(event.getNewScreen() instanceof net.aquatech.ui.client.chat.AquaChatScreen)) {
            String initial = "";
            try {
                for (java.lang.reflect.Field f : net.minecraft.client.gui.screens.ChatScreen.class.getDeclaredFields()) {
                    if (f.getType() == String.class) {
                        f.setAccessible(true);
                        Object val = f.get(vanillaChat);
                        if (val instanceof String s && !s.isEmpty()) {
                            initial = s;
                            break;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
            event.setNewScreen(new net.aquatech.ui.client.chat.AquaChatScreen(initial));
        }
    }
}

