package net.aquatech.ui.fishing;

import java.util.Random;
import java.util.UUID;

/** Чистая логика события «Сокровище из глубин»: без Minecraft-классов, покрыта JUnit. */
final class DeepChestLogic {

    static final int MIN_COINS = 5000;
    static final int MAX_COINS = 10000;
    static final double KEY_CHANCE = 0.30;
    static final String[] CASE_KEYS = {"flora", "steam", "smeltery"};

    record Reward(int coins, String caseId) {
    }

    private DeepChestLogic() {
    }

    /** Монеты 5000–10000 с шагом 100; с шансом {@link #KEY_CHANCE} ещё и ключ кейса. */
    static Reward rollReward(Random random) {
        int coins = MIN_COINS + 100 * random.nextInt((MAX_COINS - MIN_COINS) / 100 + 1);
        String caseId = random.nextDouble() < KEY_CHANCE ? CASE_KEYS[random.nextInt(CASE_KEYS.length)] : null;
        return new Reward(coins, caseId);
    }

    static long nextDelayMs(Random random, int minMinutes, int maxMinutes) {
        int max = Math.max(minMinutes, maxMinutes);
        return (minMinutes + random.nextInt(max - minMinutes + 1)) * 60_000L;
    }

    /** В Minecraft +X это восток, +Z это юг. */
    static String compass(double dx, double dz) {
        String[] names = {"на восток", "на юго-восток", "на юг", "на юго-запад",
                "на запад", "на северо-запад", "на север", "на северо-восток"};
        double degrees = Math.toDegrees(Math.atan2(dz, dx));
        int index = (int) Math.round(((degrees + 360.0) % 360.0) / 45.0) % 8;
        return names[index];
    }

    static int distance(double dx, double dz) {
        return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
    }

    static String timeLeft(long ms) {
        long seconds = Math.max(0L, (ms + 999L) / 1000L);
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    static float progress(long leftMs, long totalMs) {
        if (totalMs <= 0L) return 0.0f;
        return Math.max(0.0f, Math.min(1.0f, leftMs / (float) totalMs));
    }

    /** Прошлый победитель уступает, пока онлайн есть кто-то ещё. */
    static boolean mayClaim(UUID lastWinner, UUID player, int playersOnline) {
        return lastWinner == null || !lastWinner.equals(player) || playersOnline <= 1;
    }
}
