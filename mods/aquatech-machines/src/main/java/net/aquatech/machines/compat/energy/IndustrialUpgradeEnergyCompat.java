package net.aquatech.machines.compat.energy;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Адаптер интеграции с энергосетью Industrial Upgrade (EnergyNet, EU -> FE).
 * Регистрирует механизмы в EnergyNet и позволяет разряжать аккумуляторы IU в слотах батарей.
 */
public final class IndustrialUpgradeEnergyCompat {

    private static boolean probed;
    private static boolean iuAvailable;
    private static Constructor<?> energyForgeSinkCtor;
    private static Constructor<?> loadEventCtor;
    private static Constructor<?> unloadEventCtor;

    private static Object electricItemManager;
    private static Method getChargeMethod;
    private static Method getMaxChargeMethod;
    private static Method getTierMethod;
    private static Method dischargeMethod;

    private static Class<?> energySourceClass;
    private static Method canExtractDirMethod;
    private static Method canExtractMethod;
    private static Method extractDirMethod;
    private static Method extractMethod;

    private static final Map<BlockEntity, Object> ACTIVE_SINKS = new ConcurrentHashMap<>();

    private IndustrialUpgradeEnergyCompat() {
    }

    private static void ensureProbed() {
        if (probed) return;
        probed = true;
        try {
            Class<?> sinkClass = Class.forName("com.denfop.api.energy.forgeenergy.EnergyForgeSink");
            energyForgeSinkCtor = sinkClass.getConstructor(BlockEntity.class);

            Class<?> tileClass = Class.forName("com.denfop.api.energy.interfaces.EnergyTile");
            Class<?> loadClass = Class.forName("com.denfop.api.energy.event.load.EnergyTileLoadEvent");
            try {
                loadEventCtor = loadClass.getConstructor(Level.class, BlockEntity.class, tileClass);
            } catch (Throwable ignored) {
                loadEventCtor = loadClass.getConstructor(Level.class, tileClass);
            }

            Class<?> unloadClass = Class.forName("com.denfop.api.energy.event.unload.EnergyTileUnLoadEvent");
            unloadEventCtor = unloadClass.getConstructor(Level.class, tileClass);

            iuAvailable = true;
        } catch (Throwable t) {
            iuAvailable = false;
        }

        try {
            Class<?> utilsClass = Class.forName("com.denfop.utils.ElectricItem");
            Field managerField = utilsClass.getField("manager");
            electricItemManager = managerField.get(null);
            if (electricItemManager != null) {
                Class<?> managerClass = electricItemManager.getClass();
                getChargeMethod = managerClass.getMethod("getCharge", ItemStack.class);
                getMaxChargeMethod = managerClass.getMethod("getMaxCharge", ItemStack.class);
                getTierMethod = managerClass.getMethod("getTier", ItemStack.class);
                dischargeMethod = managerClass.getMethod("discharge",
                        ItemStack.class, double.class, int.class, boolean.class, boolean.class, boolean.class);
            }
        } catch (Throwable ignored) {
        }

        try {
            energySourceClass = Class.forName("com.denfop.api.energy.interfaces.EnergySource");
            try {
                canExtractDirMethod = energySourceClass.getMethod("canExtractEnergy", net.minecraft.core.Direction.class);
            } catch (Throwable ignored) {
                canExtractMethod = energySourceClass.getMethod("canExtractEnergy");
            }
            try {
                extractDirMethod = energySourceClass.getMethod("extractEnergy", net.minecraft.core.Direction.class, double.class);
            } catch (Throwable ignored) {
                extractMethod = energySourceClass.getMethod("extractEnergy", double.class);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void registerMachine(BlockEntity be) {
        if (be == null || be.getLevel() == null || be.getLevel().isClientSide) return;
        ensureProbed();
        if (!iuAvailable || energyForgeSinkCtor == null || loadEventCtor == null) return;
        try {
            if (ACTIVE_SINKS.containsKey(be)) return;
            Object sink = energyForgeSinkCtor.newInstance(be);
            Event loadEvent;
            if (loadEventCtor.getParameterCount() == 3) {
                loadEvent = (Event) loadEventCtor.newInstance(be.getLevel(), be, sink);
            } else {
                loadEvent = (Event) loadEventCtor.newInstance(be.getLevel(), sink);
            }
            MinecraftForge.EVENT_BUS.post(loadEvent);
            ACTIVE_SINKS.put(be, sink);
        } catch (Throwable ignored) {
        }
    }

    public static void unregisterMachine(BlockEntity be) {
        if (be == null || be.getLevel() == null || be.getLevel().isClientSide) return;
        ensureProbed();
        if (!iuAvailable || unloadEventCtor == null) return;
        try {
            Object sink = ACTIVE_SINKS.remove(be);
            if (sink != null) {
                Event unloadEvent = (Event) unloadEventCtor.newInstance(be.getLevel(), sink);
                MinecraftForge.EVENT_BUS.post(unloadEvent);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Пул энергии из соседнего генератора/аккумулятора Industrial Upgrade (EnergySource). 1 EU = 4 FE.
     */
    public static int extractFromNeighbor(BlockEntity neighbor, net.minecraft.core.Direction fromSide, int maxFe) {
        if (neighbor == null || maxFe <= 0) return 0;
        ensureProbed();
        if (!iuAvailable || energySourceClass == null) return 0;
        if (energySourceClass.isInstance(neighbor)) {
            try {
                double canExtract = 0.0;
                if (canExtractDirMethod != null) {
                    canExtract = ((Number) canExtractDirMethod.invoke(neighbor, fromSide)).doubleValue();
                } else if (canExtractMethod != null) {
                    canExtract = ((Number) canExtractMethod.invoke(neighbor)).doubleValue();
                }
                if (canExtract <= 0.0) return 0;
                double wantEu = Math.min(canExtract, maxFe / 4.0);
                if (wantEu <= 0.0) return 0;
                if (extractDirMethod != null) {
                    extractDirMethod.invoke(neighbor, fromSide, wantEu);
                } else if (extractMethod != null) {
                    extractMethod.invoke(neighbor, wantEu);
                }
                return (int) Math.round(wantEu * 4.0);
            } catch (Throwable ignored) {
            }
        }
        return 0;
    }

    public static boolean isElectricItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ensureProbed();
        if (electricItemManager != null && getMaxChargeMethod != null) {
            try {
                double max = ((Number) getMaxChargeMethod.invoke(electricItemManager, stack)).doubleValue();
                if (max > 0) return true;
            } catch (Throwable ignored) {
            }
        }
        String className = stack.getItem().getClass().getName();
        return className.contains("Electric") || className.contains("com.denfop");
    }

    /**
     * Разряжает аккумулятор Industrial Upgrade. 1 EU = 4 FE.
     * Возвращает полученное количество FE.
     */
    public static int dischargeBattery(ItemStack battery, int maxFeNeeded) {
        if (battery == null || battery.isEmpty() || maxFeNeeded <= 0) return 0;
        ensureProbed();
        if (electricItemManager == null || getChargeMethod == null || dischargeMethod == null) return 0;
        try {
            double currentCharge = ((Number) getChargeMethod.invoke(electricItemManager, battery)).doubleValue();
            if (currentCharge <= 0.0) return 0;
            double wantEu = Math.min(currentCharge, maxFeNeeded / 4.0);
            if (wantEu <= 0.0) return 0;
            int tier = 1;
            if (getTierMethod != null) {
                tier = ((Number) getTierMethod.invoke(electricItemManager, battery)).intValue();
            }
            double discharged = ((Number) dischargeMethod.invoke(electricItemManager,
                    battery, wantEu, tier, true, false, false)).doubleValue();
            return (int) Math.round(discharged * 4.0);
        } catch (Throwable ignored) {
            return 0;
        }
    }
}
