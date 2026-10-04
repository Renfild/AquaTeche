package net.aquatech.machines.util;

/**
 * Числа Рыбного генератора. Работает как печка: топит углём, деревом и любым горючим (время горения берётся из
 * предмета) или рыбой. Отдаёт 40 FE/t, этого ровно хватает на одного Рыболова MK-1. Уголь горит 1600 тиков и даёт
 * 64 000 FE, одна рыба горит 200 тиков и даёт 8000 FE, то есть два улова MK-1.
 */
public final class FishGeneratorLogic {

    public static final int FE_PER_TICK = 40;
    public static final int FISH_BURN_TICKS = 200;
    public static final int FE_PER_FISH = FISH_BURN_TICKS * FE_PER_TICK;
    public static final int CAPACITY = 100000;
    /** Сколько FE за тик отдаёт соседям (для труб и машин, которые сами не тянут). */
    public static final int MAX_EXTRACT = 400;

    private FishGeneratorLogic() {
    }

    /**
     * Топливо горит, только пока в буфере есть место на целый тик выработки. Полный буфер ставит горение на паузу,
     * поэтому энергия не пропадает впустую, а топливо не сгорает зря.
     */
    public static boolean canBurnThisTick(int stored, int capacity) {
        return stored + FE_PER_TICK <= capacity;
    }
}
