package net.aquatech.machines.util;

import net.minecraft.core.Direction;
import net.minecraftforge.energy.EnergyStorage;

/** FE-хранилище машины: принимает только по не-ап-стороне, извне — только приём. */
public class MachineEnergyStorage extends EnergyStorage {

    public MachineEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, 0);
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    /** Машина тратит энергию изнутри (без side-проверок). */
    public int consumeInternal(int amount) {
        int taken = Math.min(energy, amount);
        energy -= taken;
        return taken;
    }

    public int getEnergy() {
        return energy;
    }

    public boolean canReceiveFrom(Direction side) {
        return true;
    }
}
