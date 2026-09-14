package net.aquatech.machines.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Интеграция с маной Botania (чистая рефлексия, без compile-зависимости — как IU-компат).
 * ManaPool extends ManaReceiver: receiveMana(-x) забирает ману из пула (Цветолов),
 * receiveMana(+x) кладёт ману в пул (Мана-Фабрикатор), isFull() подскажет, есть ли место.
 */
public final class BotaniaManaBridge {

    private static boolean probed;
    private static boolean botaniaAvailable;
    private static Class<?> manaReceiverClass;
    private static final Map<String, Method> METHODS = new ConcurrentHashMap<>();

    private BotaniaManaBridge() {
    }

    private static void ensureProbed() {
        if (probed) return;
        probed = true;
        try {
            manaReceiverClass = Class.forName("vazkii.botania.api.mana.ManaReceiver");
            botaniaAvailable = true;
        } catch (Throwable t) {
            botaniaAvailable = false;
        }
    }

    public static boolean available() {
        ensureProbed();
        return botaniaAvailable;
    }

    private static Method method(Class<?> owner, String name, Class<?>... params) throws Exception {
        String key = owner.getName() + "#" + name;
        Method m = METHODS.get(key);
        if (m == null) {
            m = owner.getMethod(name, params);
            m.setAccessible(true);
            METHODS.put(key, m);
        }
        return m;
    }

    /**
     * Списывает amount маны из первого соседнего ManaReceiver с достаточным запасом.
     * @return true — мана списана, false — маны нет / Botania не установлен.
     */
    public static boolean drainFromNeighbors(Level level, BlockPos pos, int amount) {
        if (amount <= 0) return true;
        ensureProbed();
        if (!botaniaAvailable || level == null) return false;
        for (Direction side : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(side));
            if (be == null || !manaReceiverClass.isInstance(be)) continue;
            try {
                int has = (Integer) method(manaReceiverClass, "getCurrentMana").invoke(be);
                if (has < amount) continue;
                method(manaReceiverClass, "receiveMana", int.class).invoke(be, -amount);
                be.setChanged();
                return true;
            } catch (Throwable ignored) {
                // кривой чужой BE — пробуем следующий
            }
        }
        return false;
    }

    /**
     * Кладёт amount маны в первый соседний ManaReceiver, у которого есть место.
     * @return true — мана внесена, false — приёмников нет/полны/Botania не установлен.
     */
    public static boolean depositToNeighbors(Level level, BlockPos pos, int amount) {
        if (amount <= 0) return true;
        ensureProbed();
        if (!botaniaAvailable || level == null) return false;
        for (Direction side : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(side));
            if (be == null || !manaReceiverClass.isInstance(be)) continue;
            try {
                boolean full = (Boolean) method(manaReceiverClass, "isFull").invoke(be);
                if (full) continue;
                method(manaReceiverClass, "receiveMana", int.class).invoke(be, amount);
                be.setChanged();
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    /** Есть ли соседний ManaReceiver, готовый принять ману. */
    public static boolean hasCapacity(Level level, BlockPos pos) {
        ensureProbed();
        if (!botaniaAvailable || level == null) return false;
        for (Direction side : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(side));
            if (be == null || !manaReceiverClass.isInstance(be)) continue;
            try {
                if (!(Boolean) method(manaReceiverClass, "isFull").invoke(be)) return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    /** Сколько маны доступно в соседних пулах (для тултипов/тестов). */
    public static int neighborMana(Level level, BlockPos pos) {
        ensureProbed();
        if (!botaniaAvailable || level == null) return 0;
        int total = 0;
        for (Direction side : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(side));
            if (be == null || !manaReceiverClass.isInstance(be)) continue;
            try {
                total += (Integer) method(manaReceiverClass, "getCurrentMana").invoke(be);
            } catch (Throwable ignored) {
            }
        }
        return total;
    }
}
