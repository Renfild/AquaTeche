package net.aquatech.ui.client.chat;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.client.theme.LumenTheme;
import net.aquatech.ui.common.PlayerProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AquaChatMessage {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public enum Channel {
        ALL("Все", "ALL", 0xFF2FE0C0),       // Electric Turquoise
        GLOBAL("Глобал", "G", 0xFFFF4081),   // Vivid Pink / Magenta
        LOCAL("Локал", "L", 0xFF38BDF8),     // Sky Blue
        TRADE("Торговля", "T", 0xFF4CD08A),  // Mint Green
        PRIVATE("ЛС", "PM", 0xFFF5C25B),     // Sunset Gold
        SYSTEM("Система", "SYS", 0xFF00B0FF); // Azure Blue

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
    // Mutable per-instance flag: play mention sound only once
    private boolean mentionSoundPlayed = false;

    public AquaChatMessage(UUID senderUuid, String senderName, String rankId, String rankDisplay,
                           int rankColor, Channel channel, String messageText, Component originalComponent,
                           int creationTick, boolean isSystem) {
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
    }

    public AquaChatMessage withCustomRank(String customRank) {
        if (customRank == null || customRank.isBlank()) return this;
        String r = customRank.trim().toUpperCase();
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
            return new AquaChatMessage(null, null, "player", "ИГРОК", 0xFF81ECEC,
                    Channel.SYSTEM, "", Component.empty(), currentTick, true);
        }

        String raw = component.getString().trim();
        // Remove PUA glyphs from raw string
        String clean = raw.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim();
        // Remove Minecraft formatting codes for regex matching
        String unformatted = clean.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();

        // 1. Private message: [sender -> target] msg or [sender -> Я] msg
        Matcher pm = Pattern.compile("^\\[(?<sender>[A-Za-z0-9_]{2,16})\\s*(?:->|→|»|›)\\s*(?<target>[A-Za-z0-9_А-Яа-яЁё]{1,16})\\]\\s*(?<msg>.*)$").matcher(unformatted);
        if (pm.matches()) {
            String sender = resolveSelfAlias(pm.group("sender"));
            String msg = pm.group("msg");
            PlayerProfile profile = resolveProfile(sender);
            UUID uuid = resolveUuid(sender, profile);
            return fromProfile(profile, uuid, sender, msg, Channel.PRIVATE, component, currentTick);
        }

        Matcher whisperOut = Pattern.compile("^(?:You whisper to|You tell|Вы шепчете(?: игроку)?)\\s+(?<target>[A-Za-z0-9_]{2,16}):\\s*(?<msg>.*)$").matcher(unformatted);
        if (whisperOut.matches()) {
            String sender = localPlayerName();
            String msg = whisperOut.group("msg");
            PlayerProfile profile = resolveProfile(sender);
            UUID uuid = resolveUuid(sender, profile);
            return fromProfile(profile, uuid, sender, msg, Channel.PRIVATE, component, currentTick);
        }

        Matcher whisperIn = Pattern.compile("^(?<sender>[A-Za-z0-9_]{2,16})\\s+(?:whispers(?: to you)?|шепчет(?: вам)?):\\s*(?<msg>.*)$").matcher(unformatted);
        if (whisperIn.matches()) {
            String sender = whisperIn.group("sender");
            String msg = whisperIn.group("msg");
            PlayerProfile profile = resolveProfile(sender);
            UUID uuid = resolveUuid(sender, profile);
            return fromProfile(profile, uuid, sender, msg, Channel.PRIVATE, component, currentTick);
        }

        // 2. Vanilla format: <name> message
        Matcher van = Pattern.compile("^<(?<name>[A-Za-z0-9_]{2,16})>\\s*(?<msg>.*)$").matcher(unformatted);
        if (van.matches()) {
            String sender = van.group("name");
            String msg = van.group("msg");
            PlayerProfile profile = resolveProfile(sender);
            UUID uuid = resolveUuid(sender, profile);
            return fromProfile(profile, uuid, sender, stripChatBody(unformatted, sender, null), Channel.GLOBAL, component, currentTick);
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

                // Determine channel from prefixPart
                Channel ch = Channel.GLOBAL;
                if (prefixPart.startsWith("[L]") || prefixPart.startsWith("[Л]") || prefixPart.startsWith("L ") || prefixPart.startsWith("Л ")) {
                    ch = Channel.LOCAL;
                    prefixPart = prefixPart.replaceFirst("^\\[[LЛlл]\\]\\s*|^[LЛlл]\\s*", "").trim();
                } else if (prefixPart.startsWith("[G]") || prefixPart.startsWith("[Г]") || prefixPart.startsWith("G ") || prefixPart.startsWith("Г ")) {
                    ch = Channel.GLOBAL;
                    prefixPart = prefixPart.replaceFirst("^\\[[GГgг]\\]\\s*|^[GГgг]\\s*", "").trim();
                } else if (prefixPart.startsWith("[T]") || prefixPart.startsWith("[Trade]") || prefixPart.startsWith("[Т]") || prefixPart.startsWith("Trade ")) {
                    ch = Channel.TRADE;
                    prefixPart = prefixPart.replaceFirst("^\\[(Trade|T|Т)\\]\\s*|^(Trade|T|Т)\\s*", "").trim();
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
                AquaChatMessage res = fromProfile(profile, uuid, sender, body, ch, component, currentTick);
                if (customRank != null && !customRank.isBlank()) {
                    res = res.withCustomRank(customRank);
                }
                return res;
            }
        }

        // 4. System / Server announcement
        return new AquaChatMessage(null, null, "system", "СИСТЕМА",
                0xFF00B0FF, Channel.SYSTEM, clean, component, currentTick, true);
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
        t = t.replaceAll("^(\\[[^\\]]{1,32}\\]\\s*)+", "").trim();
        if (rankDisplay != null && !rankDisplay.isBlank()) {
            t = t.replaceFirst("(?iu)^(" + Pattern.quote(rankDisplay) + "\\s*)+", "").trim();
        }
        t = t.replaceFirst("(?iu)^(владелец|админ|игрок|модератор|хелпер|owner|admin)\\s+", "").trim();
        if (senderName != null && !senderName.isBlank()) {
            t = t.replaceFirst("(?i)^" + Pattern.quote(senderName) + "\\s*:\\s*", "").trim();
        }
        return t;
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
        String rankDisplay = "ИГРОК";

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
            String custom = rankDisplay.replaceAll("[\\uE000-\\uF8FF\\uD800-\\uDFFF]", "").trim().toUpperCase();
            if (!custom.isBlank() && !custom.equalsIgnoreCase(rankId)) {
                resolvedTitle = custom;
            }
        }
        rankDisplay = resolvedTitle;

        int rankColor = LumenTheme.getRankColor(rankId);

        return new AquaChatMessage(uuid, senderName, rankId, rankDisplay, rankColor,
                channel, messageText, originalComponent, currentTick, false);
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
