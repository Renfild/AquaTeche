package store.aquateche.aqualumen.client.theme;

import store.aquateche.aqualumen.config.LumenConfig;

/**
 * Colour system of the hub. Layout, spacing and behaviour stay identical between presets:
 * only the palette changes, so a re-skin never breaks the UX.
 */
public record LumenTheme(String id,
                         int canvas,
                         int surface,
                         int raised,
                         int border,
                         int text,
                         int textDim,
                         int accent,
                         int accentAlt,
                         int gold,
                         int danger,
                         int success) {

    /** Default preset: deep ocean navy with aqua / azure light. */
    public static final LumenTheme AQUA_LUMEN = new LumenTheme("aqua_lumen",
            0xFF070C12, 0xFF0E151E, 0xFF16202C, 0x24FFFFFF,
            0xFFF2F7FA, 0xFF9DB2C4,
            0xFF2FE0C0, 0xFF3B9DFF, 0xFFF5C25B, 0xFFFF6B6B, 0xFF4CD08A);

    /** Reference preset: the classic violet / magenta luminous look. */
    public static final LumenTheme VIOLET_LUMEN = new LumenTheme("violet_lumen",
            0xFF0B0713, 0xFF140E20, 0xFF1D1530, 0x24FFFFFF,
            0xFFF6F2FB, 0xFFB0A3C6,
            0xFFB072FF, 0xFFFF6BC1, 0xFFFFC978, 0xFFFF6070, 0xFF63D69B);

    /** Warm alternative for events and seasonal skins. */
    public static final LumenTheme MIDNIGHT_ROSE = new LumenTheme("midnight_rose",
            0xFF120A10, 0xFF1B0F18, 0xFF261722, 0x24FFFFFF,
            0xFFFDF3F7, 0xFFC7A9B8,
            0xFFFF7A9C, 0xFFFFB27A, 0xFFFFD98A, 0xFFFF5F6D, 0xFF7ED6A6);

    public static LumenTheme current() {
        LumenTheme base = switch (LumenConfig.CLIENT.theme.get()) {
            case "violet_lumen" -> VIOLET_LUMEN;
            case "midnight_rose" -> MIDNIGHT_ROSE;
            default -> AQUA_LUMEN;
        };
        String override = LumenConfig.CLIENT.accentOverride.get();
        if (override != null && override.matches("(?i)[0-9a-f]{6}")) {
            int accent = 0xFF000000 | Integer.parseInt(override, 16);
            return new LumenTheme(base.id(), base.canvas(), base.surface(), base.raised(), base.border(),
                    base.text(), base.textDim(), accent, base.accentAlt(), base.gold(), base.danger(), base.success());
        }
        return base;
    }

    /** Panel colour with the configured opacity applied. */
    public int panel() {
        float opacity = (float) (double) LumenConfig.CLIENT.panelOpacity.get();
        int alpha = (int) (Math.min(1.0F, Math.max(0.0F, opacity)) * 255.0F);
        return (alpha << 24) | (surface & 0x00FFFFFF);
    }

    public int accentSoft(float alpha) {
        int a = (int) (Math.min(1.0F, Math.max(0.0F, alpha)) * 255.0F);
        return (a << 24) | (accent & 0x00FFFFFF);
    }

    public int shade(int color, float alpha) {
        int a = (int) (Math.min(1.0F, Math.max(0.0F, alpha)) * 255.0F);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
