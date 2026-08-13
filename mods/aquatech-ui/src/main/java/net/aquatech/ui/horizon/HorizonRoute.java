package net.aquatech.ui.horizon;

/**
 * Horizon Route constants — meta-progression beside FTB Acts.
 * Tiers map to LuckPerms groups for display / homes.
 */
public final class HorizonRoute {

    public static final String[] TIER_NAMES = {
            "Пролог", "Матрос", "Шкипер", "Капитан", "Адмирал", "Легенда"
    };

    /** LuckPerms group granted at each tier (H0 = none / default). */
    public static final String[] LP_GROUPS = {
            "default", "sailor", "skipper", "captain", "admiral", "legend"
    };

    /** Fleet rank groups that must be cleared on promote/demote (excluding default). */
    public static final String[] FLEET_LP_GROUPS = {
            "sailor", "skipper", "captain", "admiral", "legend"
    };

    public static final int MAX_TIER = 5;
    public static final int SEASON_XP_PER_LEVEL = 100;
    public static final int SEASON_MAX_LEVEL = 40;
    public static final int DAILY_SEASON_XP = 30;
    public static final int DAILY_AQUA_XP = 40;

    public enum DailyContract {
        FISH("Улов дня", "Поймай ресурсы удочкой AquaTech", 15),
        DEPTH("Глубина дня", "Проведи время под давлением ≥8", 120),
        KELP("Водоросли дня", "Собери водоросли / морскую траву", 24),
        MACHINE("Цех дня", "Стой у работающей машины AquaTech", 60),
        MARKET("Рынок дня", "Собери медные монеты Lightman's", 16);

        public final String title;
        public final String description;
        public final int target;

        DailyContract(String title, String description, int target) {
            this.title = title;
            this.description = description;
            this.target = target;
        }
    }

    private HorizonRoute() {
    }

    public static String tierName(int tier) {
        int t = Math.max(0, Math.min(MAX_TIER, tier));
        return TIER_NAMES[t];
    }

    public static String lpGroup(int tier) {
        int t = Math.max(0, Math.min(MAX_TIER, tier));
        return LP_GROUPS[t];
    }

    public static boolean isFleetGroup(String groupId) {
        if (groupId == null) return false;
        String g = groupId.toLowerCase();
        for (String fleet : FLEET_LP_GROUPS) {
            if (fleet.equals(g)) return true;
        }
        return false;
    }
}
