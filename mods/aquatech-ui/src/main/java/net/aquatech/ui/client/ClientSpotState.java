package net.aquatech.ui.client;

import net.minecraft.core.BlockPos;

/** Клиентская копия личной точки лова: пишет S2CSpotPacket, читают стрелка и луч. */
public final class ClientSpotState {

    private static volatile boolean active;
    private static volatile BlockPos pos = BlockPos.ZERO;
    private static volatile long expiresAtMs;
    private static volatile int catchesLeft;
    private static volatile int radius;

    private ClientSpotState() {
    }

    public static void set(BlockPos newPos, long remainingMs, int newCatchesLeft, int newRadius) {
        pos = newPos;
        expiresAtMs = System.currentTimeMillis() + remainingMs;
        catchesLeft = newCatchesLeft;
        radius = newRadius;
        active = true;
    }

    public static void clear() {
        active = false;
    }

    /** Активна и не просрочена по клиентским часам. */
    public static boolean active() {
        return active && System.currentTimeMillis() < expiresAtMs;
    }

    public static BlockPos pos() {
        return pos;
    }

    public static long expiresAtMs() {
        return expiresAtMs;
    }

    public static int catchesLeft() {
        return catchesLeft;
    }

    public static int radius() {
        return radius;
    }
}
