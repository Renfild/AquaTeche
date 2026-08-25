package net.aquatech.ui.client.theme;

/**
 * Clean, modern color tokens for the AquaLumen design system.
 */
public record LumenTheme(
        int canvas,
        int surface,
        int surfaceElevated,
        int raised,
        int border,
        int borderMuted,
        int text,
        int textDim,
        int accent,
        int accentAlt,
        int gold,
        int danger,
        int success
) {

    public static final LumenTheme AQUA = new LumenTheme(
            0xFF070C12, // deep dark navy canvas
            0xFF0E151E, // card surface
            0xFF121B27, // elevated surface
            0xFF16202C, // raised hover / row background
            0x26FFFFFF, // crisp 15% white glass border
            0x14FFFFFF, // subtle 8% white separator border
            0xFFF2F7FA, // pure bright text
            0xFF9DB2C4, // muted slate text
            0xFF2FE0C0, // electric turquoise / aqua accent
            0xFF3B9DFF, // vibrant azure blue
            0xFFF5C25B, // warm gold
            0xFFFF6B6B, // danger / high latency coral red
            0xFF4CD08A  // online mint green
    );

    private static final LumenTheme INSTANCE = AQUA;

    public static LumenTheme get() {
        return INSTANCE;
    }

    public int panelAlpha(float opacity) {
        int a = (int) (Math.min(1.0F, Math.max(0.0F, opacity)) * 255.0F);
        return (a << 24) | (surface & 0x00FFFFFF);
    }

    public int accentAlpha(float opacity) {
        int a = (int) (Math.min(1.0F, Math.max(0.0F, opacity)) * 255.0F);
        return (a << 24) | (accent & 0x00FFFFFF);
    }

    public static int getRankColor(String rankId) {
        if (rankId == null) return 0xFF81ECEC;
        String id = rankId.toLowerCase(java.util.Locale.ROOT).trim();
        return switch (id) {
            case "owner", "создатель", "владелец" -> 0xFFF5C25B; // Gold
            case "admin", "администратор", "dev", "developer", "разработчик", "staff", "персонал" -> 0xFFFF6B6B; // Coral Red
            case "mod", "moderator", "модератор" -> 0xFFFF9F43; // Orange
            case "helper", "хелпер" -> 0xFF4CD08A; // Emerald Green
            case "manager", "менеджер", "куратор", "legend", "легенда" -> 0xFFE056FD; // Neon Purple
            case "deluxe", "делюкс" -> 0xFF2FE0C0; // Electric Aqua
            case "ultimate", "ультимейт", "skipper", "шкипер" -> 0xFF00E5FF; // Bright Cyan
            case "sailor", "моряк" -> 0xFF2FE0C0; // Ocean Teal
            case "vip", "вип", "vipplus", "vip+", "premium", "премиум" -> 0xFFFEEAA7; // Pale Gold / Yellow
            case "admiral", "адмирал", "streamer", "стример", "twitch" -> 0xFFA29BFE; // Lavender
            case "captain", "капитан" -> 0xFF74B9FF; // Sky Blue
            case "youtuber", "ютубер", "youtube" -> 0xFFFF4757; // YouTube Red
            case "aquanaut", "акванавт" -> 0xFF2FE0C0; // Aquanaut Mint
            case "hydrotech", "гидротех" -> 0xFFE07A5F; // Hydrotech Copper
            case "engineer", "инженер" -> 0xFF3B9DFF; // Engineer Sapphire
            case "cyberhydro", "кибер-гидро" -> 0xFF9B7BFF; // Cyber Hydro Purple
            case "artist", "артист" -> 0xFFFD79A8; // Pink
            case "builder", "билдер", "строитель" -> 0xFFFAB1A0; // Peach
            case "friend", "друг" -> 0xFF55EFC4; // Mint
            case "trainee", "стажер", "стажёр" -> 0xFFFFEAA7; // Soft Yellow
            case "npc" -> 0xFFA0AEC0; // Slate Grey
            default -> 0xFF81ECEC; // Default Player Soft Mint
        };
    }

    public static String getRankTitle(String rankId) {
        if (rankId == null || rankId.isBlank()) return "ИГРОК";
        String id = rankId.toLowerCase(java.util.Locale.ROOT).trim();
        return switch (id) {
            case "owner", "создатель", "владелец" -> "ВЛАДЕЛЕЦ";
            case "admin", "администратор" -> "АДМИН";
            case "dev", "developer", "разработчик" -> "DEV";
            case "mod", "moderator", "модератор" -> "МОДЕР";
            case "staff", "персонал" -> "ПЕРСОНАЛ";
            case "helper", "хелпер" -> "ХЕЛПЕР";
            case "manager", "менеджер", "куратор" -> "КУРАТОР";
            case "aquanaut", "акванавт" -> "АКВАНАВТ";
            case "hydrotech", "гидротех" -> "ГИДРОТЕХ";
            case "engineer", "инженер" -> "ИНЖЕНЕР";
            case "cyberhydro", "кибер-гидро" -> "КИБЕР-ГИДРО";
            case "legend", "легенда" -> "ЛЕГЕНДА";
            case "admiral", "адмирал" -> "АДМИРАЛ";
            case "captain", "капитан" -> "КАПИТАН";
            case "skipper", "шкипер" -> "ШКИПЕР";
            case "sailor", "моряк" -> "МОРЯК";
            case "vipplus", "vip+" -> "VIP+";
            case "vip", "вип", "premium", "премиум" -> "VIP";
            case "deluxe", "делюкс" -> "DELUXE";
            case "ultimate", "ультимейт" -> "ULTIMATE";
            case "streamer", "стример" -> "СТРИМЕР";
            case "twitch" -> "TWITCH";
            case "youtuber", "ютубер", "youtube" -> "YOUTUBE";
            case "artist", "артист" -> "АРТИСТ";
            case "builder", "билдер", "строитель" -> "БИЛДЕР";
            case "friend", "друг" -> "ДРУГ";
            case "trainee", "стажер", "стажёр" -> "СТАЖЕР";
            case "npc" -> "NPC";
            default -> "ИГРОК";
        };
    }
}

