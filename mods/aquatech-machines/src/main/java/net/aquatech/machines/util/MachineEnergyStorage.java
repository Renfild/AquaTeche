package net.aquatech.machines.util;

import net.minecraftforge.energy.EnergyStorage;

/**
 * FE-хранилище машины: принимает энергию извне, тратит изнутри.
 * receiveInternal — заливка от pull-механизма без ограничений стороны.
 */
public class MachineEnergyStorage extends EnergyStorage {

    public MachineEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, 0);
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    /** Машина тратит энергию изнутри. */
    public int consumeInternal(int amount) {
        int taken = Math.min(energy, amount);
        energy -= taken;
        return taken;
    }

    /** Заливка от соседа через pull — без maxReceive-ограничения тика. */
    public int receiveInternal(int amount) {
        int accepted = Math.min(Math.min(amount, capacity - energy), Math.max(maxReceive, 512));
        energy += accepted;
        return accepted;
    }

    public int getEnergy() {
        return energy;
    }
}
