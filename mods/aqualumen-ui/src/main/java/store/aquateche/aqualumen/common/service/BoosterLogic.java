package store.aquateche.aqualumen.common.service;

import java.util.Locale;

/**
 * Rules of the fish-shop booster: a personal sale multiplier with a timer. Pure logic, no Minecraft types;
 * {@link BoosterService} stores the state in the player's persistent data.
 */
public final class BoosterLogic {

    public enum Tier {
        SMALL("small", "малый", 1.5, 30L * 60_000L),
        LARGE("large", "большой", 2.0, 15L * 60_000L);

        private final String id;
        private final String label;
        private final double mult;
        private final long durationMs;

        Tier(String id, String label, double mult, long durationMs) {
            this.id = id;
            this.label = label;
            this.mult = mult;
            this.durationMs = durationMs;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public double mult() {
            return mult;
        }

        public long durationMs() {
            return durationMs;
        }
    }

    public enum Result {
        OK,
        NO_STOCK,
        ALREADY_ACTIVE
    }

    public record Stock(int small, int large) {
        public int of(Tier tier) {
            return tier == Tier.SMALL ? small : large;
        }

        public Stock plus(Tier tier, int amount) {
            return tier == Tier.SMALL ? new Stock(small + amount, large) : new Stock(small, large + amount);
        }

        public int total() {
            return small + large;
        }
    }

    public record Active(double mult, long until) {
        public static final Active NONE = new Active(1.0, 0L);

        public boolean isActive(long now) {
            return until > now;
        }

        public long remainingMs(long now) {
            return Math.max(0L, until - now);
        }
    }

    public record Activation(Result result, Stock stock, Active active) {
    }

    private BoosterLogic() {
    }

    public static Tier parse(String text) {
        if (text == null) {
            return null;
        }
        String key = text.trim().toLowerCase(Locale.ROOT);
        for (Tier tier : Tier.values()) {
            if (tier.id.equals(key) || tier.label.equals(key)) {
                return tier;
            }
        }
        return null;
    }

    public static Activation activate(Stock stock, Active active, Tier tier, long now) {
        if (active.isActive(now)) {
            return new Activation(Result.ALREADY_ACTIVE, stock, active);
        }
        if (stock.of(tier) <= 0) {
            return new Activation(Result.NO_STOCK, stock, active);
        }
        return new Activation(Result.OK, stock.plus(tier, -1), new Active(tier.mult(), now + tier.durationMs()));
    }

    public static double multiplierAt(Active active, long now) {
        return active.isActive(now) ? active.mult() : 1.0;
    }

    /** The extra coins a booster adds to a sale of {@code baseCoins}. */
    public static long bonus(long baseCoins, double mult) {
        if (baseCoins <= 0L || mult <= 1.0) {
            return 0L;
        }
        return Math.round(baseCoins * (mult - 1.0));
    }

    /** m:ss, seconds rounded up so a running timer never shows 0:00. */
    public static String formatRemaining(long ms) {
        long seconds = ms <= 0L ? 0L : (ms + 999L) / 1000L;
        return (seconds / 60L) + ":" + String.format("%02d", seconds % 60L);
    }
}
