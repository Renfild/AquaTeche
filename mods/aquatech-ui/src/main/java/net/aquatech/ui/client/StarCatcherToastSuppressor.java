package net.aquatech.ui.client;

import net.aquatech.ui.fishing.FishingRodCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

/**
 * StarCatcher shows FishCaughtToast before AquaTech cancels fish drops on resource rods.
 * Visible toasts live in ToastInstance wrappers — plain {@code instanceof Toast} misses them.
 */
public final class StarCatcherToastSuppressor {
    private static Field[] collectionFields;
    private static Method toastInstanceGetToast;
    private static boolean toastInstanceResolved;

    private StarCatcherToastSuppressor() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!holdingResourceRod(player)) return;

        removeFishCaughtToasts(mc.getToasts());
        // Action bar fish name is sent on every SC catch
        mc.gui.setOverlayMessage(Component.empty(), false);
    }

    private static boolean holdingResourceRod(LocalPlayer player) {
        return FishingRodCompat.isResourceRod(player.getMainHandItem())
                || FishingRodCompat.isResourceRod(player.getOffhandItem());
    }

    private static void removeFishCaughtToasts(ToastComponent toasts) {
        for (Field field : collectionFields(toasts.getClass())) {
            try {
                Object value = field.get(toasts);
                if (!(value instanceof Collection<?> col)) continue;
                Iterator<?> it = col.iterator();
                while (it.hasNext()) {
                    Object next = it.next();
                    if (isFishCaughtEntry(next)) {
                        it.remove();
                    }
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static boolean isFishCaughtEntry(Object entry) {
        if (entry == null) return false;
        if (entry instanceof Toast toast) {
            return isFishCaughtToast(toast);
        }
        Toast nested = extractToast(entry);
        if (nested != null) {
            return isFishCaughtToast(nested);
        }
        String name = entry.getClass().getName();
        return name.contains("FishCaughtToast");
    }

    private static Toast extractToast(Object toastInstance) {
        Method getter = toastInstanceGetter(toastInstance.getClass());
        if (getter != null) {
            try {
                Object result = getter.invoke(toastInstance);
                if (result instanceof Toast toast) return toast;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        for (Field field : toastInstance.getClass().getDeclaredFields()) {
            if (!Toast.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                Object value = field.get(toastInstance);
                if (value instanceof Toast toast) return toast;
            } catch (IllegalAccessException ignored) {
            }
        }
        return null;
    }

    private static Method toastInstanceGetter(Class<?> clazz) {
        if (toastInstanceResolved) return toastInstanceGetToast;
        toastInstanceResolved = true;
        try {
            Method m = clazz.getMethod("getToast");
            m.setAccessible(true);
            toastInstanceGetToast = m;
        } catch (NoSuchMethodException ignored) {
            toastInstanceGetToast = null;
        }
        return toastInstanceGetToast;
    }

    private static boolean isFishCaughtToast(Toast toast) {
        return toast.getClass().getName().contains("FishCaughtToast");
    }

    private static Field[] collectionFields(Class<?> clazz) {
        if (collectionFields != null) return collectionFields;
        java.util.ArrayList<Field> found = new java.util.ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            if (Collection.class.isAssignableFrom(type)
                    || java.util.Deque.class.isAssignableFrom(type)
                    || java.util.List.class.isAssignableFrom(type)
                    || java.util.Queue.class.isAssignableFrom(type)) {
                field.setAccessible(true);
                found.add(field);
            }
        }
        collectionFields = found.toArray(Field[]::new);
        return collectionFields;
    }
}
