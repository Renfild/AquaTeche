package store.aquateche.aqualumen.client.screen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import store.aquateche.aqualumen.AquaLumenUI;
import store.aquateche.aqualumen.client.LumenClient;
import store.aquateche.aqualumen.client.web.HubSnapshotJson;
import store.aquateche.aqualumen.client.web.ItemIconResolver;
import store.aquateche.aqualumen.client.web.LumenWebBridge;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.common.service.FishMerchantProximity;
import store.aquateche.aqualumen.config.LumenConfig;

import java.util.Set;

public final class LumenWebScreen extends Screen implements HubSnapshotScreen {

    private static final int PAGE_READY_TIMEOUT_TICKS = 300;
    private static final Set<String> SERVER_ACTIONS = Set.of(
            "hub.refresh", "daily.claim", "hub.claim_daily", "store.buy", "case.open", "case.claim", "pass.claim", "pass.claim_premium",
            "hub.kit", "hub.warp", "fish.sell", "fish.sell_all", "events.claim", "events.reroll",
            "auction.buy", "auction.cancel", "exchange.buy_gems", "exchange.sell_gems");

    private final String initialTab;
    /** Открыт торговцем: страница показывает только одну вкладку, без остального меню. */
    private final boolean standalone;
    private LumenWebBridge bridge;
    private HubSnapshot pendingSnapshot;
    private boolean pageReady;
    private boolean modalOpen;
    private int pageWaitTicks;
    private int merchantCheckTicks;
    /** Предметы, чьи иконки уже отправлены странице: аукцион показывает любые предметы, иконки берём из ресурсов игры. */
    private final java.util.Set<String> iconsSent = new java.util.HashSet<>();
    private Boolean merchantNearSent;

    public LumenWebScreen() {
        this("profile");
    }

    public LumenWebScreen(String initialTab) {
        this(initialTab, false);
    }

    public LumenWebScreen(String initialTab, boolean standalone) {
        super(Component.translatable("gui.aqualumen.hub"));
        this.initialTab = initialTab == null || initialTab.isBlank() ? "profile" : initialTab;
        this.standalone = standalone;
    }

    @Override
    protected void init() {
        double scale = minecraft.getWindow().getGuiScale();
        bridge = new LumenWebBridge((int) Math.ceil(width * scale), (int) Math.ceil(height * scale));
        if (!bridge.isAvailable()) {
            abort("браузер меню не создан");
            return;
        }
        pendingSnapshot = LumenClient.snapshot();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        if (bridge != null) {
            double scale = minecraft.getWindow().getGuiScale();
            bridge.resize((int) Math.ceil(width * scale), (int) Math.ceil(height * scale));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (bridge == null) {
            return;
        }
        String message;
        while (bridge != null && (message = bridge.pollMessage()) != null) {
            dispatch(message);
        }
        if (bridge == null) {
            return;
        }
        if (!pageReady && ++pageWaitTicks >= PAGE_READY_TIMEOUT_TICKS) {
            abort("страница меню не загрузилась за 15 секунд");
            return;
        }
        pushSnapshot();
        pushMerchantNear();
    }

    /** Раз в полсекунды говорит странице, стоит ли торговец рядом: от этого зависят кнопки продажи рыбы. */
    private void pushMerchantNear() {
        if (!pageReady || bridge == null || minecraft == null || minecraft.player == null) {
            return;
        }
        if (merchantNearSent != null && ++merchantCheckTicks < 10) {
            return;
        }
        merchantCheckTicks = 0;
        boolean near = FishMerchantProximity.isNear(minecraft.player);
        if (merchantNearSent == null || merchantNearSent != near) {
            merchantNearSent = near;
            bridge.execute("if(window.AquaLumen&&window.AquaLumen.setMerchantNear){window.AquaLumen.setMerchantNear(" + near + ");}");
        }
    }

    private void abort(String reason) {
        AquaLumenUI.LOGGER.warn("[AquaLumen CEF] {}", reason);
        if (bridge != null) {
            bridge.close();
            bridge = null;
        }
        if (minecraft != null && minecraft.screen == this) {
            minecraft.setScreen(null);
            LumenClient.reportHubUnavailable(reason);
        }
    }

    @Override
    public void refresh(HubSnapshot snapshot) {
        pendingSnapshot = snapshot;
        pushSnapshot();
    }

    private void pushSnapshot() {
        if (bridge == null) {
            return;
        }
        if (pendingSnapshot == null) {
            pendingSnapshot = LumenClient.snapshot();
        }
        if (pendingSnapshot == null) {
            return;
        }
        String json = HubSnapshotJson.encode(pendingSnapshot, LumenClient.snapshotReceivedAt(), initialTab, standalone);
        bridge.execute("if(window.AquaLumen&&window.AquaLumen.applySnapshot){window.AquaLumen.applySnapshot(" + json + ");}");
        if (pageReady) {
            pushItemIcons(pendingSnapshot);
            pendingSnapshot = null;
        }
    }

    /** Иконки предметов аукциона, которых нет в странице: берутся из моделей предметов, без OpenGL. */
    private void pushItemIcons(HubSnapshot snapshot) {
        if (snapshot == null || snapshot.market() == null || bridge == null) {
            return;
        }
        JsonObject icons = new JsonObject();
        for (HubSnapshot.MarketEntry lot : snapshot.market()) {
            String id = lot.itemId();
            if (id == null || id.isBlank() || !iconsSent.add(id)) {
                continue;
            }
            String url = ItemIconResolver.dataUrl(id);
            if (url != null) {
                icons.addProperty(id, url);
            }
        }
        if (icons.size() > 0) {
            bridge.execute("if(window.AquaLumen&&window.AquaLumen.setItemIcons){window.AquaLumen.setItemIcons(" + icons + ");}");
        }
    }

    private void dispatch(String message) {
        try {
            JsonObject root = JsonParser.parseString(message).getAsJsonObject();
            String type = text(root, "type");
            switch (type) {
                case "ready" -> {
                    if (root.has("page") && !root.get("page").getAsBoolean()) {
                        abort("скрипт страницы меню упал");
                        return;
                    }
                    pageReady = true;
                    pendingSnapshot = LumenClient.snapshot();
                }
                case "modal" -> modalOpen = root.has("open") && root.get("open").getAsBoolean();
                case "action" -> dispatchAction(text(root, "action"), text(root, "argument"));
                case "settings" -> updateSettings(root);
                default -> AquaLumenUI.LOGGER.debug("[AquaLumen CEF] dropped IPC type '{}'", type);
            }
        } catch (RuntimeException error) {
            AquaLumenUI.LOGGER.debug("[AquaLumen CEF] invalid IPC: {}", error.toString());
        }
    }

    private void dispatchAction(String action, String argument) {
        if ("hub.close".equals(action)) {
            onClose();
            return;
        }
        if (!SERVER_ACTIONS.contains(action) || argument.length() > 96) {
            AquaLumenUI.LOGGER.warn("[AquaLumen CEF] rejected action '{}'", action);
            return;
        }
        LumenClient.sendAction(action, argument);
    }

    private void updateSettings(JsonObject root) {
        if (root.has("theme")) {
            String theme = root.get("theme").getAsString();
            if (Set.of("aqua_lumen", "violet_lumen", "midnight_rose").contains(theme)) {
                LumenConfig.CLIENT.theme.set(theme);
            }
        }
        if (root.has("animations")) {
            LumenConfig.CLIENT.animations.set(root.get("animations").getAsBoolean());
        }
        pendingSnapshot = LumenClient.snapshot();
        pushSnapshot();
    }

    private static String text(JsonObject root, String name) {
        return root.has(name) && root.get(name).isJsonPrimitive() ? root.get(name).getAsString() : "";
    }

    private int cefX(double mouseX) {
        return (int) Math.round(mouseX * minecraft.getWindow().getGuiScale());
    }

    private int cefY(double mouseY) {
        return (int) Math.round(mouseY * minecraft.getWindow().getGuiScale());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (LumenConfig.CLIENT.blurBackground.get()) {
            renderBackground(graphics);
        }
        graphics.fill(0, 0, width, height, 0x9C050B11);
        if (bridge != null && bridge.isAvailable()) {
            bridge.blit(0, 0, width, height);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendMousePress(cefX(mouseX), cefY(mouseY), button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendMouseRelease(cefX(mouseX), cefY(mouseY), button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendMouseMove(cefX(mouseX), cefY(mouseY));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendMouseWheel(cefX(mouseX), cefY(mouseY), delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (modalOpen && bridge != null) {
                bridge.execute("window.AquaLumen&&window.AquaLumen.escapeLayer&&window.AquaLumen.escapeLayer();");
                // один Esc закрывает слой, следующий за ним закрывает сам хаб:
                // флаг модалок мог залипнуть, поэтому всегда отпускаем его здесь
                modalOpen = false;
            } else {
                onClose();
            }
            return true;
        }
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendKeyPress(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendKeyRelease(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (bridge != null && bridge.isAvailable()) {
            bridge.sendKeyTyped(codePoint, modifiers);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        if (bridge != null) {
            bridge.close();
            bridge = null;
        }
        LumenClient.sendAction("hub.close", "");
        super.onClose();
    }
}
