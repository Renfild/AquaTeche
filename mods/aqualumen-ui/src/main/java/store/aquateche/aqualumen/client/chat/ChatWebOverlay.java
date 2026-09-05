package store.aquateche.aqualumen.client.chat;

import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.client.web.LumenWebBridge;
import store.aquateche.aqualumen.client.web.PlayerHeadCapture;
import store.aquateche.aqualumen.config.LumenConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Web-rendered chat: the merged concept — a compact feed bottom-left while playing,
 * and a full panel (channels + input) when the player opens chat.
 * The vanilla chat overlay is suppressed while this is enabled; fallback flag in config.
 */
@Mod.EventBusSubscriber(modid = AquaLumenUI.MODID, value = Dist.CLIENT)
public final class ChatWebOverlay {

    private static final String SELF_ACTIONS = "chat.send";
    private static final Pattern NICK = Pattern.compile("([A-Za-z0-9_]{3,16})\\s*[:>\u25b8\u00bb]");
    private static final Pattern RANK_COLOR = Pattern.compile("\u00a7([0-9a-f])", Pattern.CASE_INSENSITIVE);

    private static LumenWebBridge bridge;
    private static boolean inputOpen;
    private static int lastPixelWidth;
    private static int lastPixelHeight;

    private ChatWebOverlay() {
    }

    public static boolean enabled() {
        return LumenConfig.CLIENT.webChat.get();
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    // ------------------------------------------------------------ HUD render

    public static void render(GuiGraphics graphics, int width, int height, float partialTick) {
        if (!enabled()) {
            return;
        }
        ensureBridge(width, height);
        if (bridge == null) {
            return;
        }
        if (!inputOpen) {
            pollBridge();
        }
        int feedHeight = inputOpen ? height - 120 : 150;
        bridge.blit(6, height - feedHeight - 56, 500, feedHeight);
    }

    private static void ensureBridge(int width, int height) {
        var window = mc().getWindow();
        int pw = (int) Math.ceil(width * window.getGuiScale());
        int ph = (int) Math.ceil(height * window.getGuiScale());
        if (bridge == null) {
            bridge = new LumenWebBridge(LumenWebBridge.CHAT_URL, pw, ph);
            lastPixelWidth = pw;
            lastPixelHeight = ph;
            pushSelf();
        } else if (pw != lastPixelWidth || ph != lastPixelHeight) {
            bridge.resize(pw, ph);
            lastPixelWidth = pw;
            lastPixelHeight = ph;
        }
    }

    private static void pushSelf() {
        if (bridge == null || mc().player == null) {
            return;
        }
        String nick = mc().player.getGameProfile().getName();
        String head = PlayerHeadCapture.dataUrl(nick);
        bridge.execute("window.Chat&&window.Chat.setSelf('" + head + "','" + nick + "');");
    }

    private static void pollBridge() {
        String message;
        while ((message = bridge.pollMessage()) != null) {
            dispatch(message);
        }
    }

    /** Called by the HUD (compact) and by the input screen (expanded) — whoever is alive. */
    static void pollMessages() {
        if (bridge != null) {
            pollBridge();
        }
    }

    static void dispatch(String message) {
        try {
            var root = com.google.gson.JsonParser.parseString(message).getAsJsonObject();
            String type = root.has("type") ? root.get("type").getAsString() : "";
            switch (type) {
                case "chat.send" -> {
                    String text = root.has("text") ? root.get("text").getAsString() : "";
                    if (!text.isBlank() && text.length() <= 256 && mc().player != null) {
                        var connection = mc().player.connection;
                        if (text.startsWith("/")) {
                            connection.sendCommand(text.substring(1));
                        } else {
                            connection.sendChat(text);
                        }
                    }
                    collapse();
                }
                case "chat.close" -> collapse();
                default -> AquaLumenUI.LOGGER.debug("[AquaLumen Chat] dropped IPC '{}'", type);
            }
        } catch (RuntimeException error) {
            AquaLumenUI.LOGGER.debug("[AquaLumen Chat] invalid IPC: {}", error.toString());
        }
    }

    private static void collapse() {
        inputOpen = false;
        bridge.execute("document.body.classList.remove('open')");
        mc().setScreen(null);
    }

    // ------------------------------------------------------------ input screen

    public static void openInput() {
        if (!enabled() || bridge == null) {
            return;
        }
        inputOpen = true;
        bridge.execute("document.body.classList.add('open');"
                + "var i=document.getElementById('input');if(i){i.focus();}");
        mc().setScreen(new ChatWebInputScreen());
    }

    public static LumenWebBridge bridgeForInput() {
        return inputOpen ? bridge : null;
    }

    public static void onInputClosed() {
        inputOpen = false;
        if (bridge != null) {
            bridge.execute("document.body.classList.remove('open')");
        }
    }

    // ------------------------------------------------------------ events

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChatReceived(ClientChatReceivedEvent event) {
        if (!enabled()) {
            return;
        }
        var window = mc().getWindow();
        ensureBridge(window.getGuiScaledWidth(), window.getGuiScaledHeight());
        String legacy = toLegacy(event.getMessage());
        boolean sys = event.isSystem();
        String nick = nickOf(legacy);
        String color = colorOf(legacy);
        boolean own = mc().player != null && nick.equalsIgnoreCase(mc().player.getGameProfile().getName());
        String head = own && !"".equals(PlayerHeadCapture.dataUrl(nick))
                ? PlayerHeadCapture.dataUrl(nick)
                : PlayerHeadCapture.dataUrlFor(nick);
        StringBuilder json = new StringBuilder("{");
        json.append("\"text\":").append(quote(legacy));
        if (!nick.isBlank()) {
            json.append(",\"nick\":").append(quote(nick));
        }
        if (!head.isBlank()) {
            json.append(",\"head\":").append(quote(head));
        }
        json.append(",\"color\":").append(quote(color));
        json.append(",\"own\":").append(own);
        json.append(",\"sys\":").append(sys);
        json.append("}");
        if (bridge != null) {
            bridge.execute("window.Chat&&window.Chat.push(" + json + ");");
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!enabled()) {
            return;
        }
        if (event.getNewScreen() instanceof net.minecraft.client.gui.screens.ChatScreen) {
            event.setCanceled(true);
            openInput();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (enabled() && event.getOverlay().id() == VanillaGuiOverlay.CHAT_PANEL.id()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        shutdown();
    }

    public static void shutdown() {
        if (bridge != null) {
            bridge.close();
            bridge = null;
        }
        inputOpen = false;
    }

    // ------------------------------------------------------------ helpers

    private static String nickOf(String legacy) {
        String plain = legacy.replaceAll("\u00a7.", "");
        Matcher m = NICK.matcher(plain);
        return m.find() ? m.group(1) : "";
    }

    private static String colorOf(String legacy) {
        Matcher m = RANK_COLOR.matcher(legacy);
        String first = m.find() ? m.group(1).toLowerCase() : "b";
        return switch (first) {
            case "a" -> "#44e1aa";
            case "c", "4" -> "#ff5a5a";
            case "6", "e" -> "#ffc94d";
            case "d" -> "#f05ad0";
            case "9", "3", "b" -> "#4ec9ff";
            case "5" -> "#b38cff";
            default -> "#7fd0ff";
        };
    }

    private static String toLegacy(Component component) {
        StringBuilder out = new StringBuilder();
        appendLegacy(component, out);
        return out.toString();
    }

    private static void appendLegacy(Component component, StringBuilder out) {
        var style = component.getStyle();
        if (style.getColor() != null) {
            out.append('\u00a7').append(legacyCodeOf(style.getColor().toString()));
        }
        if (style.isBold()) {
            out.append('\u00a7').append('l');
        }
        if (style.isItalic()) {
            out.append('\u00a7').append('o');
        }
        if (style.isUnderlined()) {
            out.append('\u00a7').append('n');
        }
        out.append(component.getString());
        for (Component child : component.getSiblings()) {
            appendLegacy(child, out);
        }
    }

    private static String legacyCodeOf(String colorKey) {
        // colorKey looks like "chat_blue" / "#4ec9ff" — map common chat palette names, fallback white
        return switch (colorKey) {
            case "black" -> "0";
            case "dark_blue" -> "1";
            case "dark_green" -> "2";
            case "dark_aqua" -> "3";
            case "dark_red" -> "4";
            case "dark_purple" -> "5";
            case "gold" -> "6";
            case "gray" -> "7";
            case "dark_gray" -> "8";
            case "blue" -> "9";
            case "green" -> "a";
            case "aqua" -> "b";
            case "red" -> "c";
            case "light_purple" -> "d";
            case "yellow" -> "e";
            default -> "f";
        };
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
