package net.aquatech.ui.fishing;

/**
 * Старый Лей: легендарная рыба, которая раз в неделю всплывает на {@link #WINDOW_MS} в одной точке и достаётся
 * только одному игроку. Чистая логика расписания и условий поимки без Minecraft, чтобы её можно было тестировать.
 */
public final class LeyLogic {

    /** Радиус зоны вокруг точки появления, в блоках. */
    public static final int RADIUS = 56;
    public static final long WINDOW_MS = 30L * 60_000L;
    public static final long REWARD_COINS = 50_000L;

    private static final long DAY_MS = 24L * 3600_000L;
    private static final long WEEK_TAIL_MS = 6L * 3600_000L;

    private static final int MIN_GRAMS = 200_000;
    private static final int MAX_GRAMS = 320_000;
    private static final int MIN_CM = 520;
    private static final int MAX_CM = 640;

    public static final class State {
        public int week = -1;
        public long appearAtMs;
        public boolean appeared;
        public boolean caught;
        /** Окно закончилось без поимки и уже объявлено; новое появление до конца недели не будет. */
        public boolean closed;
        public String caughtBy = "";
        public String caughtByUuid = "";
        public int caughtGrams;
        public long activeUntilMs;
        public String dimension = "";
        public double x;
        /** Высота игрока в момент появления: вода ищется рядом с ней, а не по уровню моря. */
        public double y;
        public double z;
    }

    private LeyLogic() {
    }

    /** Новая неделя: сбрасывает состояние и назначает время появления не раньше вторника и не позже вечера воскресенья. */
    public static void startWeek(State s, int week, long weekStartMs, long weekEndMs, double roll01) {
        s.week = week;
        s.appeared = false;
        s.caught = false;
        s.closed = false;
        s.caughtBy = "";
        s.caughtByUuid = "";
        s.caughtGrams = 0;
        s.activeUntilMs = 0L;
        s.dimension = "";
        long from = weekStartMs + DAY_MS;
        long to = weekEndMs - WEEK_TAIL_MS - WINDOW_MS;
        double r = Math.max(0.0, Math.min(1.0, roll01));
        s.appearAtMs = from + (long) (r * (to - from));
    }

    public static boolean due(State s, long nowMs, int playersOnline) {
        return !s.appeared && playersOnline > 0 && nowMs >= s.appearAtMs;
    }

    public static void appear(State s, long nowMs, String dimension, double x, double z) {
        s.appeared = true;
        s.activeUntilMs = nowMs + WINDOW_MS;
        s.dimension = dimension;
        s.x = x;
        s.z = z;
    }

    public static boolean active(State s, long nowMs) {
        return s.appeared && !s.caught && !s.closed && nowMs < s.activeUntilMs;
    }

    /** Окно закончилось, а Лея так никто и не поймал. */
    public static boolean expired(State s, long nowMs) {
        return s.appeared && !s.caught && !s.closed && nowMs >= s.activeUntilMs;
    }

    public static void close(State s) {
        s.closed = true;
    }

    public static boolean inZone(State s, String dimension, double x, double z) {
        if (!s.dimension.equals(dimension)) {
            return false;
        }
        double dx = x - s.x;
        double dz = z - s.z;
        return dx * dx + dz * dz <= (double) RADIUS * RADIUS;
    }

    /** Можно ли начать схватку: окно открыто, Лея ещё никто не поймал, игрок в зоне. */
    public static boolean canFight(State s, long nowMs, String dimension, double x, double z) {
        return active(s, nowMs) && inZone(s, dimension, x, z);
    }

    public static void markCaught(State s, String name, String uuid, int grams) {
        s.caught = true;
        s.caughtBy = name;
        s.caughtByUuid = uuid;
        s.caughtGrams = grams;
    }

    public static int rollGrams(double u01) {
        return MIN_GRAMS + (int) (clamp01(u01) * (MAX_GRAMS - MIN_GRAMS));
    }

    public static int rollCm(double u01) {
        return MIN_CM + (int) (clamp01(u01) * (MAX_CM - MIN_CM));
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
