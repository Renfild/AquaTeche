package net.aquatech.machines.util;

/**
 * Числа Рыбного генератора. Одна рыба горит 400 тиков по 20 FE/t, то есть даёт 8000 FE.
 * Рыболову MK-1 на один улов нужно 4000 FE, значит рыбы хватает ровно на два улова.
 */
public final class FishGeneratorLogic {

    public static final int BURN_TICKS = 400;
    public static final int FE_PER_TICK = 20;
    public static final int FE_PER_FISH = BURN_TICKS * FE_PER_TICK;
    public static final int CAPACITY = 40000;
    /** Сколько FE за тик отдаёт соседям (для труб и машин, которые сами не тянут). */
    public static final int MAX_EXTRACT = 400;

    private FishGeneratorLogic() {
    }

    /** Новую рыбу кладём в огонь, только если вся её энергия поместится в буфер: ничего не сгорает впустую. */
    public static boolean canStartBurn(int stored, int capacity) {
        return stored + FE_PER_FISH <= capacity;
    }

    /** Энергия, которую можно добавить за этот тик, не переполнив буфер. */
    public static int generatedThisTick(int stored, int capacity) {
        return Math.max(0, Math.min(FE_PER_TICK, capacity - stored));
    }
}
