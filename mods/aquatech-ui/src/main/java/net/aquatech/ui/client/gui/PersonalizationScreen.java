package net.aquatech.ui.client.gui;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.cache.ResourceCacheManager;
import net.aquatech.ui.client.gui.widget.AquaButton;
import net.aquatech.ui.client.gui.widget.AquaGlassPanel;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.aquatech.ui.client.render.UiDraw;
import net.aquatech.ui.common.ModClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PersonalizationScreen extends AquaBlurredScreen {

    private static final int AVATAR = 64;
    private ResourceLocation remoteAvatar;
    private String status = "загрузка аватара…";

    public PersonalizationScreen() {
        super(Component.literal("Внешний вид"));
        setEnableAtmosphericParticles(false);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new AquaButton(width / 2 - 110, height / 2 + 92, 100, 22,
                Component.literal("Обновить"), this::reloadAvatar));
        addRenderableWidget(new AquaButton(width / 2 + 10, height / 2 + 92, 100, 22,
                Component.literal("Закрыть"), this::onClose));
        reloadAvatar();
    }

    private void reloadAvatar() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            status = "нет игрока";
            return;
        }
        String uuid = mc.player.getUUID().toString().replace("-", "");
        String url = "https://crafatar.com/avatars/" + uuid + "?size=64&overlay=true";
        status = "загрузка…";
        ResourceCacheManager.getInstance().getOrFetchTexture("avatar_" + uuid, url)
                .whenComplete((loc, err) -> mc.execute(() -> {
                    if (err != null || loc == null) {
                        status = "локальный скин (кэш не ответил)";
                        remoteAvatar = null;
                        return;
                    }
                    remoteAvatar = loc;
                    status = "аватар из кэша";
                }));
    }

    @Override
    protected void renderScreenContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int panelW = 320;
        int panelH = 220;
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        AquaGlassPanel.draw(g, x, y, panelW, panelH, AquaGlassPanel.FILL, AquaGlassPanel.BORDER_HOT, 5, true);
        AquaFontRenderer.drawCenteredHeader(g, font, "Внешний вид", x + panelW / 2, y + 12, COLOR_CYAN_ACCENT);

        String rank = ClientUiState.sessionRankId();
        AquaFontRenderer.drawCentered(g, font, "ранг: " + rank, x + panelW / 2, y + 28, COLOR_TEXT_MUTED);

        int avX = x + 24;
        int avY = y + 52;
        AquaGlassPanel.draw(g, avX - 4, avY - 4, AVATAR + 8, AVATAR + 8, AquaGlassPanel.FILL_LIGHT, AquaGlassPanel.BORDER, 3, false);
        if (remoteAvatar != null) {
            g.blit(remoteAvatar, avX, avY, AVATAR, AVATAR, 0, 0, AVATAR, AVATAR, AVATAR, AVATAR);
        } else if (minecraft.player != null) {
            UiDraw.drawPlayerHead(g, minecraft.player.getUUID(), minecraft.player.getGameProfile().getName(), avX + 16, avY + 16, 32);
        }

        ResourceLocation rankTex = rankTexture(rank);
        int rx = x + 110;
        int ry = y + 56;
        g.blit(rankTex, rx, ry, 16, 16, 0, 0, 8, 8, 8, 8);
        AquaFontRenderer.draw(g, font, status, rx, ry + 22, UiDraw.COLOR_MUTED);
        AquaFontRenderer.drawWrapped(g, font,
                "Иконка ранга из пака. Аватар тянется в ~/.aquatech/cache/textures и живёт между сессиями.",
                rx, ry + 38, panelW - 140, COLOR_TEXT_MUTED);

        String portal = ModClientConfig.PORTAL_BASE.get();
        AquaFontRenderer.draw(g, font, AquaFontRenderer.fit(font, portal, panelW - 32), x + 16, y + panelH - 36, 0xFF64748B);
    }

    static ResourceLocation rankTexture(String rankId) {
        String id = rankId == null || rankId.isBlank() ? "player" : rankId.toLowerCase();
        if (!id.matches("[a-z0-9_]+")) {
            id = "player";
        }
        return new ResourceLocation("aquatech_ui", "textures/ranks/" + id + ".png");
    }
}
