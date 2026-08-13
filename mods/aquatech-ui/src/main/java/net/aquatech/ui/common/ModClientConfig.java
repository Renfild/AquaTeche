package net.aquatech.ui.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue HUD_MARGIN_RIGHT;
    public static final ForgeConfigSpec.IntValue HUD_MARGIN_TOP;
    public static final ForgeConfigSpec.IntValue HUD_WIDTH;
    public static final ForgeConfigSpec.DoubleValue HUD_SCALE;
    public static final ForgeConfigSpec.BooleanValue SHOW_PRESSURE;
    public static final ForgeConfigSpec.BooleanValue HUD_VISIBLE;
    public static final ForgeConfigSpec.ConfigValue<String> PORTAL_BASE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("hud");
        HUD_VISIBLE = builder
                .comment("Показывать ли океанский HUD вообще")
                .define("visible", true);
        HUD_MARGIN_RIGHT = builder
                .comment("Отступ HUD от правого края экрана (px)")
                .defineInRange("marginRight", 12, 0, 400);
        HUD_MARGIN_TOP = builder
                .comment("Отступ HUD от верхнего края экрана (px). Увеличь, чтобы опустить ниже.")
                .defineInRange("marginTop", 80, 0, 400);
        HUD_WIDTH = builder
                .comment("Ширина панели HUD в логических пикселях (до масштабирования)")
                .defineInRange("width", 118, 90, 260);
        HUD_SCALE = builder
                .comment("Общий масштаб панели HUD. 1.0 = обычный размер, меньше — компактнее.")
                .defineInRange("scale", 0.62, 0.4, 1.3);
        SHOW_PRESSURE = builder
                .comment("Показывать строку давления воды при погружении")
                .define("showPressure", true);
        builder.pop();
        builder.push("web");
        PORTAL_BASE = builder
                .comment("Origin for in-game CEF overlays (donate / cabinet embeds)")
                .define("portalBase", "https://aquateche.store");
        builder.pop();
        SPEC = builder.build();
    }
}
