package net.aquatech.ui.util;

import net.minecraftforge.energy.EnergyStorage;

/**
 * Forge Energy storage that notifies a listener on any change
 * (cable receive/extract, internal add/consume, setEnergy).
 */
public class CustomEnergyStorage extends EnergyStorage {

    private Runnable onChanged = () -> {};

    public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract, 0);
    }

    public CustomEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer, maxTransfer, 0);
    }

    public CustomEnergyStorage withListener(Runnable onChanged) {
        this.onChanged = onChanged != null ? onChanged : () -> {};
        return this;
    }

    private void notifyChanged() {
        onChanged.run();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) {
            notifyChanged();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) {
            notifyChanged();
        }
        return extracted;
    }

    public void setEnergy(int energy) {
        int clamped = Math.max(0, Math.min(capacity, energy));
        if (clamped != this.energy) {
            this.energy = clamped;
            notifyChanged();
        }
    }

    public int addEnergy(int amount) {
        int energyAdded = Math.min(capacity - energy, amount);
        if (energyAdded > 0) {
            this.energy += energyAdded;
            notifyChanged();
        }
        return energyAdded;
    }

    public int consumeEnergy(int amount) {
        int energyConsumed = Math.min(energy, amount);
        if (energyConsumed > 0) {
            this.energy -= energyConsumed;
            notifyChanged();
        }
        return energyConsumed;
    }
}
