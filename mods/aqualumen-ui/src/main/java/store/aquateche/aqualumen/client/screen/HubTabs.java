package store.aquateche.aqualumen.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Content of every hub tab. Only this zone changes when the player switches sections. */
public final class HubTabs {

    private HubTabs() {
    }

    public enum Tab {
        PROFILE("profile", Icons.Icon.PLAYER),
        STORE("store", Icons.Icon.BAG),
        CASES("cases", Icons.Icon.CASE),
        PASS("pass", Icons.Icon.STAR),
        TOPS("tops", Icons.Icon.CHART),
        SETTINGS("settings", Icons.Icon.GEAR);

        private final String key;
        private final Icons.Icon icon;

        Tab(String key, Icons.Icon icon) {
            this.key = key;
            this.icon = icon;
        }

        public String id() {
            return key;
        }

        public Icons.Icon icon() {
            return icon;
        }

        public String translationKey() {
            return "gui.aqualumen.tab." + key;
        }

        public String badge(@Nullable HubSnapshot snapshot) {
            if (snapshot == null) {
                return "";
            }
            return switch (this) {
                case CASES -> {
                    int total = snapshot.cases().stream().mapToInt(HubSnapshot.CaseEntry::count).sum();
                    yield total > 0 ? String.valueOf(total) : "";
                }
                case PASS -> snapshot.season().claimable() > 0 ? String.valueOf(snapshot.season().claimable()) : "";
                default -> "";
            };
        }
    }

    public static List<Tab> enabled() {
        List<Tab> out = new ArrayList<>();
        for (String raw : LumenConfig.COMMON.enabledTabs.get()) {
            String id = raw.trim();
            for (Tab tab : Tab.values()) {
                if (tab.id().equalsIgnoreCase(id)) {
                    out.add(tab);
                    break;
                }
            }
        }
        return out.isEmpty() ? List.of(Tab.values()) : List.copyOf(out);
    }

    public static void render(GuiGraphics graphics, Font font, LumenTheme theme, @Nullable HubSnapshot snapshot,
                              Tab tab, int x, int y, int width, int height, int mouseX, int mouseY, float time) {
        if (snapshot == null) {
            HubFont.draw(graphics, font, "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430...", x + 4, y + 4, theme.textDim());
            return;
        }
        switch (tab) {
            case PROFILE -> profile(graphics, font, theme, snapshot, x, y, width, height, time);
            case STORE -> store(graphics, font, theme, snapshot, x, y, width, height, mouseX, mouseY);
            case CASES -> cases(graphics, font, theme, snapshot, x, y, width, height);
            case PASS -> pass(graphics, font, theme, snapshot, x, y, width, height, time);
            case TOPS -> tops(graphics, font, theme, snapshot, x, y, width, height);
            case SETTINGS -> settings(graphics, font, theme, x, y, width, height);
        }
    }

    private static void profile(GuiGraphics graphics, Font font, LumenTheme theme, HubSnapshot snapshot,
                                int x, int y, int width, int height, float time) {
        HubSnapshot.Profile profile = snapshot.profile();

        int heroHeight = 78;
        LumenWidgets.card(graphics, theme, x, y, width, heroHeight, null);

        float reveal = HubFx.enter();
        float pulse = Anim.pulse(time, 0.05F);
        Gfx.ring(graphics, x + 44, y + heroHeight / 2, 26, 5, profile.levelProgress() * reveal,
                theme.shade(theme.surface(), 0.9F), theme.accent(),
                Gfx.lerpColor(theme.accentAlt(), theme.accent(), pulse));
        HubFont.centered(graphics, font, String.valueOf(profile.level()), x + 44, y + heroHeight / 2 - 8, theme.text());
        HubFont.centered(graphics, font, "LVL", x + 44, y + heroHeight / 2 + 3, theme.textDim());

        HubFont.draw(graphics, font, profile.name(), x + 82, y + 18, theme.text());
        HubFont.draw(graphics, font, profile.rank(), x + 82, y + 32, 0xFF000000 | profile.rankColor());

        Gfx.progressBar(graphics, x + 82, y + 48, width - 100, 6, profile.levelProgress() * reveal,
                theme.shade(theme.surface(), 0.9F), theme.accent(), theme.accentAlt());
        HubFont.draw(graphics, font,
                Math.round(profile.levelProgress() * 100) + "% \u0434\u043e \u0443\u0440\u043e\u0432\u043d\u044f " + (profile.level() + 1),
                x + 82, y + 58, theme.textDim());

        int tileWidth = (width - 24) / 4;
        int tileY = y + heroHeight + 10;
        int[] lift = new int[4];
        for (int i = 0; i < lift.length; i++) {
            lift[i] = Math.round((1.0F - Anim.easeOutCubic(Anim.stagger(reveal, i, lift.length, 0.55F))) * 6.0F);
        }
        LumenWidgets.statTile(graphics, theme, x, tileY + lift[0], tileWidth, 40,
                formatHours(profile.playtimeMinutes()), "\u0432 \u0438\u0433\u0440\u0435", theme.accent(),
                Icons.Icon.CLOCK);
        LumenWidgets.statTile(graphics, theme, x + tileWidth + 8, tileY + lift[1], tileWidth, 40,
                String.valueOf(profile.kills()), "\u0443\u0431\u0438\u0439\u0441\u0442\u0432", theme.accentAlt(),
                Icons.Icon.BOLT);
        LumenWidgets.statTile(graphics, theme, x + (tileWidth + 8) * 2, tileY + lift[2], tileWidth, 40,
                String.valueOf(profile.deaths()), "\u0441\u043c\u0435\u0440\u0442\u0435\u0439", theme.danger(),
                Icons.Icon.SHIELD);
        LumenWidgets.statTile(graphics, theme, x + (tileWidth + 8) * 3, tileY + lift[3], tileWidth, 40,
                String.valueOf(profile.friendsOnline()), "\u0434\u0440\u0443\u0437\u0435\u0439", theme.success(),
                Icons.Icon.PLAYER);

        int dailyY = tileY + 48;
        if (dailyY + 34 <= y + height) {
            LumenWidgets.card(graphics, theme, x, dailyY, width, 34, null);
            Icons.drawCentered(graphics, Icons.Icon.CASE, x + 20, dailyY + 17, 10,
                    snapshot.wallet().dailyAvailable() ? theme.accent() : theme.textDim());
            HubFont.draw(graphics, font,
                    "\u0415\u0436\u0435\u0434\u043d\u0435\u0432\u043d\u0430\u044f \u043d\u0430\u0433\u0440\u0430\u0434\u0430 \u2022 \u0441\u0435\u0440\u0438\u044f " + snapshot.wallet().dailyStreak(),
                    x + 32, dailyY + 13, theme.text());
            String state = snapshot.wallet().dailyAvailable()
                    ? "\u0413\u043e\u0442\u043e\u0432\u043e"
                    : "\u0417\u0430\u0432\u0442\u0440\u0430";
            int chipWidth = HubFont.width(font, state) + 16;
            Gfx.roundedRect(graphics, x + width - chipWidth - 12, dailyY + 8, chipWidth, 18, 9,
                    Gfx.withAlpha(snapshot.wallet().dailyAvailable() ? theme.accent() : theme.textDim(), 0.18F));
            HubFont.draw(graphics, font, state, x + width - chipWidth - 4, dailyY + 13,
                    snapshot.wallet().dailyAvailable() ? theme.accent() : theme.textDim());
        }
    }

    private static void store(GuiGraphics graphics, Font font, LumenTheme theme, HubSnapshot snapshot,
                              int x, int y, int width, int height, int mouseX, int mouseY) {
        List<HubSnapshot.Offer> offers = snapshot.store();
        int columns = 3;
        int cardWidth = (width - (columns - 1) * 8) / columns;
        int cardHeight = 62;

        for (int i = 0; i < offers.size(); i++) {
            int column = i % columns;
            int row = i / columns;
            int cardX = x + column * (cardWidth + 8);
            int cardY = y + row * (cardHeight + 8);
            if (cardY + cardHeight > y + height) {
                break;
            }
            HubSnapshot.Offer offer = offers.get(i);
            boolean hovered = mouseX >= cardX && mouseX <= cardX + cardWidth
                    && mouseY >= cardY && mouseY <= cardY + cardHeight;

            float appear = Anim.easeOutCubic(Anim.stagger(HubFx.enter(), i, Math.max(1, offers.size()), 0.5F));
            cardY += Math.round((1.0F - appear) * 8.0F);
            if (hovered) {
                Gfx.glow(graphics, cardX, cardY, cardWidth, cardHeight, 12, theme.accent(), 3);
            }
            LumenWidgets.card(graphics, theme, cardX, cardY, cardWidth, cardHeight, null);
            boolean gems = String.valueOf(offer.currency()).toLowerCase().contains("gem");
            Icons.badge(graphics, gems ? Icons.Icon.GEM : Icons.Icon.COIN, cardX + 10, cardY + 9, 16,
                    Gfx.withAlpha(theme.accent(), 0.16F), gems ? theme.accentAlt() : theme.accent());
            HubFont.draw(graphics, font, offer.title(), cardX + 30, cardY + 12, theme.text());
            HubFont.draw(graphics, font, offer.subtitle(), cardX + 30, cardY + 24, theme.textDim());

            String price = offer.owned()
                    ? "\u041a\u0443\u043f\u043b\u0435\u043d\u043e"
                    : offer.price() + " " + currency(offer.currency());
            int priceColor = offer.owned() ? theme.success() : theme.gold();
            Icons.drawCentered(graphics, offer.owned() ? Icons.Icon.CHECK : Icons.Icon.ARROW,
                    cardX + 16, cardY + 46, 8, priceColor);
            HubFont.draw(graphics, font, price, cardX + 24, cardY + 42, priceColor);

            if (!offer.badge().isEmpty()) {
                int badgeWidth = HubFont.width(font, offer.badge()) + 12;
                Gfx.roundedRect(graphics, cardX + cardWidth - badgeWidth - 10, cardY + 9, badgeWidth, 15, 7,
                        Gfx.withAlpha(theme.accentAlt(), 0.22F));
                HubFont.draw(graphics, font, offer.badge(), cardX + cardWidth - badgeWidth - 4, cardY + 13,
                        theme.accentAlt());
            }
        }

        HubFont.draw(graphics, font,
                "\u041f\u043e\u043a\u0430 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u2014 \u044d\u043a\u043e\u043d\u043e\u043c\u0438\u043a\u0430 \u0435\u0449\u0451 \u043d\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0430",
                x, y + height - 9, theme.textDim());
    }

    private static void cases(GuiGraphics graphics, Font font, LumenTheme theme, HubSnapshot snapshot,
                              int x, int y, int width, int height) {
        int rowHeight = 40;
        int index = 0;
        for (HubSnapshot.CaseEntry entry : snapshot.cases()) {
            int rowY = y + index * (rowHeight + 8);
            if (rowY + rowHeight > y + height) {
                break;
            }
            LumenWidgets.card(graphics, theme, x, rowY, width, rowHeight, null);
            int accent = switch (entry.rarity()) {
                case "legendary" -> theme.gold();
                case "rare" -> theme.accentAlt();
                default -> theme.accent();
            };
            Icons.badge(graphics, Icons.Icon.CASE, x + 10, rowY + 10, 20, Gfx.withAlpha(accent, 0.22F), accent);
            HubFont.draw(graphics, font, entry.title(), x + 40, rowY + 12, theme.text());
            HubFont.draw(graphics, font, "\u0432 \u043d\u0430\u043b\u0438\u0447\u0438\u0438: " + entry.count(),
                    x + 40, rowY + 24, theme.textDim());

            String action = entry.count() > 0
                    ? "\u041e\u0442\u043a\u0440\u044b\u0442\u044c"
                    : "\u041d\u0435\u0442 \u043a\u043b\u044e\u0447\u0435\u0439";
            int actionWidth = HubFont.width(font, action) + 20;
            if (entry.count() > 0) {
                Gfx.gradientRoundedH(graphics, x + width - actionWidth - 22, rowY + 11, actionWidth + 12, 18, 9,
                        theme.accent(), theme.accentAlt());
                Icons.drawCentered(graphics, Icons.Icon.KEY, x + width - actionWidth - 10, rowY + 20, 8, 0xFF08131A);
                HubFont.draw(graphics, font, action, x + width - actionWidth, rowY + 16, 0xFF08131A);
            } else {
                Gfx.roundedRect(graphics, x + width - actionWidth - 22, rowY + 11, actionWidth + 12, 18, 9,
                        theme.shade(theme.surface(), 0.8F));
                Icons.drawCentered(graphics, Icons.Icon.LOCK, x + width - actionWidth - 10, rowY + 20, 8,
                        theme.textDim());
                HubFont.draw(graphics, font, action, x + width - actionWidth, rowY + 16, theme.textDim());
            }
            index++;
        }
    }

    private static void pass(GuiGraphics graphics, Font font, LumenTheme theme, HubSnapshot snapshot,
                             int x, int y, int width, int height, float time) {
        HubSnapshot.Season season = snapshot.season();

        LumenWidgets.card(graphics, theme, x, y, width, 52, null);
        HubFont.draw(graphics, font, season.title(), x + 14, y + 14, theme.text());
        HubFont.draw(graphics, font,
                "\u0423\u0440\u043e\u0432\u0435\u043d\u044c " + season.tier() + " \u0438\u0437 " + season.maxTier(),
                x + 14, y + 26, theme.textDim());
        Gfx.progressBar(graphics, x + 14, y + 38, width - 28, 6, season.tierProgress() * HubFx.enter(),
                theme.shade(theme.surface(), 0.9F), theme.accent(), theme.accentAlt());

        int trackY = y + 62;
        int nodes = 8;
        int nodeWidth = (width - (nodes - 1) * 6) / nodes;
        for (int i = 0; i < nodes; i++) {
            int nodeX = x + i * (nodeWidth + 6);
            boolean unlocked = i < Math.min(nodes, season.claimable() + 2);
            int color = unlocked ? theme.accent() : theme.textDim();
            Gfx.roundedRect(graphics, nodeX, trackY, nodeWidth, 34, 10,
                    Gfx.withAlpha(color, unlocked ? 0.20F : 0.10F));
            Gfx.outline(graphics, nodeX, trackY, nodeWidth, 34, 10, theme.border());
            HubFont.centered(graphics, font, String.valueOf(season.tier() + i + 1),
                    nodeX + nodeWidth / 2, trackY + 5, unlocked ? theme.text() : theme.textDim());
            Icons.drawCentered(graphics, unlocked ? Icons.Icon.CHECK : Icons.Icon.LOCK,
                    nodeX + nodeWidth / 2, trackY + 23, 10, color);
            if (!unlocked) {
                float sweep = Anim.shimmer(time, i * 14.0F, nodes * 14.0F);
                if (sweep > 0.0F) {
                    Gfx.roundedRect(graphics, nodeX, trackY, nodeWidth, 34, 10,
                            Gfx.withAlpha(theme.text(), 0.07F * sweep));
                }
            }
        }

        int premiumY = trackY + 44;
        if (premiumY + 30 <= y + height) {
            Gfx.gradientRoundedH(graphics, x, premiumY, width, 30, 10,
                    Gfx.withAlpha(theme.gold(), 0.16F), Gfx.withAlpha(theme.accentAlt(), 0.16F));
            Gfx.outline(graphics, x, premiumY, width, 30, 10, theme.border());
            HubFont.draw(graphics, font, season.premium()
                            ? "Premium \u0430\u043a\u0442\u0438\u0432\u0435\u043d"
                            : "Premium \u043f\u0440\u043e\u043f\u0443\u0441\u043a \u2014 \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0430\u0433\u0440\u0430\u0434 \u043d\u0430 \u043a\u0430\u0436\u0434\u043e\u043c \u0443\u0440\u043e\u0432\u043d\u0435",
                    x + 14, premiumY + 11, theme.gold());
        }
    }

    private static void tops(GuiGraphics graphics, Font font, LumenTheme theme, HubSnapshot snapshot,
                             int x, int y, int width, int height) {
        int rowHeight = 20;
        int index = 0;
        for (HubSnapshot.TopEntry entry : snapshot.tops()) {
            int rowY = y + index * (rowHeight + 4);
            if (rowY + rowHeight > y + height) {
                break;
            }
            int background = entry.self()
                    ? Gfx.withAlpha(theme.accent(), 0.14F)
                    : theme.shade(theme.raised(), 0.75F);
            Gfx.roundedRect(graphics, x, rowY, width, rowHeight, 8, background);
            int placeColor = switch (entry.place()) {
                case 1 -> theme.gold();
                case 2 -> theme.accentAlt();
                case 3 -> theme.accent();
                default -> theme.textDim();
            };
            HubFont.draw(graphics, font, "#" + entry.place(), x + 10, rowY + 6, placeColor);
            if (entry.place() <= 3) {
                Icons.drawCentered(graphics, Icons.Icon.STAR, x + 32, rowY + 10, 8, placeColor);
            } else if (entry.self()) {
                Icons.drawCentered(graphics, Icons.Icon.PLAYER, x + 32, rowY + 10, 8, theme.accent());
            }
            HubFont.draw(graphics, font, entry.player(), x + 42, rowY + 6, theme.text());
            HubFont.draw(graphics, font, entry.value(), x + width - HubFont.width(font, entry.value()) - 12, rowY + 6,
                    theme.textDim());
            index++;
        }
        if (index == 0) {
            HubFont.draw(graphics, font, "\u041d\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0445", x, y, theme.textDim());
        }
    }

    private static void settings(GuiGraphics graphics, Font font, LumenTheme theme,
                                 int x, int y, int width, int height) {
        String[][] rows = {
                {"\u0422\u0435\u043c\u0430 \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430", theme.id()},
                {"\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u043f\u0430\u043d\u0435\u043b\u0438", "\u043a\u043e\u043d\u0444\u0438\u0433"},
                {"\u0410\u043d\u0438\u043c\u0430\u0446\u0438\u0438", "\u0432\u043a\u043b/\u0432\u044b\u043a\u043b"},
                {"\u041a\u043e\u043c\u043f\u0430\u043a\u0442\u043d\u044b\u0439 \u0440\u0435\u0436\u0438\u043c", "\u0434\u043b\u044f \u043c\u0430\u043b\u044b\u0445 \u043e\u043a\u043e\u043d"},
                {"\u0413\u043e\u0440\u044f\u0447\u0430\u044f \u043a\u043b\u0430\u0432\u0438\u0448\u0430", "H"}
        };
        Icons.Icon[] icons = {Icons.Icon.WAVE, Icons.Icon.SHIELD, Icons.Icon.BOLT, Icons.Icon.CHART, Icons.Icon.KEY};
        for (int i = 0; i < rows.length; i++) {
            int rowY = y + i * 26;
            if (rowY + 22 > y + height) {
                break;
            }
            Gfx.roundedRect(graphics, x, rowY, width, 22, 8, theme.shade(theme.raised(), 0.7F));
            Icons.drawCentered(graphics, icons[Math.min(i, icons.length - 1)], x + 16, rowY + 11, 8, theme.accent());
            HubFont.draw(graphics, font, rows[i][0], x + 28, rowY + 7, theme.text());
            HubFont.draw(graphics, font, rows[i][1], x + width - HubFont.width(font, rows[i][1]) - 12, rowY + 7,
                    theme.accent());
        }
        HubFont.draw(graphics, font,
                "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0445\u0440\u0430\u043d\u044f\u0442\u0441\u044f \u0432 config/aqualumen-client.toml",
                x, y + height - 9, theme.textDim());
    }

    private static String currency(String currency) {
        return "gems".equals(currency) ? "\u043a\u0440" : "\u043c\u043e\u043d";
    }

    private static String formatHours(long minutes) {
        long hours = minutes / 60;
        return hours > 0 ? hours + " \u0447" : minutes + " \u043c";
    }

    public static boolean click(@Nullable HubSnapshot snapshot, Tab tab, int x, int y, int width, int height,
                                int mouseX, int mouseY) {
        if (snapshot == null) {
            return false;
        }
        return switch (tab) {
            case PROFILE -> clickDaily(snapshot, x, y, width, height, mouseX, mouseY);
            case STORE -> clickStore(snapshot, x, y, width, height, mouseX, mouseY);
            case CASES -> clickCases(snapshot, x, y, width, height, mouseX, mouseY);
            case PASS -> clickPass(snapshot, x, y, width, height, mouseX, mouseY);
            default -> false;
        };
    }

    private static boolean clickDaily(HubSnapshot snapshot, int x, int y, int width, int height,
                                      int mouseX, int mouseY) {
        int tileY = y + 78 + 10;
        int dailyY = tileY + 48;
        if (dailyY + 34 > y + height) {
            return false;
        }
        if (mouseX >= x && mouseX <= x + width && mouseY >= dailyY && mouseY <= dailyY + 34) {
            LumenClient.sendAction("daily.claim", "");
            return true;
        }
        return false;
    }

    private static boolean clickStore(HubSnapshot snapshot, int x, int y, int width, int height,
                                      int mouseX, int mouseY) {
        List<HubSnapshot.Offer> offers = snapshot.store();
        int columns = 3;
        int cardWidth = (width - (columns - 1) * 8) / columns;
        int cardHeight = 62;
        for (int i = 0; i < offers.size(); i++) {
            int column = i % columns;
            int row = i / columns;
            int cardX = x + column * (cardWidth + 8);
            int cardY = y + row * (cardHeight + 8);
            if (cardY + cardHeight > y + height) {
                break;
            }
            if (mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= cardY && mouseY <= cardY + cardHeight) {
                LumenClient.sendAction("store.buy", offers.get(i).id());
                return true;
            }
        }
        return false;
    }

    private static boolean clickCases(HubSnapshot snapshot, int x, int y, int width, int height,
                                      int mouseX, int mouseY) {
        int rowHeight = 40;
        int index = 0;
        for (HubSnapshot.CaseEntry entry : snapshot.cases()) {
            int rowY = y + index * (rowHeight + 8);
            if (rowY + rowHeight > y + height) {
                break;
            }
            if (entry.count() > 0 && mouseX >= x && mouseX <= x + width
                    && mouseY >= rowY && mouseY <= rowY + rowHeight) {
                LumenClient.sendAction("case.open", entry.id());
                return true;
            }
            index++;
        }
        return false;
    }

    private static boolean clickPass(HubSnapshot snapshot, int x, int y, int width, int height,
                                     int mouseX, int mouseY) {
        int trackY = y + 62;
        int nodes = 8;
        int nodeWidth = (width - (nodes - 1) * 6) / nodes;
        HubSnapshot.Season season = snapshot.season();
        for (int i = 0; i < nodes; i++) {
            int nodeX = x + i * (nodeWidth + 6);
            if (mouseX >= nodeX && mouseX <= nodeX + nodeWidth && mouseY >= trackY && mouseY <= trackY + 34) {
                LumenClient.sendAction("pass.claim", String.valueOf(season.tier() + i + 1));
                return true;
            }
        }
        return false;
    }

    public static void buy(HubSnapshot.Offer offer) {
        LumenClient.sendAction("store.buy", offer.id());
    }

    public static Component title(Tab tab) {
        return Component.translatable(tab.translationKey());
    }
}
