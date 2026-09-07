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

    /** PUA char whose ranks-font glyph is this rank's wordmark art. */
    public static String getRankGlyph(String rankId) {
        if (rankId == null) return "";
        String id = rankId.toLowerCase(java.util.Locale.ROOT).trim();
        return switch (id) {
            case "owner", "создатель", "владелец" -> "";
            case "admin", "администратор" -> "";
            case "dev", "developer", "разработчик" -> "";
            case "mod", "moderator", "модератор" -> "";
            case "staff", "персонал" -> "";
            case "helper", "хелпер" -> "";
            case "manager", "менеджер", "куратор" -> "";
            case "magnate", "магнат" -> "";
            case "mvp" -> "";
            case "vipplus", "vip+" -> "";
            case "vip", "вип" -> "";
            case "streamer", "стример" -> "";
            case "twitch" -> "";
            case "youtuber", "youtube", "ютубер" -> "";
            case "artist", "артист" -> "";
            case "builder", "билдер", "строитель" -> "";
            case "friend", "друг" -> "";
            case "trainee", "стажер", "стажёр" -> "";
            case "player", "игрок" -> "";
            case "npc", "нпс" -> "";
            default -> "";
        };
    }

    public static int getRankColor(String rankId) {
        if (rankId == null) return 0xFF81ECEC;
        String id = rankId.toLowerCase(java.util.Locale.ROOT).trim();
        return switch (id) {
            case "owner", "создатель", "владелец" -> 0xFFF5C25B; // Gold
            case "admin", "администратор", "dev", "developer", "разработчик", "staff", "персонал" -> 0xFFFF6B6B; // Coral Red
            case "mod", "moderator", "модератор" -> 0xFFFF9F43; // Orange
            case "helper", "хелпер" -> 0xFF4CD08A; // Emerald Green
            case "manager", "менеджер", "куратор" -> 0xFFE056FD; // Neon Purple
            case "legend", "легенда" -> 0xFFC264FF; // Rank Violet (store palette)
            case "deluxe", "делюкс" -> 0xFF2FE0C0; // Electric Aqua
            case "ultimate", "ультимейт" -> 0xFF00E5FF; // Bright Cyan
            case "skipper", "шкипер" -> 0xFF3B9DFF; // Rank Blue (store palette)
            case "sailor", "моряк" -> 0xFF2FE0C0; // Ocean Teal
            case "vip", "вип", "vipplus", "vip+" -> 0xFFFF6B6B; // Rank Red (store palette)
            case "premium", "премиум" -> 0xFFFEEAA7; // Pale Gold / Yellow
            case "admiral", "адмирал" -> 0xFFFF8C42; // Rank Orange (store palette)
            case "streamer", "стример", "twitch" -> 0xFFA29BFE; // Lavender
            case "captain", "капитан" -> 0xFFF5C25B; // Rank Gold (store palette)
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
        if (rankId == null || rankId.isBlank()) return "Игрок";
        String id = rankId.toLowerCase(java.util.Locale.ROOT).trim();
        return switch (id) {
            case "owner", "создатель", "владелец" -> "Владелец";
            case "admin", "администратор" -> "Админ";
            case "dev", "developer", "разработчик" -> "Разраб";
            case "mod", "moderator", "модератор" -> "Модер";
            case "staff", "персонал" -> "Персонал";
            case "helper", "хелпер" -> "Хелпер";
            case "manager", "менеджер", "куратор" -> "Куратор";
            case "aquanaut", "акванавт" -> "Акванавт";
            case "hydrotech", "гидротех" -> "Гидротех";
            case "engineer", "инженер" -> "Инженер";
            case "cyberhydro", "кибер-гидро" -> "Кибер-гидро";
            case "legend", "легенда" -> "Легенда";
            case "admiral", "адмирал" -> "Адмирал";
            case "captain", "капитан" -> "Капитан";
            case "skipper", "шкипер" -> "Шкипер";
            case "sailor", "моряк" -> "Моряк";
            case "vipplus", "vip+" -> "VIP+";
            case "vip", "вип", "premium", "премиум" -> "VIP";
            case "deluxe", "делюкс" -> "Deluxe";
            case "ultimate", "ультимейт" -> "Ultimate";
            case "streamer", "стример" -> "Стример";
            case "twitch" -> "Twitch";
            case "youtuber", "ютубер", "youtube" -> "YouTube";
            case "artist", "артист" -> "Артист";
            case "builder", "билдер", "строитель" -> "Билдер";
            case "friend", "друг" -> "Друг";
            case "trainee", "стажер", "стажёр" -> "Стажёр";
            case "npc" -> "NPC";
            default -> "Игрок";
        };
    }
}

