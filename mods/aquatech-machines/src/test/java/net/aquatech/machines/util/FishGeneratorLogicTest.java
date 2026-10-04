package net.aquatech.machines.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FishGeneratorLogicTest {

    @Test
    void oneFishPaysForExactlyTwoCatchesOfTheMk1Fisher() {
        int energyPerCatch = net.aquatech.machines.block.entity.FisherMk1BlockEntity.ENERGY_PER_TICK
                * net.aquatech.machines.block.entity.FisherMk1BlockEntity.CYCLE_TICKS;
        assertEquals(8000, FishGeneratorLogic.FE_PER_FISH);
        assertEquals(2 * energyPerCatch, FishGeneratorLogic.FE_PER_FISH);
    }

    @Test
    void coalKeepsOneMk1FisherRunning() {
        assertEquals(net.aquatech.machines.block.entity.FisherMk1BlockEntity.ENERGY_PER_TICK,
                FishGeneratorLogic.rateForBurnTicks(1600));
    }

    @Test
    void rateGrowsWithFuelDensity() {
        assertEquals(20, FishGeneratorLogic.rateForBurnTicks(300));   // доски, брёвна
        assertEquals(20, FishGeneratorLogic.rateForBurnTicks(100));   // палка
        assertEquals(40, FishGeneratorLogic.rateForBurnTicks(1600));  // уголь, древесный уголь
        assertEquals(60, FishGeneratorLogic.rateForBurnTicks(2400));  // блейз-стержень
        assertEquals(60, FishGeneratorLogic.rateForBurnTicks(4000));  // блок сушёного кельпа
        assertEquals(80, FishGeneratorLogic.rateForBurnTicks(16000)); // блок угля
        assertEquals(80, FishGeneratorLogic.rateForBurnTicks(20000)); // ведро лавы
    }

    @Test
    void differentFuelGivesDifferentTotalEnergy() {
        long wood = FishGeneratorLogic.totalEnergy(300, FishGeneratorLogic.rateForBurnTicks(300));
        long coal = FishGeneratorLogic.totalEnergy(1600, FishGeneratorLogic.rateForBurnTicks(1600));
        assertEquals(6000, wood);
        assertEquals(64000, coal);
        assertTrue(coal > wood);
        assertTrue(coal <= FishGeneratorLogic.CAPACITY);
    }

    @Test
    void burningPausesWhenTheBufferHasNoRoomForAWholeTick() {
        int cap = FishGeneratorLogic.CAPACITY;
        int rate = FishGeneratorLogic.RATE_COAL;
        assertTrue(FishGeneratorLogic.canBurnThisTick(0, cap, rate));
        assertTrue(FishGeneratorLogic.canBurnThisTick(cap - rate, cap, rate));
        assertFalse(FishGeneratorLogic.canBurnThisTick(cap - rate + 1, cap, rate));
        assertFalse(FishGeneratorLogic.canBurnThisTick(cap, cap, rate));
    }
}
