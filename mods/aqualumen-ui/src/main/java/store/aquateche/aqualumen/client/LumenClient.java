package store.aquateche.aqualumen.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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

    public static final String CLIENT_VERSION = AquaLumenUI.VERSION;

    public static final KeyMapping OPEN_HUB = new KeyMapping(
            "key.aqualumen.open_hub",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F4,
            "key.categories.aqualumen");

    @Nullable
    private static HubSnapshot snapshot;
    private static long snapshotReceivedAt;

    private LumenClient() {
    }

    public static void bootstrap() {
        MinecraftForge.EVENT_BUS.register(GameEvents.class);
        MinecraftForge.EVENT_BUS.register(store.aquateche.aqualumen.client.chat.ChatWebOverlay.class);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUB);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CHAT_PANEL.id(),
                "web_chat",
                (gui, graphics, partialTick, width, height) ->
                        store.aquateche.aqualumen.client.chat.ChatWebOverlay.render(graphics, width, height, partialTick));
    }

    /** Called from the network thread wrapper; already scheduled on the client thread. */
    public static void acceptSync(HubSnapshot incoming, boolean openScreen) {
        snapshot = incoming;
        snapshotReceivedAt = System.currentTimeMillis();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof HubSnapshotScreen hub) {
            hub.refresh(incoming);
        } else if (openScreen) {
            minecraft.setScreen(HubScreenFactory.create("profile"));
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
        sendAction("hub.open", "");
        if (snapshot != null) {
            Minecraft.getInstance().setScreen(HubScreenFactory.create(initialTab));
        }
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
            LumenNetwork.toServer(new LumenPackets.ClientHello(CLIENT_VERSION));
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
        }
    }
}
