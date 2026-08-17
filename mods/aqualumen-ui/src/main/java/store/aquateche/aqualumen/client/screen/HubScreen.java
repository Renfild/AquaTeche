package store.aquateche.aqualumen.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import store.aquateche.aqualumen.client.LumenClient;
import store.aquateche.aqualumen.client.render.Anim;
import store.aquateche.aqualumen.client.render.Gfx;
import store.aquateche.aqualumen.client.render.HubFont;
import store.aquateche.aqualumen.client.render.Icons;
import store.aquateche.aqualumen.client.theme.LumenTheme;
import store.aquateche.aqualumen.client.widget.LumenWidgets;
import store.aquateche.aqualumen.common.data.HubSnapshot;
import store.aquateche.aqualumen.config.LumenConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * The hub screen. Fixed three zone shell: header, sidebar, content.
 * Switching a tab only swaps the content zone, so navigation never moves under the cursor.
 */
public final class HubScreen extends Screen implements HubSnapshotScreen {

    private static final int SIDEBAR = 104;
    private static final int HEADER = 34;
    private static final int FOOTER = 26;

    private final List<LumenWidgets.NavButton> navButtons = new ArrayList<>();
    private List<HubTabs.Tab> tabs = List.of();

    private LumenTheme theme = LumenTheme.current();
    private HubTabs.Tab tab = HubTabs.Tab.PROFILE;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private float time;

    public HubScreen() {
        super(Component.translatable("gui.aqualumen.hub"));
    }

    @Override
    protected void init() {
        theme = LumenTheme.current();
        HubFx.reset();
        boolean compact = LumenConfig.CLIENT.compactMode.get();
        panelWidth = Math.min(this.width - 24, compact ? 400 : 456);
        panelHeight = Math.min(this.height - 24, compact ? 224 : 250);
        left = (this.width - panelWidth) / 2;
        top = (this.height - panelHeight) / 2;

        navButtons.clear();
        HubSnapshot snapshot = LumenClient.snapshot();
        tabs = HubTabs.enabled();
        if (!tabs.contains(tab) && !tabs.isEmpty()) {
            tab = tabs.get(0);
        }

        int y = top + HEADER + 6;
        for (HubTabs.Tab value : tabs) {
            LumenWidgets.NavButton button = new LumenWidgets.NavButton(
                    left + 8, y, SIDEBAR - 16, 22,
                    Component.translatable(value.translationKey()),
                    value.badge(snapshot), value.icon(), theme, pressed -> select(value));
            button.setSelected(value == tab);
            navButtons.add(button);
            addRenderableWidget(button);
            y += 25;
        }

        addRenderableWidget(new LumenWidgets.PillButton(
                left + panelWidth - 152, top + panelHeight - 21, 72, 16,
                Component.translatable("gui.aqualumen.action.refresh"), false, theme,
                () -> {
                    LumenClient.sendAction("hub.refresh", "");
                    HubFx.toast("\u0414\u0430\u043d\u043d\u044b\u0435 \u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u044b",
                            Icons.Icon.REFRESH, theme.accent());
                }));
        addRenderableWidget(new LumenWidgets.PillButton(
                left + panelWidth - 74, top + panelHeight - 21, 66, 16,
                Component.translatable("gui.aqualumen.action.close"), true, theme, this::onClose));
    }

    public void refresh() {
        HubSnapshot snapshot = LumenClient.snapshot();
        for (int i = 0; i < navButtons.size(); i++) {
            if (i < tabs.size()) {
                navButtons.get(i).setBadge(tabs.get(i).badge(snapshot));
            }
        }
    }

    @Override
    public void refresh(HubSnapshot snapshot) {
        refresh();
    }

    private void select(HubTabs.Tab value) {
        HubFx.switchTab(tab.ordinal(), value.ordinal());
        tab = value;
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setSelected(i < tabs.size() && tabs.get(i) == value);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float delta = Anim.delta();
        HubFx.tick(delta);
        float reveal = HubFx.open();
        if (LumenConfig.CLIENT.animations.get()) {
            time += partialTick;
        }

        if (LumenConfig.CLIENT.blurBackground.get()) {
            renderBackground(graphics);
        }
        graphics.fill(0, 0, this.width, this.height, Gfx.withAlpha(theme.canvas(), 0.66F * reveal));

        int breath = Math.round((3.0F + 2.0F * Anim.pulse(time, 0.03F)) * reveal);
        Gfx.glow(graphics, left, top, panelWidth, panelHeight, 14, theme.accent(), Math.max(1, breath));
        Gfx.gradientRounded(graphics, left, top, panelWidth, panelHeight, 14,
                Anim.fade(theme.panel(), reveal), Anim.fade(theme.shade(theme.canvas(), 0.94F), reveal));
        Gfx.outline(graphics, left, top, panelWidth, panelHeight, 14, Anim.fade(theme.border(), reveal));

        renderHeader(graphics);
        graphics.fill(left + SIDEBAR, top + HEADER, left + SIDEBAR + 1, top + panelHeight - FOOTER, theme.border());

        super.render(graphics, mouseX, mouseY, partialTick);

        int contentX = left + SIDEBAR + 12;
        int contentY = top + HEADER + 8;
        int contentWidth = panelWidth - SIDEBAR - 24;
        int contentHeight = panelHeight - HEADER - FOOTER - 12;
        graphics.enableScissor(contentX - 6, contentY - 4, contentX + contentWidth + 6, contentY + contentHeight + 4);
        HubTabs.render(graphics, this.font, theme, LumenClient.snapshot(), tab,
                contentX + HubFx.slide(12), contentY, contentWidth, contentHeight, mouseX, mouseY, time);
        graphics.disableScissor();

        renderFooter(graphics);
        HubFx.render(graphics, this.font, theme, left + panelWidth - 10, top + panelHeight - FOOTER - 8);
    }

    private void renderHeader(GuiGraphics graphics) {
        HubSnapshot snapshot = LumenClient.snapshot();
        String serverName = snapshot == null ? "aquaTeche" : snapshot.server().name();

        Icons.drawCentered(graphics, Icons.Icon.WAVE, left + 15, top + 17, 10,
                Gfx.lerpColor(theme.accent(), theme.accentAlt(), Anim.pulse(time, 0.02F)));
        HubFont.draw(graphics, this.font, serverName, left + 25, top + 12, theme.text());

        if (snapshot != null) {
            String online = snapshot.server().online() + "/" + snapshot.server().slots();
            String tps = String.format("%.1f TPS", snapshot.server().tps());
            chip(graphics, left + panelWidth - 186, top + 8, 58, online, theme.accent(), Icons.Icon.PLAYER);
            chip(graphics, left + panelWidth - 124, top + 8, 58, tps, theme.success(), Icons.Icon.BOLT);
            chip(graphics, left + panelWidth - 62, top + 8, 54, compact(snapshot.wallet().gems()), theme.gold(),
                    Icons.Icon.GEM);
        }

        graphics.fill(left + 1, top + HEADER, left + panelWidth - 1, top + HEADER + 1, theme.border());
    }

    private void chip(GuiGraphics graphics, int x, int y, int width, String text, int accent, Icons.Icon icon) {
        Gfx.roundedRect(graphics, x, y, width, 17, 8, theme.shade(theme.raised(), 0.9F));
        Gfx.outline(graphics, x, y, width, 17, 8, theme.border());
        Icons.drawCentered(graphics, icon, x + 10, y + 9, 8, accent);
        HubFont.draw(graphics, this.font, text, x + 17, y + 5, accent);
    }

    private void renderFooter(GuiGraphics graphics) {
        int y = top + panelHeight - FOOTER;
        graphics.fill(left + 1, y, left + panelWidth - 1, y + 1, theme.border());
        HubSnapshot snapshot = LumenClient.snapshot();
        String build = snapshot == null ? "AquaLumen UI" : "AquaLumen UI " + snapshot.server().build();
        HubFont.draw(graphics, this.font, build, left + 12, y + 9, theme.textDim());
    }

    private static String compact(long value) {
        if (value >= 1_000_000L) {
            return String.format("%.1fM", value / 1_000_000.0);
        }
        if (value >= 1_000L) {
            return String.format("%.1fk", value / 1_000.0);
        }
        return Long.toString(value);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }
        int contentX = left + SIDEBAR + 12;
        int contentY = top + HEADER + 8;
        int contentWidth = panelWidth - SIDEBAR - 24;
        int contentHeight = panelHeight - HEADER - FOOTER - 12;
        return HubTabs.click(LumenClient.snapshot(), tab, contentX, contentY, contentWidth, contentHeight,
                (int) mouseX, (int) mouseY);
    }

    @Override
    public void onClose() {
        LumenClient.sendAction("hub.close", "");
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
