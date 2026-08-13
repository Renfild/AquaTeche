package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Force StarCatcher rods to be damageable. KubeJS {@code item.maxDamage = N} on a raw
 * {@link Item} often no-ops (setter never runs), so rods stay at maxDamage 0 and show no bar.
 */
public final class RodDurabilityApplier {
    private RodDurabilityApplier() {
    }

    public static void apply() {
        int ok = 0;
        int fail = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null) continue;
            Integer max = maxForItemId(id);
            if (max == null) continue;
            if (item.getMaxDamage() == max) {
                ok++;
                continue;
            }
            if (writeMaxDamage(item, max) && item.getMaxDamage() == max) {
                ok++;
            } else {
                fail++;
                AquaTechUI.LOGGER.warn("[AquaTech] Could not set maxDamage={} on {}", max, id);
            }
        }
        AquaTechUI.LOGGER.info("[AquaTech] Rod maxDamage ready: ok={} fail={}", ok, fail);
    }

    private static Integer maxForItemId(ResourceLocation id) {
        if ("minecraft".equals(id.getNamespace()) && "fishing_rod".equals(id.getPath())) {
            return RodDurability.maxUsesForPath("bamboo_rod");
        }
        if (!"starcatcher".equals(id.getNamespace())) return null;
        String path = id.getPath();
        if (!path.endsWith("_rod") && !"starcatcher_rod".equals(path)) return null;
        return RodDurability.maxUsesForPath(path);
    }

    private static boolean writeMaxDamage(Item item, int value) {
        try {
            Field field = findMaxDamageField();
            Object unsafe = unsafe();
            Method objectFieldOffset = unsafe.getClass().getMethod("objectFieldOffset", Field.class);
            Method putInt = unsafe.getClass().getMethod("putInt", Object.class, long.class, int.class);
            long offset = (Long) objectFieldOffset.invoke(unsafe, field);
            putInt.invoke(unsafe, item, offset, value);
            return true;
        } catch (Throwable t) {
            AquaTechUI.LOGGER.warn("[AquaTech] writeMaxDamage failed: {}", t.toString());
            return false;
        }
    }

    private static Field findMaxDamageField() throws NoSuchFieldException {
        for (String name : new String[]{"maxDamage", "f_41370_"}) {
            try {
                Field f = Item.class.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException("Item.maxDamage");
    }

    private static Object unsafe() throws Exception {
        Field f = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return f.get(null);
    }
}
