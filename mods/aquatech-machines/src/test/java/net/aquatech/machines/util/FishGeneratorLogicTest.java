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
    void aFishIsOnlyBurnedWhenItsWholeEnergyFitsTheBuffer() {
        int cap = FishGeneratorLogic.CAPACITY;
        assertTrue(FishGeneratorLogic.canStartBurn(0, cap));
        assertTrue(FishGeneratorLogic.canStartBurn(cap - FishGeneratorLogic.FE_PER_FISH, cap));
        assertFalse(FishGeneratorLogic.canStartBurn(cap - FishGeneratorLogic.FE_PER_FISH + 1, cap));
    }

    @Test
    void generationNeverOverflowsTheBuffer() {
        int cap = FishGeneratorLogic.CAPACITY;
        assertEquals(FishGeneratorLogic.FE_PER_TICK, FishGeneratorLogic.generatedThisTick(0, cap));
        assertEquals(5, FishGeneratorLogic.generatedThisTick(cap - 5, cap));
        assertEquals(0, FishGeneratorLogic.generatedThisTick(cap, cap));
        assertEquals(0, FishGeneratorLogic.generatedThisTick(cap + 10, cap));
    }
}
