package net.aquatech.ui.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks AquaTech machines that are currently working (progress/burn &gt; 0)
 * so daily MACHINE contracts avoid scanning a 13×7×13 cube every second.
 */
public final class WorkingMachineTracker {

    private static final ConcurrentHashMap<Long, Level> WORKING = new ConcurrentHashMap<>();

    private WorkingMachineTracker() {
    }

    public static void setWorking(Level level, BlockPos pos, boolean working) {
        if (level == null || level.isClientSide || pos == null) return;
        long key = pos.asLong();
        if (working) {
            WORKING.put(key, level);
        } else {
            WORKING.remove(key, level);
        }
    }

    public static boolean hasWorkingNear(Level level, BlockPos center, int rangeXZ, int rangeY) {
        if (level == null || WORKING.isEmpty()) return false;
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        Iterator<Map.Entry<Long, Level>> it = WORKING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Level> e = it.next();
            if (e.getValue() != level) continue;
            BlockPos p = BlockPos.of(e.getKey());
            if (Math.abs(p.getX() - cx) > rangeXZ) continue;
            if (Math.abs(p.getZ() - cz) > rangeXZ) continue;
            if (Math.abs(p.getY() - cy) > rangeY) continue;
            // Stale entry if chunk unloaded — drop
            if (!level.isLoaded(p)) {
                it.remove();
                continue;
            }
            return true;
        }
        return false;
    }

    public static void clear() {
        WORKING.clear();
    }
}
