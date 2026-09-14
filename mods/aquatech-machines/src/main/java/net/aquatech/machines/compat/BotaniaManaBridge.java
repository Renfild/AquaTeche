package net.aquatech.machines.compat;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Интеграция с маной Botania (чистая рефлексия, без compile-зависимости — как IU-компат).
 * Машина-цветолов списывает ману из соседних ManaReceiver: мана-пул Botania
 * (ManaPool extends ManaReceiver) принимает отрицательный receiveMana — так пул и «съедается».
 * Искры/генераторы маны можно подключать напрямую к пулу рядом с машиной.
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
        for (var side : var_sides()) {
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

    /** Сколько маны доступно в соседних пулах (для тултипов/тестов). */
    public static int neighborMana(Level level, BlockPos pos) {
        ensureProbed();
        if (!botaniaAvailable || level == null) return 0;
        int total = 0;
        for (var side : var_sides()) {
            BlockEntity be = level.getBlockEntity(pos.relative(side));
            if (be == null || !manaReceiverClass.isInstance(be)) continue;
            try {
                total += (Integer) method(manaReceiverClass, "getCurrentMana").invoke(be);
            } catch (Throwable ignored) {
            }
        }
        return total;
    }

    private static net.minecraft.core.Direction[] var_sides() {
        return net.minecraft.core.Direction.values();
    }
}
