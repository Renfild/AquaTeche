package net.aquatech.ui.fishing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/**
 * Чтение и запись вес-компонента Starcatcher прямо в NBT предмета. Starcatcher хранит компонент
 * {@code caught_fish_info} через NeoBackports под ключом {@code starcatcher:caught_fish_info} в корне тега предмета.
 */
public final class FishCatchInfo {

    public static final String KEY = "starcatcher:caught_fish_info";

    public record Info(int grams, int cm, float topPercent, String rarity, boolean golden) {
    }

    private FishCatchInfo() {
    }

    public static String speciesId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }

    /** Имя предмета для серверных сообщений: без разметки редкости Starcatcher. */
    public static String displayName(ItemStack stack) {
        return FishNames.strip(stack.getHoverName().getString());
    }

    /** Данные улова из предмета; null, если у предмета их нет (или вес не положительный). */
    public static Info read(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return null;
        }
        CompoundTag root = stack.getTag();
        if (!root.contains(KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag tag = root.getCompound(KEY);
        int grams = tag.getInt("weight");
        if (grams <= 0) {
            return null;
        }
        return new Info(grams, tag.getInt("size"), tag.getFloat("percentile"),
                tag.getString("rarity"), tag.getBoolean("golden"));
    }

    /**
     * Данные улова: уже стоящие на предмете или, если это рыба известного вида без них, свежий ролл, записанный
     * в предмет. Для не-рыбы (руды, доски и т.п.) возвращает null.
     */
    public static Info ensure(ItemStack stack, RandomSource random) {
        Info existing = read(stack);
        if (existing != null) {
            return existing;
        }
        FishWeight.Species species = FishWeight.species(speciesId(stack));
        if (species == null) {
            return null;
        }
        float top = FishWeight.MIN_TOP_PERCENT
                + random.nextFloat() * (FishWeight.MAX_TOP_PERCENT - FishWeight.MIN_TOP_PERCENT);
        FishWeight.Roll roll = FishWeight.roll(species, top);
        CompoundTag tag = new CompoundTag();
        tag.putInt("size", roll.cm());
        tag.putInt("weight", roll.grams());
        tag.putFloat("percentile", roll.topPercent());
        tag.putString("rarity", species.rarity());
        tag.putBoolean("golden", false);
        stack.getOrCreateTag().put(KEY, tag);
        return new Info(roll.grams(), roll.cm(), roll.topPercent(), species.rarity(), false);
    }
}
