package net.aquatech.ui.client.chat;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.theme.LumenTheme;
import net.aquatech.ui.common.PlayerProfile;
import net.aquatech.ui.client.render.AquaFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AquaChatMessage {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Set<String> SYSTEM_SENDER_NAMES = Set.of(
            "placeholderapi", "viaversion", "protocollib", "luckperms", "essentials",
            "fastasyncworldedit", "worldedit", "vault", "chunky", "skinsrestorer",
            "mohist", "forge", "minecraft", "system", "server", "console", "broadcast",
            "announcement", "warning", "error", "info", "notice", "help", "usage",
            "plugin", "plugins", "discordsrv", "discord", "telegram", "vk", "vkontakte",
            "authme", "floodgate", "geyser", "spark", "coreprotect", "multiverse",
            "citizens", "command", "commands", "quest", "quests", "market", "shop",
            "auction", "backup", "save", "motd", "tip", "alert", "rules", "rule",
            "site", "ip", "status", "bungee", "velocity", "waterfall",
            "home", "sethome", "delhome", "spawn", "warp", "tpa", "tpaccept", "tpdeny",
            "kit", "money", "balance", "pay", "ah", "clan", "party", "region", "rg",
            "worldguard", "world", "cooldown", "teleport", "время", "баланс", "дом", "спавн",
            "варп", "сервер", "система", "ошибка", "внимание", "квесты", "магазин", "аукцион"
    );

    public static class ItemTagRef {
        private final String rawTag;
        private final String displayName;
        private final ItemStack stack;
        private final int rarityColor;

        public ItemTagRef(String rawTag, String displayName, ItemStack stack, int rarityColor) {
            this.rawTag = rawTag;
            this.displayName = displayName;
            this.stack = stack;
            this.rarityColor = rarityColor;
        }

        public String getRawTag() { return rawTag; }
        public String getDisplayName() { return displayName; }
        public ItemStack getStack() { return stack; }
        public int getRarityColor() { return rarityColor; }
    }

    public enum Channel {
        ALL("Все", "ALL", 0xFF00F0FF),       // Electric Cyan Neon
        GLOBAL("Глобал", "G", 0xFF38BDF8),   // Sky Blue
        LOCAL("Локал", "L", 0xFF10B981),     // Emerald Green
        TRADE("Рынок", "T", 0xFFF59E0B),     // Amber Gold
        PRIVATE("ЛС", "PM", 0xFFD946EF),     // Magenta Purple
        SYSTEM("Инфо", "SYS", 0xFF60A5FA);   // Azure Blue

        private final String label;
        private final String tag;
        private final int color;

        Channel(String label, String tag, int color) {
            this.label = label;
            this.tag = tag;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public String getTag() {
            return tag;
        }

        public int getColor() {
            return color;
        }
    }

    private final UUID senderUuid;
    private final String senderName;
    private final String rankId;
    private final String rankDisplay;
    private final int rankColor;
    private final Channel channel;
    private final String messageText;
    private final Component originalComponent;
    private final String timeFormatted;
    private final int creationTick;
    private final boolean isSystem;
    private final long creationTimestamp;
    private ItemStack sharedItem = ItemStack.EMPTY;
    private final List<ItemTagRef> itemTags;
    // Mutable per-instance flag: play mention sound only once
    private boolean mentionSoundPlayed = false;

    public AquaChatMessage(UUID senderUuid, String senderName, String rankId, String rankDisplay,
                           int rankColor, Channel channel, String messageText, Component originalComponent,
                           int creationTick, boolean isSystem) {
        this(senderUuid, senderName, rankId, rankDisplay, rankColor, channel, messageText, originalComponent,
                creationTick, isSystem, ItemStack.EMPTY);
    }

    public AquaChatMessage(UUID senderUuid, String senderName, String rankId, String rankDisplay,
                           int rankColor, Channel channel, String messageText, Component originalComponent,
                           int creationTick, boolean isSystem, ItemStack sharedItem) {
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.rankId = rankId;
        this.rankDisplay = rankDisplay;
        this.rankColor = rankColor;
        this.channel = channel != null ? channel : Channel.ALL;
        this.messageText = messageText != null ? messageText : "";
        this.originalComponent = originalComponent;
        this.timeFormatted = LocalTime.now().format(TIME_FMT);
        this.creationTick = creationTick;
        this.isSystem = isSystem;
        this.creationTimestamp = System.currentTimeMillis();
        this.sharedItem = sharedItem != null ? sharedItem : ItemStack.EMPTY;
        this.itemTags = extractItemTags(this.messageText);
        if (this.sharedItem.isEmpty() && !this.itemTags.isEmpty()) {
            this.sharedItem = this.itemTags.get(0).getStack();
        }
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public ItemStack getSharedItem() {
        return sharedItem;
    }

    public void setSharedItem(ItemStack sharedItem) {
        this.sharedItem = sharedItem != null ? sharedItem : ItemStack.EMPTY;
    }

    public List<ItemTagRef> getItemTags() {
        return Collections.unmodifiableList(itemTags);
    }

    private Component cachedFormattedComponent = null;

    public Component getFormattedComponent() {
        if (cachedFormattedComponent == null) {
            cachedFormattedComponent = buildFormattedComponent();
        }
        return cachedFormattedComponent;
    }

    private Component buildFormattedComponent() {
        if (this.isSystem && this.originalComponent != null) {
            return AquaFontRenderer.withMain(this.originalComponent);
        }

        String rawText = this.messageText != null ? this.messageText : "";
        if (rawText.isBlank() && this.originalComponent != null) {
            String raw = this.originalComponent.getString();
            rawText = stripChatBody(raw, this.senderName, this.rankDisplay);
        }
        if (rawText == null || rawText.isBlank()) {
            return Component.empty();
        }

        String textToParse = rawText;
        if (!this.sharedItem.isEmpty()) {
            String handTag = "[✦ " + this.sharedItem.getHoverName().getString() + "]";
            textToParse = textToParse.replace("[#]", handTag).replace("[i]", handTag);
        }

        if (this.itemTags.isEmpty() && this.sharedItem.isEmpty()) {
            return Component.literal(textToParse).withStyle(Style.EMPTY
                    .withFont(AquaFontRenderer.FONT_MAIN)
                    .withColor(TextColor.fromRgb(0xFFF1F5F9)));
        }

        MutableComponent root = Component.empty();
        int cursor = 0;

        class TagMatch {
            final int start;
            final int end;
            final String label;
            final int color;
            TagMatch(int start, int end, String label, int color) {
                this.start = start;
                this.end = end;
                this.label = label;
                this.color = color;
            }
        }

        List<TagMatch> matches = new ArrayList<>();
        for (ItemTagRef tag : this.itemTags) {
            int idx = 0;
            while ((idx = textToParse.indexOf(tag.getRawTag(), idx)) >= 0) {
                matches.add(new TagMatch(idx, idx + tag.getRawTag().length(),
                        "[✦ " + tag.getDisplayName() + "]", tag.getRarityColor()));
                idx += tag.getRawTag().length();
            }
        }

        if (!this.sharedItem.isEmpty()) {
            String handTag = "[✦ " + this.sharedItem.getHoverName().getString() + "]";
            int idx = 0;
            while ((idx = textToParse.indexOf(handTag, idx)) >= 0) {
                matches.add(new TagMatch(idx, idx + handTag.length(), handTag,
                        getRarityColor(this.sharedItem)));
                idx += handTag.length();
            }
        }

        matches.sort((a, b) -> Integer.compare(a.start, b.start));

        for (TagMatch m : matches) {
            if (m.start < cursor) continue;
            if (m.start > cursor) {
                String seg = textToParse.substring(cursor, m.start);
                root.append(Component.literal(seg).withStyle(Style.EMPTY
                        .withFont(AquaFontRenderer.FONT_MAIN)
                        .withColor(TextColor.fromRgb(0xFFF1F5F9))));
            }
            root.append(Component.literal(m.label).withStyle(Style.EMPTY
                    .withFont(AquaFontRenderer.FONT_MAIN)
                    .withColor(TextColor.fromRgb(m.color))
                    .withBold(true)));
            cursor = m.end;
        }

        if (cursor < textToParse.length()) {
            String seg = textToParse.substring(cursor);
            root.append(Component.literal(seg).withStyle(Style.EMPTY
                    .withFont(AquaFontRenderer.FONT_MAIN)
                    .withColor(TextColor.fromRgb(0xFFF1F5F9))));
        }

        return root;
    }

    public AquaChatMessage withCustomRank(String customRank) {
        if (customRank == null || customRank.isBlank()) return this;
        String r = customRank.trim();
        String rankId = "player";
        String rLow = r.toLowerCase();
        if (rLow.contains("owner") || rLow.contains("владелец") || rLow.contains("создатель")) rankId = "owner";
        else if (rLow.contains("admin") || rLow.contains("админ") || rLow.contains("dev")) rankId = "admin";
        else if (rLow.contains("help") || rLow.contains("хелпер") || rLow.contains("mod") || rLow.contains("модер")) rankId = "mod";
        else if (rLow.contains("vip") || rLow.contains("premium") || rLow.contains("премиум")) rankId = "vip";
        else if (rLow.contains("legend") || rLow.contains("легенда")) rankId = "legend";
        else if (rLow.contains("admiral") || rLow.contains("адмирал")) rankId = "admiral";

        int color = LumenTheme.getRankColor(rankId);
        return new AquaChatMessage(this.senderUuid, this.senderName, rankId, r, color,
                this.channel, this.messageText, this.originalComponent, this.creationTick, this.isSystem);
    }

    public static AquaChatMessage parse(Component component, int currentTick) {
        if (component == null) {
            return null;
        }

        String raw = component.getString().trim();
        if (raw.isBlank()) {
            return null;
        }

        // Remove PUA glyphs from raw string
        String clean = raw.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim();
        // Remove Minecraft formatting codes for regex matching
        String unformatted = clean.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
        if (unformatted.isBlank()) {
            return null;
        }

        AquaChatMessage economy = tryParseEconomy(unformatted, currentTick);
        if (economy != null) {
            return economy;
        }

        // 1. Private message: [sender -> target] msg or [sender -> Я] msg
        Matcher pm = Pattern.compile("^\\[(?<sender>[A-Za-z0-9_]{2,16})\\s*(?:->|→|»|›)\\s*(?<target>[A-Za-z0-9_А-Яа-яЁё]{1,16})\\]\\s*(?<msg>.*)$").matcher(unformatted);
        if (pm.matches()) {
            String sender = resolveSelfAlias(pm.group("sender"));
            String msg = pm.group("msg");
            if (msg == null || msg.isBlank()) return null;
            PlayerProfile profile = resolveProfile(sender);
            UUID uuid = resolveUuid(sender, profile);
            return fromProfile(profile, uuid, sender, msg.trim(), Channel.PRIVATE, component, currentTick);
        }

        Matcher whisperOut = Pattern.compile("^(?:You whisper to|You tell|Вы шепчете(?: игроку)?)\\s+(?<target>[A-Za-z0-9_]{2,16}):\\s*(?<msg>.*)$").matcher(unformatted);
        if (whisperOut.matches()) {
            String sender = localPlayerName();
            String msg = whisperOut.group("msg");
            if (msg == null || msg.isBlank()) return null;
            PlayerProfile profile = resolveProfile(sender);
            UUID uuid = resolveUuid(sender, profile);
            return fromProfile(profile, uuid, sender, msg.trim(), Channel.PRIVATE, component, currentTick);
        }

        Matcher whisperIn = Pattern.compile("^(?<sender>[A-Za-z0-9_]{2,16})\\s+(?:whispers(?: to you)?|шепчет(?: вам)?):\\s*(?<msg>.*)$").matcher(unformatted);
        if (whisperIn.matches()) {
            String sender = whisperIn.group("sender");
            String msg = whisperIn.group("msg");
            if (msg == null || msg.isBlank()) return null;
            PlayerProfile profile = resolveProfile(sender);
            UUID uuid = resolveUuid(sender, profile);
            return fromProfile(profile, uuid, sender, msg.trim(), Channel.PRIVATE, component, currentTick);
        }

        // 2. Vanilla format: <name> message
        Matcher van = Pattern.compile("^<(?<name>[A-Za-z0-9_]{2,16})>\\s*(?<msg>.*)$").matcher(unformatted);
        if (van.matches()) {
            String sender = van.group("name");
            String msg = van.group("msg");
            String body = stripChatBody(unformatted, sender, null);
            if (body.isBlank()) body = msg;
            if (body.isBlank()) return null;
            PlayerProfile profile = resolveProfile(sender);
            UUID uuid = resolveUuid(sender, profile);
            return fromProfile(profile, uuid, sender, body.trim(), Channel.GLOBAL, component, currentTick);
        }

        // 3. Server Chat Formats (supports [ВЛАДЕЛЕЦ]xietoru: msg, ВЛАДЕЛЕЦxietoru: msg, [G] [ВЛАДЕЛЕЦ] xietoru: msg, etc.)
        int sepIdx = -1;
        for (int i = 0; i < unformatted.length(); i++) {
            char c = unformatted.charAt(i);
            if (c == ':' || c == '>') {
                sepIdx = i;
                break;
            }
        }

        if (sepIdx > 0) {
            String header = unformatted.substring(0, sepIdx).trim();
            String msg = unformatted.substring(sepIdx + 1).trim();

            // Check if header contains a valid Latin player name at the end
            Matcher nameMatcher = Pattern.compile("(?<name>[A-Za-z0-9_]{2,16})$").matcher(header);
            if (nameMatcher.find()) {
                String sender = nameMatcher.group("name");
                String prefixPart = header.substring(0, nameMatcher.start()).trim();

                if (!isSystemMessage(sender, msg, prefixPart, unformatted)) {
                    // Determine channel from prefixPart, message prefix or body content
                    Channel ch = Channel.GLOBAL;
                    String pLow = prefixPart.toLowerCase();
                    if (prefixPart.startsWith("[L]") || prefixPart.startsWith("[Л]") || prefixPart.startsWith("L ") || prefixPart.startsWith("Л ")
                            || msg.startsWith("=")) {
                        ch = Channel.LOCAL;
                        prefixPart = prefixPart.replaceFirst("^\\[[LЛlл]\\]\\s*|^[LЛlл]\\s*", "").trim();
                    } else if (prefixPart.startsWith("[T]") || prefixPart.startsWith("[Trade]") || prefixPart.startsWith("[Т]") || prefixPart.startsWith("Trade ")
                            || pLow.contains("торг") || pLow.contains("рынок") || msg.startsWith("$") || msg.startsWith("[Trade]") || msg.startsWith("[Торговля]")) {
                        ch = Channel.TRADE;
                        prefixPart = prefixPart.replaceFirst("^\\[(?i:Trade|Торговля|Рынок|T|Т)\\]\\s*|^(?i:Trade|Торговля|Рынок|T|Т)\\s*", "").trim();
                    } else if (prefixPart.startsWith("[G]") || prefixPart.startsWith("[Г]") || prefixPart.startsWith("G ") || prefixPart.startsWith("Г ")
                            || msg.startsWith("!")) {
                        ch = Channel.GLOBAL;
                        prefixPart = prefixPart.replaceFirst("^\\[[GГgг]\\]\\s*|^[GГgг]\\s*", "").trim();
                    }

                    // Extract rank from prefixPart
                    String customRank = null;
                    if (!prefixPart.isEmpty()) {
                        String cleanedPrefix = prefixPart.replaceAll("[\\[\\]()§]", " ").trim();
                        String[] words = cleanedPrefix.split("\\s+");
                        if (words.length > 0 && !words[0].isBlank()) {
                            customRank = words[0].trim();
                        }
                    }

                    PlayerProfile profile = resolveProfile(sender);
                    UUID uuid = resolveUuid(sender, profile);
                    String body = stripChatBody(unformatted, sender, customRank);
                    if (body.isBlank()) {
                        body = msg;
                    }
                    if (body.isBlank()) {
                        body = clean;
                    }
                    if (body.isBlank()) {
                        return null; // Never emit an empty message
                    }
                    AquaChatMessage res = fromProfile(profile, uuid, sender, body.trim(), ch, component, currentTick);
                    if (customRank != null && !customRank.isBlank()) {
                        res = res.withCustomRank(customRank);
                    }
                    return res;
                }
            }
        }

        // 4. System / Server announcement
        return new AquaChatMessage(null, null, "system", "СИСТЕМА",
                0xFF00B0FF, Channel.SYSTEM, clean, component, currentTick, true);
    }

    private static final Pattern PAY_RECEIVED = Pattern.compile(
            "(?iu)^(?<amt>[\\d\\s.,]+)\\s*¤\\s*(?:has been received from|получено от)\\s+(?<who>.+?)\\.?\\s*$");
    private static final Pattern PAY_SENT = Pattern.compile(
            "(?iu)^(?<amt>[\\d\\s.,]+)\\s*¤\\s*(?:has been sent to|отправлено игроку)\\s+(?<who>.+?)\\.?\\s*$");
    private static final Pattern PAY_BARE_RECEIVED = Pattern.compile(
            "(?iu)^(?<amt>[\\d\\s.,]+)\\s+has been received from\\s+(?<who>.+?)\\.?\\s*$");
    private static final Pattern PAY_BARE_SENT = Pattern.compile(
            "(?iu)^(?<amt>[\\d\\s.,]+)\\s+has been sent to\\s+(?<who>.+?)\\.?\\s*$");
    private static final Pattern ACCOUNT_ADDED = Pattern.compile(
            "(?iu)^(?<amt>[\\d\\s.,]+)\\s*¤?\\s*has been added to your account\\.?\\s*$");
    private static final Pattern ACCOUNT_TAKEN = Pattern.compile(
            "(?iu)^(?<amt>[\\d\\s.,]+)\\s*¤?\\s*has been taken from your account\\.?\\s*$");

    private static AquaChatMessage tryParseEconomy(String unformatted, int currentTick) {
        if (unformatted == null || unformatted.isBlank()) {
            return null;
        }
        Matcher received = PAY_RECEIVED.matcher(unformatted);
        if (!received.matches()) {
            received = PAY_BARE_RECEIVED.matcher(unformatted);
        }
        if (received.matches()) {
            String amt = formatPayAmount(received.group("amt"));
            String nick = extractPayNick(received.group("who"));
            String body = "Получено " + amt + " ¤ от " + nick + ".";
            return new AquaChatMessage(null, null, "system", "СИСТЕМА",
                    0xFF34C759, Channel.SYSTEM, body, null, currentTick, true);
        }
        Matcher sent = PAY_SENT.matcher(unformatted);
        if (!sent.matches()) {
            sent = PAY_BARE_SENT.matcher(unformatted);
        }
        if (sent.matches()) {
            String amt = formatPayAmount(sent.group("amt"));
            String nick = extractPayNick(sent.group("who"));
            String body = "Отправлено " + amt + " ¤ игроку " + nick + ".";
            return new AquaChatMessage(null, null, "system", "СИСТЕМА",
                    0xFF34C759, Channel.SYSTEM, body, null, currentTick, true);
        }
        Matcher added = ACCOUNT_ADDED.matcher(unformatted);
        if (added.matches()) {
            String amt = formatPayAmount(added.group("amt"));
            return new AquaChatMessage(null, null, "system", "СИСТЕМА",
                    0xFF34C759, Channel.SYSTEM, "Баланс + " + amt + " ¤.", null, currentTick, true);
        }
        Matcher taken = ACCOUNT_TAKEN.matcher(unformatted);
        if (taken.matches()) {
            String amt = formatPayAmount(taken.group("amt"));
            return new AquaChatMessage(null, null, "system", "СИСТЕМА",
                    0xFFFF9F43, Channel.SYSTEM, "Баланс − " + amt + " ¤.", null, currentTick, true);
        }
        return null;
    }

    private static String extractPayNick(String who) {
        if (who == null) {
            return "игрок";
        }
        String t = who.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "")
                .replaceAll("[\\[\\]]", " ")
                .trim();
        Matcher latin = Pattern.compile("([A-Za-z0-9_]{2,16})$").matcher(t);
        if (latin.find()) {
            return latin.group(1);
        }
        return t.replaceAll("\\s+", " ");
    }

    private static String formatPayAmount(String raw) {
        if (raw == null) {
            return "0";
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return raw.trim();
        }
        try {
            return NumberFormat.getIntegerInstance(new Locale("ru", "RU")).format(Long.parseLong(digits));
        } catch (NumberFormatException e) {
            return raw.trim();
        }
    }

    public static String stripChatBody(String raw, String senderName, String rankDisplay) {
        String t = raw == null ? "" : raw;
        t = t.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "");
        t = t.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();
        if (senderName != null && !senderName.isBlank()) {
            String tagged = senderName + ":";
            int colon = t.lastIndexOf(tagged);
            if (colon >= 0) {
                t = t.substring(colon + tagged.length()).trim();
            } else {
                String van = "<" + senderName + ">";
                int v = t.indexOf(van);
                if (v >= 0) {
                    t = t.substring(v + van.length()).trim();
                }
            }
        }
        // Only safely strip channel tags like [G], [L], [Trade], [T], [Рынок], [Торговля]
        t = t.replaceFirst("^(?:\\[(?:G|L|T|Trade|Г|Л|Т|Рынок|Торговля)\\]\\s*|(?:G|L|T|Trade|Г|Л|Т|Рынок|Торговля)\\s+)", "").trim();
        if (rankDisplay != null && !rankDisplay.isBlank()) {
            Pattern rankPat = Pattern.compile(
                    "(?iu)^(?:\\[" + Pattern.quote(rankDisplay) + "\\]|" + Pattern.quote(rankDisplay) + ")\\s*");
            for (int n = 0; n < 4; n++) {
                String next = rankPat.matcher(t).replaceFirst("").trim();
                if (next.equals(t)) {
                    break;
                }
                t = next;
            }
        }
        Pattern bracketRank = Pattern.compile(
                "(?iu)^\\[(?:владелец|админ|игрок|модератор|хелпер|owner|admin)\\]\\s*");
        for (int n = 0; n < 4; n++) {
            String next = bracketRank.matcher(t).replaceFirst("").trim();
            if (next.equals(t)) {
                break;
            }
            t = next;
        }
        Pattern bareRank = Pattern.compile(
                "(?iu)^(?:владелец|админ|игрок|модератор|хелпер|owner|admin)\\s+");
        for (int n = 0; n < 4; n++) {
            String next = bareRank.matcher(t).replaceFirst("").trim();
            if (next.equals(t)) {
                break;
            }
            t = next;
        }
        if (senderName != null && !senderName.isBlank()) {
            t = t.replaceFirst("(?i)^" + Pattern.quote(senderName) + "\\s*:\\s*", "").trim();
        }
        if (t.startsWith("!") || t.startsWith("$") || t.startsWith("=")) {
            t = t.substring(1).trim();
        }
        return t;
    }

    public static List<ItemTagRef> extractItemTags(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        List<ItemTagRef> list = new ArrayList<>();
        // Matches #mod:item or #item (supports slashes in path like #industrialupgrade:machines/solar and Cyrillic)
        Matcher hashMatcher = Pattern.compile("#([a-zA-Z0-9_./А-Яа-яЁё-]+(?::[a-zA-Z0-9_./А-Яа-яЁё-]+)?)").matcher(text);
        while (hashMatcher.find()) {
            String tag = hashMatcher.group(0);
            String idStr = hashMatcher.group(1);
            ItemStack stack = resolveItem(idStr);
            if (!stack.isEmpty()) {
                list.add(new ItemTagRef(tag, stack.getHoverName().getString(), stack, getRarityColor(stack)));
            }
        }
        return list;
    }

    public static ItemStack resolveItem(String query) {
        if (query == null || query.isBlank()) return ItemStack.EMPTY;
        String q = query.trim().toLowerCase();

        // Common Aliases
        if (q.equals("workbench") || q.equals("верстак")) q = "crafting_table";
        else if (q.equals("печь") || q.equals("печка")) q = "furnace";
        else if (q.equals("сундук")) q = "chest";
        else if (q.equals("алмаз")) q = "diamond";
        else if (q.equals("железо")) q = "iron_ingot";
        else if (q.equals("золото")) q = "gold_ingot";
        else if (q.equals("уголь")) q = "coal";
        else if (q.equals("палка")) q = "stick";
        else if (q.equals("дерево") || q.equals("дуб")) q = "oak_log";
        else if (q.equals("камень")) q = "stone";

        try {
            if (q.contains(":")) {
                ResourceLocation rl = ResourceLocation.tryParse(q);
                if (rl != null && BuiltInRegistries.ITEM.containsKey(rl)) {
                    return new ItemStack(BuiltInRegistries.ITEM.get(rl));
                }
            }
            ResourceLocation mcRl = new ResourceLocation("minecraft", q);
            if (BuiltInRegistries.ITEM.containsKey(mcRl)) {
                return new ItemStack(BuiltInRegistries.ITEM.get(mcRl));
            }
            String qNoUnderscore = q.replace('_', ' ');
            for (Item item : BuiltInRegistries.ITEM) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                if (key.getPath().equalsIgnoreCase(q)) {
                    return new ItemStack(item);
                }
                ItemStack testStack = new ItemStack(item);
                String hName = testStack.getHoverName().getString().toLowerCase();
                if (hName.equalsIgnoreCase(query.trim()) || hName.equalsIgnoreCase(q) || hName.equalsIgnoreCase(qNoUnderscore)) {
                    return testStack;
                }
            }
            // Fuzzy search by path or name
            for (Item item : BuiltInRegistries.ITEM) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                String path = key.getPath().toLowerCase();
                ItemStack testStack = new ItemStack(item);
                String hName = testStack.getHoverName().getString().toLowerCase();
                if (path.contains(q) || hName.contains(q) || (!qNoUnderscore.isEmpty() && hName.contains(qNoUnderscore))) {
                    return testStack;
                }
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    public static int getRarityColor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0xFF5CE1E6;
        Rarity rarity = stack.getRarity();
        if (rarity == Rarity.EPIC) return 0xFFC084FC;
        if (rarity == Rarity.RARE) return 0xFF38BDF8;
        if (rarity == Rarity.UNCOMMON) return 0xFFFBBF24;
        return 0xFF5CE1E6;
    }

    public static boolean isKnownPlayer(String name) {
        if (name == null || name.isBlank()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && name.equalsIgnoreCase(mc.player.getName().getString())) {
            return true;
        }
        if (mc.getConnection() != null) {
            if (mc.getConnection().getPlayerInfo(name) != null) return true;
            for (var p : mc.getConnection().getOnlinePlayers()) {
                if (p.getProfile() != null && name.equalsIgnoreCase(p.getProfile().getName())) {
                    return true;
                }
            }
        }
        return ClientUiState.profileByName(name) != null;
    }

    public static boolean isSystemMessage(String sender, String msg, String prefixPart, String unformatted) {
        if (sender == null || sender.isBlank()) return true;
        String sLow = sender.trim().toLowerCase();

        // 1. Blacklisted known plugin/system names
        if (SYSTEM_SENDER_NAMES.contains(sLow)) {
            return true;
        }

        // 2. Suffixes indicating system/plugin modules
        if ((sLow.endsWith("api") || sLow.endsWith("plugin") || sLow.endsWith("lib") ||
             sLow.endsWith("mod") || sLow.endsWith("core") || sLow.endsWith("engine") ||
             sLow.endsWith("service") || sLow.endsWith("bridge") || sLow.endsWith("bot"))
                && !isKnownPlayer(sender)) {
            return true;
        }

        // 3. Command feedback or help formats
        if (msg != null) {
            String mLow = msg.trim().toLowerCase();
            if (mLow.startsWith("plugin help:") || mLow.startsWith("usage:") ||
                mLow.startsWith("использование:") || mLow.startsWith("to see more help:") ||
                mLow.startsWith("type /") || mLow.startsWith("aliases:") ||
                mLow.startsWith("description:") || mLow.startsWith("permission:") ||
                mLow.startsWith("permissions:") || mLow.startsWith("version:") ||
                mLow.startsWith("commands:") || mLow.startsWith("syntax:") ||
                mLow.startsWith("unknown command") || mLow.startsWith("неизвестная команда") ||
                mLow.startsWith("/") || mLow.contains("используйте /") || mLow.contains("type /help")) {
                return true;
            }
        }

        // 4. Header or banner decorations (e.g. ---- Help ---- or === Stats ===)
        if (unformatted != null) {
            String uTrim = unformatted.trim();
            if (uTrim.startsWith("---") || uTrim.startsWith("===") || uTrim.startsWith("***") || uTrim.startsWith("/")) {
                return true;
            }
        }

        // 5. If there is NO channel prefix ([G], [L], [Trade], etc.) and NO bracketed rank,
        // and the sender is not an active online player in the server connection:
        boolean hasChannelTag = prefixPart.contains("[G]") || prefixPart.contains("[L]") ||
                                prefixPart.contains("[T]") || prefixPart.contains("[Trade]") ||
                                prefixPart.contains("[Г]") || prefixPart.contains("[Л]") ||
                                prefixPart.contains("[Т]") || prefixPart.startsWith("G ") ||
                                prefixPart.startsWith("L ") || prefixPart.startsWith("Trade ");
        boolean hasRankTag = prefixPart.contains("[") && prefixPart.contains("]");

        if (!hasChannelTag && !hasRankTag && !isKnownPlayer(sender)) {
            return true;
        }

        return false;
    }

    private static String localPlayerName() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return mc.player.getName().getString();
        }
        return "me";
    }

    private static String resolveSelfAlias(String sender) {
        if (sender == null) return localPlayerName();
        String s = sender.trim();
        if (s.equalsIgnoreCase("me") || s.equalsIgnoreCase("you") || s.equals("я") || s.equals("Вы") || s.equals("вы")) {
            return localPlayerName();
        }
        return s;
    }

    private static PlayerProfile resolveProfile(String name) {
        if (name == null || name.isBlank()) return null;
        PlayerProfile profile = ClientUiState.profileByName(name);
        if (profile == null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && name.equalsIgnoreCase(mc.player.getName().getString())) {
                return ClientUiState.profile(mc.player.getUUID());
            }
        }
        return profile;
    }

    private static UUID resolveUuid(String name, PlayerProfile profile) {
        if (profile != null && profile.uuid() != null) {
            return profile.uuid();
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && name.equalsIgnoreCase(mc.player.getName().getString())) {
            return mc.player.getUUID();
        }
        if (mc.getConnection() != null) {
            var info = mc.getConnection().getPlayerInfo(name);
            if (info != null && info.getProfile() != null) {
                return info.getProfile().getId();
            }
            for (var p : mc.getConnection().getOnlinePlayers()) {
                if (p.getProfile() != null && name.equalsIgnoreCase(p.getProfile().getName())) {
                    return p.getProfile().getId();
                }
            }
        }
        return null;
    }

    private static AquaChatMessage fromProfile(PlayerProfile profile, UUID uuid, String senderName, String messageText,
                                                Channel channel, Component originalComponent, int currentTick) {
        if (uuid == null && profile != null) {
            uuid = profile.uuid();
        }
        if (uuid == null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && senderName.equalsIgnoreCase(mc.player.getName().getString())) {
                uuid = mc.player.getUUID();
            }
        }

        String rankId = "player";
        String rankDisplay = "Игрок";

        if (profile != null) {
            rankId = profile.rankId();
            rankDisplay = profile.rankDisplay();
        } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && senderName.equalsIgnoreCase(mc.player.getName().getString())) {
                rankId = ClientUiState.sessionRankId();
                if (mc.player.hasPermissions(4)) rankId = "owner";
                else if (mc.player.hasPermissions(3)) rankId = "admin";
            }
        }

        String resolvedTitle = LumenTheme.getRankTitle(rankId);
        if (rankDisplay != null && !rankDisplay.isBlank()) {
        String custom = rankDisplay.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim();
            if (!custom.isBlank() && !custom.equalsIgnoreCase(rankId)) {
                resolvedTitle = custom;
            }
        }
        rankDisplay = resolvedTitle;

        int rankColor = LumenTheme.getRankColor(rankId);
        ItemStack sharedItem = extractSharedItem(originalComponent);

        return new AquaChatMessage(uuid, senderName, rankId, rankDisplay, rankColor,
                channel, messageText, originalComponent, currentTick, false, sharedItem);
    }

    public static ItemStack extractSharedItem(Component component) {
        if (component == null) return ItemStack.EMPTY;
        for (Component sibling : component.toFlatList()) {
            if (sibling.getStyle() != null && sibling.getStyle().getHoverEvent() != null) {
                HoverEvent hover = sibling.getStyle().getHoverEvent();
                if (hover.getAction() == HoverEvent.Action.SHOW_ITEM) {
                    HoverEvent.ItemStackInfo info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
                    if (info != null) {
                        return info.getItemStack();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRankId() {
        return rankId;
    }

    public String getRankDisplay() {
        return rankDisplay;
    }

    public int getRankColor() {
        return rankColor;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getMessageText() {
        return messageText;
    }

    public boolean isMentionSoundPlayed() {
        return mentionSoundPlayed;
    }

    public void markMentionSoundPlayed() {
        this.mentionSoundPlayed = true;
    }

    public Component getOriginalComponent() {
        return originalComponent;
    }

    public String getTimeFormatted() {
        return timeFormatted;
    }

    public int getCreationTick() {
        return creationTick;
    }

    public boolean isSystem() {
        return isSystem;
    }
}
