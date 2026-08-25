package store.aquateche.aqualumen.client.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import store.aquateche.aqualumen.client.theme.LumenTheme;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.config.LumenConfig;

public final class HubSnapshotJson {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private HubSnapshotJson() {
    }

    public static String encode(HubSnapshot snapshot, long receivedAt, String initialTab) {
        JsonObject root = new JsonObject();
        root.add("snapshot", GSON.toJsonTree(snapshot));
        root.addProperty("receivedAt", receivedAt);
        root.addProperty("initialTab", initialTab);
        root.addProperty("openKey", "F4");
        root.addProperty("playerHead", PlayerHeadCapture.dataUrl(
                snapshot != null && snapshot.profile() != null ? snapshot.profile().name() : ""));

        JsonArray tabs = new JsonArray();
        LumenConfig.COMMON.enabledTabs.get().forEach(tab -> tabs.add(String.valueOf(tab)));
        root.add("enabledTabs", tabs);

        LumenTheme theme = LumenTheme.current();
        JsonObject appearance = new JsonObject();
        appearance.addProperty("theme", theme.id());
        appearance.addProperty("accent", color(theme.accent()));
        appearance.addProperty("accentAlt", color(theme.accentAlt()));
        appearance.addProperty("animations", LumenConfig.CLIENT.animations.get());
        appearance.addProperty("compact", LumenConfig.CLIENT.compactMode.get());
        appearance.addProperty("panelOpacity", LumenConfig.CLIENT.panelOpacity.get());
        root.add("appearance", appearance);
        return GSON.toJson(root);
    }

    private static String color(int argb) {
        return "#%06X".formatted(argb & 0x00FFFFFF);
    }
}
