package net.aquatech.machines.util;

import net.minecraftforge.energy.EnergyStorage;

/**
 * FE-хранилище машины: принимает энергию извне и изнутри без искусственных затыков.
 */
public class MachineEnergyStorage extends EnergyStorage {

    private final Runnable onChange;

    public MachineEnergyStorage(int capacity, int maxReceive, Runnable onChange) {
        super(capacity, maxReceive, 0);
        this.onChange = onChange;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate && onChange != null) {
            onChange.run();
        }
        return received;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
        if (onChange != null) {
            onChange.run();
        }
    }

    /** Машина тратит энергию изнутри. */
    public int consumeInternal(int amount) {
        int taken = Math.min(energy, amount);
        energy -= taken;
        if (taken > 0 && onChange != null) {
            onChange.run();
        }
        return taken;
    }

    /** Заливка от соседа через pull — напрямую в буфер. */
    public int receiveInternal(int amount) {
        int accepted = Math.min(amount, capacity - energy);
        energy += accepted;
        if (accepted > 0 && onChange != null) {
            onChange.run();
        }
        return accepted;
    }

    public int getEnergy() {
        return energy;
    }
}
