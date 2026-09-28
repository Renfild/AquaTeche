package net.aquatech.ui.client;

import net.minecraft.core.BlockPos;

/** Клиентская копия личной точки лова: пишет S2CSpotPacket, читают стрелка и луч. */
public final class ClientSpotState {

    private static volatile boolean active;
    private static volatile BlockPos pos = BlockPos.ZERO;
    private static volatile long expiresAtMs;
    private static volatile int catchesLeft;
    private static volatile int radius;
    private static volatile String typeLabel = "";
    private static volatile float priceMult;
    private static volatile int color = 0x3FD8E8;

    private ClientSpotState() {
    }

    public static void set(BlockPos newPos, long remainingMs, int newCatchesLeft, int newRadius,
                            String newTypeLabel, float newPriceMult, int newColor) {
        pos = newPos;
        expiresAtMs = System.currentTimeMillis() + remainingMs;
        catchesLeft = newCatchesLeft;
        radius = newRadius;
        typeLabel = newTypeLabel;
        priceMult = newPriceMult;
        color = newColor;
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

    public static String typeLabel() {
        return typeLabel;
    }

    public static int color() {
        return color;
    }

    /** "2" или "3.5" — без лишних нулей после точки. */
    public static String multLabel() {
        return priceMult == (float) Math.floor(priceMult) ? String.valueOf((int) priceMult) : String.valueOf(priceMult);
    }
}
