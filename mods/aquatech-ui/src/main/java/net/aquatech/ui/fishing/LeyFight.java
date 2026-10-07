package net.aquatech.ui.fishing;

import java.util.Random;

/**
 * Схватка со Старым Леем, чистая логика без Minecraft. Игрок держит леску в зелёной зоне натяжения: наматывает,
 * пока Лей спокоен, и отпускает на рывках, подтягивая удилище в сторону, противоположную рывку. Выносливость Лея
 * падает только пока леска натянута правильно; пережал или проспал рывок, и леска рвётся.
 */
public final class LeyFight {

    public static final double TENSION_MAX = 100.0;
    /** Зелёная зона натяжения: только в ней намотка выматывает Лея по-настоящему. */
    public static final double SAFE_LOW = 25.0;
    public static final double SAFE_HIGH = 80.0;
    public static final double ESCAPE_DISTANCE = 45.0;
    public static final double START_DISTANCE = 18.0;

    private static final double START_TENSION = 30.0;
    private static final int CALM_MIN_TICKS = 90;
    private static final int CALM_EXTRA_TICKS = 80;
    /** Предупреждение о рывке: стрелка на экране и всплеск воды. */
    private static final int WARN_TICKS = 24;
    private static final int SURGE_MIN_TICKS = 50;
    private static final int SURGE_EXTRA_TICKS = 30;
    /** В конце Лей сильнее: когда выносливости мало, рывки длиннее и злее. */
    private static final double DESPERATE_BELOW = 25.0;

    public enum Phase { CALM, WARN, SURGE }

    public enum Result { NONE, WON, SNAPPED, ESCAPED }

    public static final class State {
        public double tension = START_TENSION;
        public double stamina = 100.0;
        public double distance = START_DISTANCE;
        public Phase phase = Phase.CALM;
        /** Куда рвёт Лей: -1 влево, 1 вправо; 0 вне рывка. */
        public int surgeDir;
        public int phaseTicksLeft;
        public Result result = Result.NONE;
    }

    private LeyFight() {
    }

    public static State start(Random rng) {
        State s = new State();
        s.phaseTicksLeft = CALM_MIN_TICKS + rng.nextInt(CALM_EXTRA_TICKS);
        return s;
    }

    /**
     * Один тик схватки.
     *
     * @param reeling игрок держит ПКМ
     * @param steer   -1 влево (A), 1 вправо (D), 0 ничего
     */
    public static void step(State s, boolean reeling, int steer, Random rng) {
        if (s.result != Result.NONE) {
            return;
        }
        double wobble = (rng.nextDouble() - 0.5) * 0.5;
        boolean desperate = s.stamina < DESPERATE_BELOW;
        if (s.phase == Phase.SURGE) {
            boolean counter = steer == -s.surgeDir;
            double rage = desperate ? 1.2 : 1.0;
            if (counter) {
                s.tension += (reeling ? 1.1 : -0.4) * rage + wobble;
                s.distance += reeling ? 0.0 : 0.02;
                s.stamina -= 0.03;
            } else {
                s.tension += (2.6 + (reeling ? 0.8 : 0.0)) * rage + wobble;
                s.distance += 0.09;
            }
        } else if (reeling) {
            s.tension += 0.55 + wobble;
            s.stamina -= s.tension >= SAFE_LOW && s.tension <= SAFE_HIGH ? 0.11 : 0.03;
            s.distance -= 0.04;
        } else {
            s.tension -= 1.4;
            s.stamina += 0.12;
            s.distance += 0.01;
        }
        s.tension = Math.max(0.0, s.tension);
        s.stamina = Math.min(100.0, s.stamina);
        advancePhase(s, rng, desperate);

        if (s.tension >= TENSION_MAX) {
            s.result = Result.SNAPPED;
        } else if (s.distance >= ESCAPE_DISTANCE) {
            s.result = Result.ESCAPED;
        } else if (s.stamina <= 0.0) {
            s.stamina = 0.0;
            s.result = Result.WON;
        }
    }

    private static void advancePhase(State s, Random rng, boolean desperate) {
        if (--s.phaseTicksLeft > 0) {
            return;
        }
        switch (s.phase) {
            case CALM -> {
                s.phase = Phase.WARN;
                s.surgeDir = rng.nextBoolean() ? -1 : 1;
                s.phaseTicksLeft = WARN_TICKS;
            }
            case WARN -> {
                s.phase = Phase.SURGE;
                s.phaseTicksLeft = SURGE_MIN_TICKS + rng.nextInt(SURGE_EXTRA_TICKS) + (desperate ? 20 : 0);
            }
            case SURGE -> {
                s.phase = Phase.CALM;
                s.surgeDir = 0;
                s.phaseTicksLeft = CALM_MIN_TICKS + rng.nextInt(CALM_EXTRA_TICKS);
            }
        }
    }
}
