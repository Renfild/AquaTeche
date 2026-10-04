package store.aquateche.aqualumen.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.client.screen.HubScreenFactory;
import store.aquateche.aqualumen.client.screen.HubSnapshotScreen;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.network.LumenNetwork;
import store.aquateche.aqualumen.network.LumenPackets;

import javax.annotation.Nullable;

/** Client entry point: keybinding, handshake and the cached snapshot the screen renders. */
@Mod.EventBusSubscriber(modid = AquaLumenUI.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LumenClient {


    public static final KeyMapping OPEN_HUB = new KeyMapping(
            "key.aqualumen.open_hub",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F4,
            "key.categories.aqualumen");

    public static final KeyMapping TOGGLE_GUIDE = new KeyMapping(
            "key.aqualumen.toggle_guide",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "key.categories.aqualumen");

    @Nullable
    private static HubSnapshot snapshot;
    private static long snapshotReceivedAt;
    private static final String DEFAULT_TAB = "profile";
    /** Вкладка, которую просил открыть последний {@link #openScreen}; нужна, пока снимок хаба ещё не пришёл. */
    private static String requestedTab = DEFAULT_TAB;
    private static boolean requestedStandalone;

    private LumenClient() {
    }

    public static void bootstrap() {
        MinecraftForge.EVENT_BUS.register(GameEvents.class);
        MinecraftForge.EVENT_BUS.register(store.aquateche.aqualumen.client.chat.ChatWebOverlay.class);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUB);
        event.register(TOGGLE_GUIDE);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CHAT_PANEL.id(),
                "web_chat",
                (gui, graphics, partialTick, width, height) ->
                        store.aquateche.aqualumen.client.chat.ChatWebOverlay.render(graphics, width, height, partialTick));
        event.registerAboveAll("onboarding_guide",
                (gui, graphics, partialTick, width, height) -> OnboardingHud.render(graphics, width, height));
    }

    /** Called from the network thread wrapper; already scheduled on the client thread. */
    public static void acceptSync(HubSnapshot incoming, boolean openScreen) {
        snapshot = incoming;
        snapshotReceivedAt = System.currentTimeMillis();
        String tab = requestedTab;
        boolean standalone = requestedStandalone;
        requestedTab = DEFAULT_TAB;
        requestedStandalone = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof HubSnapshotScreen hub) {
            hub.refresh(incoming);
        } else if (openScreen) {
            openHub(tab, standalone);
        }
    }

    @Nullable
    public static HubSnapshot snapshot() {
        return snapshot;
    }

    public static long snapshotReceivedAt() {
        return snapshotReceivedAt;
    }

    public static void openScreen(String initialTab) {
        open(initialTab, false);
    }

    /** Окно с одной вкладкой без остального меню: его открывает торговец рыбой. */
    public static void openStandalone(String tab) {
        open(tab, true);
    }

    private static void open(String tab, boolean standalone) {
        requestedTab = tab;
        requestedStandalone = standalone;
        sendAction("hub.open", "");
        if (snapshot != null) {
            openHub(tab, standalone);
        }
    }

    private static void openHub(String tab, boolean standalone) {
        Screen screen = HubScreenFactory.create(tab, standalone);
        if (screen == null) {
            reportHubUnavailable("браузерный движок (MCEF) не запущен");
            return;
        }
        Minecraft.getInstance().setScreen(screen);
    }

    /** Меню F4 не открылось: без сообщения игрок видит только, что кнопка ничего не делает. */
    public static void reportHubUnavailable(String reason) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.player.sendSystemMessage(Component.literal("[AquaTech] Меню F4 не открылось: " + reason
                + ". Перезапусти игру и дождись конца загрузки (при первом запуске докачивается Chromium, около 125 МБ)."
                + " Не помогло: отправь админу файл logs/latest.log.").withStyle(ChatFormatting.RED));
    }

    public static void sendAction(String action, String argument) {
        LumenNetwork.toServer(new LumenPackets.HubAction(action, argument));
    }

    /** Forge bus listeners, registered only on the physical client. */
    public static final class GameEvents {

        private GameEvents() {
        }

        @SubscribeEvent
        public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
            LumenNetwork.toServer(new LumenPackets.ClientHello(AquaLumenUI.version()));
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.screen != null) {
                return;
            }
            while (OPEN_HUB.consumeClick()) {
                openScreen("profile");
            }
            while (TOGGLE_GUIDE.consumeClick()) {
                OnboardingHud.toggle();
            }
        }
    }
}
