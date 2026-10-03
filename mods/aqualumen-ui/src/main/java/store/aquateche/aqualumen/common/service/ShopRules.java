package store.aquateche.aqualumen.common.service;

import java.util.Locale;
import java.util.Set;

/** Pure shop rules: what the in-game store may sell and for how much. */
public final class ShopRules {

    /** Premium season pass in gems; the website sells the same pass directly for rubles. */
    public static final long PASS_PREMIUM_GEMS = 1000L;

    private static final Set<String> RANKS = Set.of("sailor", "skipper", "captain", "admiral", "legend", "vip");

    private ShopRules() {
    }

    /** Ranks are sold for rubles on the website only: true for "rank.vip", "vip", "VIP" and the like. */
    public static boolean isRankId(String id) {
        if (id == null) {
            return false;
        }
        String key = id.trim().toLowerCase(Locale.ROOT);
        if (key.startsWith("rank.")) {
            key = key.substring("rank.".length());
        }
        return RANKS.contains(key);
    }
}
