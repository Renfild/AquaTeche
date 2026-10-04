package net.aquatech.machines.util;

/**
 * Числа Рыбного генератора. Работает как печка: топит углём, деревом и любым горючим (время горения берётся из
 * предмета) или рыбой. Выработка зависит от топлива: дерево даёт 20 FE/t, уголь 40, блейз-стержень и блоки кельпа 60,
 * блок угля и лава 80. Энергию получают только авторыболовы, соседям другого типа и трубам генератор ничего не отдаёт.
 */
public final class FishGeneratorLogic {

    public static final int RATE_WOOD = 20;
    public static final int RATE_COAL = 40;
    public static final int RATE_DENSE = 60;
    public static final int RATE_BLOCK = 80;
    /** Рыба горит 200 тиков по 40 FE/t: 8000 FE, ровно два улова MK-1. */
    public static final int FISH_BURN_TICKS = 200;
    public static final int FISH_RATE = RATE_COAL;
    public static final int FE_PER_FISH = FISH_BURN_TICKS * FISH_RATE;
    public static final int MAX_RATE = RATE_BLOCK;
    public static final int CAPACITY = 100000;
    /** Сколько FE за тик генератор передаёт каждому соседнему авторыболову. */
    public static final int MAX_EXTRACT = 400;

    private FishGeneratorLogic() {
    }

    /** Выработка в FE/t по длительности горения предмета (как в печке): чем «плотнее» топливо, тем выше ставка. */
    public static int rateForBurnTicks(int burnTicks) {
        if (burnTicks <= 400) return RATE_WOOD;
        if (burnTicks <= 2000) return RATE_COAL;
        if (burnTicks <= 4000) return RATE_DENSE;
        return RATE_BLOCK;
    }

    /** Всего FE из одной единицы топлива. */
    public static long totalEnergy(int burnTicks, int rate) {
        return (long) burnTicks * rate;
    }

    /**
     * Топливо горит, только пока в буфере есть место на целый тик выработки. Полный буфер ставит горение на паузу,
     * поэтому энергия не пропадает впустую, а топливо не сгорает зря.
     */
    public static boolean canBurnThisTick(int stored, int capacity, int rate) {
        return stored + rate <= capacity;
    }
}
