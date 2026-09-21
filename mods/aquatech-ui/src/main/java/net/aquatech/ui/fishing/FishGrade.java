package net.aquatech.ui.fishing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Грейд пойманной рыбы. Хранится в NBT стека, множитель применяет скупщик
 * (аqualumen FishShopConfig читает тот же тег "AquaGrade").
 */
public final class FishGrade {

    public static final String TAG = "AquaGrade";

    public static final int NONE = 0;
    public static final int SILVER = 1;
    public static final int GOLD = 2;
    public static final int RAINBOW = 3;

    private static final double[] PRICE_MULT = {1.0, 1.25, 1.6, 3.0};
    private static final String[] NAMES = {"", "Серебро", "Золото", "Радужная"};
    private static final String[] COLORS = {"", "§7", "§6", "§d"};

    private FishGrade() {
    }

    public static int of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return NONE;
        CompoundTag tag = stack.getTag();
        if (tag == null) return NONE;
        int grade = tag.getInt(TAG);
        return grade >= SILVER && grade <= RAINBOW ? grade : NONE;
    }

    public static void set(ItemStack stack, int grade) {
        if (stack == null || stack.isEmpty() || grade <= NONE || grade > RAINBOW) return;
        stack.getOrCreateTag().putInt(TAG, grade);
    }

    public static double priceMultiplier(int grade) {
        return grade >= SILVER && grade <= RAINBOW ? PRICE_MULT[grade] : 1.0;
    }

    public static String name(int grade) {
        return grade >= SILVER && grade <= RAINBOW ? NAMES[grade] : "";
    }

    public static String color(int grade) {
        return grade >= SILVER && grade <= RAINBOW ? COLORS[grade] : "";
    }

    /** Бит для маски грейдов в атласе: silver=1, gold=2, rainbow=4. */
    public static int maskBit(int grade) {
        return grade >= SILVER && grade <= RAINBOW ? 1 << (grade - 1) : 0;
    }
}
