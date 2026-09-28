package net.aquatech.ui.fishing;

import net.minecraft.util.RandomSource;

/**
 * Тип личной точки лова. Параметры зашиты константами прямо в enum — тот же приём,
 * что у {@link FishGrade#PRICE_MULT}: баланс тюнится в коде, не в конфиге.
 */
public enum SpotType {

    /** Обычная: как было раньше, просто ×2 к цене. Выпадает чаще всего. */
    ZAVOD("Заводь", "§b", 2.0, 10, 40, 85, false, 0x3FD8E8),
    /** Редкая: живёт вдвое меньше и уловов даёт меньше, зато дороже и может поднять грейд рыбы без грейда. */
    ZHILA("Жила", "§e", 3.5, 5, 15, 15, true, 0xF5C25B);

    private final String label;
    private final String chatColor;
    private final double priceMult;
    private final int lifetimeMinutes;
    private final int maxCatches;
    private final int weight;
    private final boolean gradeBoost;
    private final int colorRgb;

    SpotType(String label, String chatColor, double priceMult, int lifetimeMinutes,
             int maxCatches, int weight, boolean gradeBoost, int colorRgb) {
        this.label = label;
        this.chatColor = chatColor;
        this.priceMult = priceMult;
        this.lifetimeMinutes = lifetimeMinutes;
        this.maxCatches = maxCatches;
        this.weight = weight;
        this.gradeBoost = gradeBoost;
        this.colorRgb = colorRgb;
    }

    public String label() {
        return label;
    }

    public String chatColor() {
        return chatColor;
    }

    public double priceMult() {
        return priceMult;
    }

    public int lifetimeMinutes() {
        return lifetimeMinutes;
    }

    public int maxCatches() {
        return maxCatches;
    }

    /** Жила: шанс поднять уже пойманную рыбу без грейда до серебра. */
    public boolean gradeBoost() {
        return gradeBoost;
    }

    /** 0xRRGGBB для луча и стрелки на клиенте. */
    public int colorRgb() {
        return colorRgb;
    }

    /** "2" или "3.5" — без лишних нулей после точки. */
    public String multLabel() {
        return FishingSpotService.formatMult((float) priceMult);
    }

    public static SpotType roll(RandomSource random) {
        int total = 0;
        for (SpotType t : values()) total += t.weight;
        int r = random.nextInt(total);
        for (SpotType t : values()) {
            r -= t.weight;
            if (r < 0) return t;
        }
        return ZAVOD;
    }
}
