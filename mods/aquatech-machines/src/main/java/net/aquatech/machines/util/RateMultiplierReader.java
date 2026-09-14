package net.aquatech.machines.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Читает множитель улова (рейт) с удочки StarCatcher: bait/bobber/hook-слоты через SC-data,
 * AquaPinnedRate или TackleInventory в NBT. Копия логики FishingLootHandler.readRateMultiplier.
 */
public final class RateMultiplierReader {

    private static final String PINNED_RATE = "AquaPinnedRate";
    private static boolean probed;
    private static Method getMethod;
    private static Object[] holders;

    private RateMultiplierReader() {
    }

    public static int read(ItemStack rodStack) {
        if (rodStack == null || rodStack.isEmpty()) return 1;
        int best = 1;
        try {
            ensureProbed();
            if (getMethod != null && holders != null) {
                for (Object holder : holders) {
                    if (holder == null) continue;
                    Object container = getMethod.invoke(null, rodStack, holder);
                    if (container != null) {
                        ItemStack attached = (ItemStack) container.getClass().getMethod("stack").invoke(container);
                        best = Math.max(best, multiplierOf(attached));
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        if (rodStack.hasTag()) {
            CompoundTag tag = rodStack.getTag();
            if (tag != null) {
                if (tag.contains(PINNED_RATE)) {
                    ResourceLocation id = ResourceLocation.tryParse(tag.getString(PINNED_RATE));
                    if (id != null) {
                        best = Math.max(best, multiplierOfId(id));
                    }
                }
                if (tag.contains("TackleInventory")) {
                    try {
                        CompoundTag tackle = tag.getCompound("TackleInventory");
                        ListTag itemsList = tackle.getList("Items", 10);
                        for (int i = 0; i < itemsList.size(); i++) {
                            ItemStack item = ItemStack.of(itemsList.getCompound(i));
                            best = Math.max(best, multiplierOf(item));
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
        return Math.max(1, best);
    }

    private static int multiplierOf(ItemStack bait) {
        if (bait == null || bait.isEmpty()) return 1;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(bait.getItem());
        return multiplierOfId(id);
    }

    private static int multiplierOfId(ResourceLocation id) {
        if (id == null) return 1;
        String path = id.getPath();
        return switch (path) {
            case "rate_x2" -> 2;
            case "rate_x4" -> 4;
            case "rate_x8" -> 8;
            case "rate_x16" -> 16;
            case "rate_x32" -> 32;
            case "rate_x64" -> 64;
            default -> {
                if (path.contains("x64")) yield 64;
                if (path.contains("x32")) yield 32;
                if (path.contains("x16")) yield 16;
                if (path.contains("x8")) yield 8;
                if (path.contains("x4")) yield 4;
                if (path.contains("x2")) yield 2;
                yield 1;
            }
        };
    }

    private static void ensureProbed() {
        if (probed) return;
        probed = true;
        try {
            Class<?> sc = Class.forName("com.wdiscute.starcatcher.io.SCDataComponents");
            getMethod = sc.getMethod("get", ItemStack.class, java.util.function.Supplier.class);
            List<Object> list = new ArrayList<>();
            for (String fName : new String[]{"BAIT", "BOBBER", "HOOK"}) {
                try {
                    Field f = sc.getField(fName);
                    list.add(f.get(null));
                } catch (Throwable ignored) {
                }
            }
            holders = list.toArray();
        } catch (Throwable ignored) {
            getMethod = null;
            holders = null;
        }
    }
}
