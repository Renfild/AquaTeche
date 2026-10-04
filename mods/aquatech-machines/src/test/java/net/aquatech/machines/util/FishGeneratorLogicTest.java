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
    void oneGeneratorKeepsOneMk1FisherRunning() {
        assertEquals(net.aquatech.machines.block.entity.FisherMk1BlockEntity.ENERGY_PER_TICK, FishGeneratorLogic.FE_PER_TICK);
    }

    @Test
    void anItemOfCoalGivesSixtyFourThousandFeAndFitsTheBuffer() {
        int coalEnergy = 1600 * FishGeneratorLogic.FE_PER_TICK;
        assertEquals(64000, coalEnergy);
        assertTrue(coalEnergy <= FishGeneratorLogic.CAPACITY);
    }

    @Test
    void burningPausesWhenTheBufferHasNoRoomForAWholeTick() {
        int cap = FishGeneratorLogic.CAPACITY;
        assertTrue(FishGeneratorLogic.canBurnThisTick(0, cap));
        assertTrue(FishGeneratorLogic.canBurnThisTick(cap - FishGeneratorLogic.FE_PER_TICK, cap));
        assertFalse(FishGeneratorLogic.canBurnThisTick(cap - FishGeneratorLogic.FE_PER_TICK + 1, cap));
        assertFalse(FishGeneratorLogic.canBurnThisTick(cap, cap));
    }
}
