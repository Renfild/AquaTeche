package net.aquatech.machines.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Достаёт ресурсы тем же способом, каким их ловит сама удочка: через FishingLootHandler мода aquatech_ui. Мода нет в
 * зависимостях при компиляции, поэтому вызов идёт через reflection; если мода нет или сигнатура другая, возвращается
 * пустой список и вызывающий код берёт запасную таблицу.
 */
public final class RodLootBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("aquatech_machines/rod_loot");
    private static final String HANDLER = "net.aquatech.ui.fishing.FishingLootHandler";

    private static Method rollResources;
    private static boolean unavailable;

    private RodLootBridge() {
    }

    /** Ресурсные стеки, которые выдала бы удочка за один улов (1–3 штуки): без рыбы, приманок и множителя улова. */
    public static List<ItemStack> rollLikeRod(ItemStack rod, RandomSource random) {
        if (unavailable || rod == null || rod.isEmpty()) {
            return List.of();
        }
        try {
            if (rollResources == null) {
                rollResources = Class.forName(HANDLER).getMethod("rollResourcesOnly", ItemStack.class, RandomSource.class);
            }
            Object result = rollResources.invoke(null, rod, random);
            List<ItemStack> out = new ArrayList<>();
            if (result instanceof List<?> list) {
                for (Object entry : list) {
                    if (entry instanceof ItemStack stack && !stack.isEmpty()) {
                        out.add(stack);
                    }
                }
            }
            return out;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
            unavailable = true;
            LOGGER.warn("Rod loot bridge unavailable, using the built-in resource table: {}", error.toString());
            return List.of();
        }
    }
}
