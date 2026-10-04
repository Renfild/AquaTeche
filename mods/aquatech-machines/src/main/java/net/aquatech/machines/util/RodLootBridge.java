package net.aquatech.machines.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
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
    private static final String ROD_TYPE = "net.aquatech.ui.fishing.AquaTechFishingRodItem$RodType";

    private static Method generate;
    private static Object fallbackRodType;
    private static boolean unavailable;

    private RodLootBridge() {
    }

    /** Все стеки, которые удочка выдала бы за один улов (1–3 штуки), без множителя улова и без сокровищ игрока. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<ItemStack> rollLikeRod(ItemStack rod, RandomSource random) {
        if (unavailable || rod == null || rod.isEmpty()) {
            return List.of();
        }
        try {
            if (generate == null) {
                Class<?> handler = Class.forName(HANDLER);
                Class<?> rodType = Class.forName(ROD_TYPE);
                generate = handler.getMethod("generateLoot", rodType, RandomSource.class, ItemStack.class,
                        Player.class, int.class, boolean.class);
                fallbackRodType = Enum.valueOf((Class<Enum>) rodType, "IRON");
            }
            Object result = generate.invoke(null, fallbackRodType, random, rod, null, 1, true);
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
