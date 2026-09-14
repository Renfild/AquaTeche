package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Экстрактор: перерабатывает сырье/руду/кристаллы в удвоенную готовую продукцию.
 * Слоты: [0] Вход, [1] Выход, [2] Скорость, [3] Энергоэффективность, [4] Батарея.
 */
public class ExtractorBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_SPEED = 2;
    public static final int SLOT_EFF = 3;
    public static final int SLOT_BATTERY = 4;

    public record RecipeEntry(String inputId, String outputId, int count, String label) {}

    public static final List<RecipeEntry> RECIPE_LIST = List.of(
            new RecipeEntry("ae2:certus_quartz_crystal", "ae2:quartz_dust", 2, "Истинный кварц → 2x Кварцевая пыль"),
            new RecipeEntry("minecraft:quartz", "ae2:quartz_dust", 2, "Кварц Нижнего мира → 2x Кварцевая пыль"),
            new RecipeEntry("minecraft:sand", "minecraft:glass", 2, "Песок → 2x Стекло"),
            new RecipeEntry("minecraft:cobblestone", "minecraft:gravel", 1, "Булыжник → Гравий"),
            new RecipeEntry("minecraft:gravel", "minecraft:sand", 1, "Гравий → Песок"),
            new RecipeEntry("botania:manasteel_ingot", "botania:elementium_ingot", 2, "Слиток манастали → 2x Слиток элементiumа"),
            new RecipeEntry("industrialupgrade:baseore/spinel", "industrialupgrade:preciousgem/sapphire_gem", 2, "Шпинель → 2x Сапфир"),
            new RecipeEntry("industrialupgrade:baseore2/strontium", "industrialupgrade:classicore/tin", 2, "Стронций → 2x Олово"),
            new RecipeEntry("industrialupgrade:baseore2/barium", "industrialupgrade:itemingots/copper_ingot", 2, "Барий → 2x Медь"),
            new RecipeEntry("minecraft:lapis_lazuli", "botania:mana_pearl", 2, "Лазурит → 2x Жемчужина маны"),
            new RecipeEntry("minecraft:prismarine_crystals", "minecraft:heart_of_the_sea", 2, "Осколок призмарина → 2x Сердце моря"),
            new RecipeEntry("minecraft:raw_iron", "minecraft:iron_ingot", 2, "Сырое железо → 2x Слиток железа"),
            new RecipeEntry("minecraft:raw_copper", "minecraft:copper_ingot", 2, "Сырая медь → 2x Слиток меди"),
            new RecipeEntry("minecraft:raw_gold", "minecraft:gold_ingot", 2, "Сырое золото → 2x Слиток золота"),
            new RecipeEntry("minecraft:iron_ore", "minecraft:iron_ingot", 2, "Железная руда → 2x Слиток железа"),
            new RecipeEntry("minecraft:copper_ore", "minecraft:copper_ingot", 2, "Медная руда → 2x Слиток меди"),
            new RecipeEntry("minecraft:gold_ore", "minecraft:gold_ingot", 2, "Золотая руда → 2x Слиток золота"),
            new RecipeEntry("minecraft:deepslate_iron_ore", "minecraft:iron_ingot", 2, "Глубинная железная руда → 2x Слиток железа"),
            new RecipeEntry("minecraft:deepslate_copper_ore", "minecraft:copper_ingot", 2, "Глубинная медная руда → 2x Слиток меди"),
            new RecipeEntry("minecraft:deepslate_gold_ore", "minecraft:gold_ingot", 2, "Глубинная золотая руда → 2x Слиток золота"),
            new RecipeEntry("minecraft:diamond_ore", "minecraft:diamond", 2, "Алмазная руда → 2x Алмаз"),
            new RecipeEntry("minecraft:deepslate_diamond_ore", "minecraft:diamond", 2, "Глубинная алмазная руда → 2x Алмаз"),
            new RecipeEntry("minecraft:emerald_ore", "minecraft:emerald", 2, "Изумрудная руда → 2x Изумруд"),
            new RecipeEntry("minecraft:deepslate_emerald_ore", "minecraft:emerald", 2, "Глубинная изумрудная руда → 2x Изумруд"),
            new RecipeEntry("minecraft:redstone_ore", "minecraft:redstone", 4, "Красная руда → 4x Редстоун"),
            new RecipeEntry("minecraft:deepslate_redstone_ore", "minecraft:redstone", 4, "Глубинная красная руда → 4x Редстоун"),
            new RecipeEntry("minecraft:coal_ore", "minecraft:coal", 2, "Угольная руда → 2x Уголь"),
            new RecipeEntry("minecraft:deepslate_coal_ore", "minecraft:coal", 2, "Глубинная угольная руда → 2x Уголь")
    );

    private static final Map<String, RecipeEntry> RECIPE_MAP = new HashMap<>();
    static {
        for (RecipeEntry r : RECIPE_LIST) {
            RECIPE_MAP.put(r.inputId(), r);
        }
    }

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTRACTOR.get(), pos, state, 5, 200000, 1024, 140, 45, 200000);
        defineSlots(SLOT_OUTPUT, SLOT_OUTPUT, SLOT_SPEED, SLOT_EFF, SLOT_BATTERY);
    }

    @Override
    protected net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.ExtractorMenu(id, inv, this);
    }

    @Override
    protected int outputSlots() {
        return 1;
    }

    private RecipeEntry currentRecipe() {
        ItemStack in = items.getStackInSlot(SLOT_INPUT);
        if (in.isEmpty()) return null;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(in.getItem());
        return RECIPE_MAP.get(id.toString());
    }

    @Override
    protected boolean hasWork() {
        RecipeEntry recipe = currentRecipe();
        if (recipe == null) return false;
        ItemStack result = resultStack(recipe);
        if (result.isEmpty()) return false;
        ItemStack cur = items.getStackInSlot(SLOT_OUTPUT);
        return cur.isEmpty() || (ItemStack.isSameItemSameTags(cur, result)
                && cur.getCount() + result.getCount() <= cur.getMaxStackSize());
    }

    @Override
    protected void craftOnce() {
        RecipeEntry recipe = currentRecipe();
        if (recipe == null) return;
        ItemStack result = resultStack(recipe);
        if (result.isEmpty()) return;
        ItemStack rest = items.insertItem(SLOT_OUTPUT, result, false);
        if (rest.isEmpty()) {
            items.extractItem(SLOT_INPUT, 1, false);
        }
    }

    private ItemStack resultStack(RecipeEntry recipe) {
        ResourceLocation loc = new ResourceLocation(recipe.outputId());
        Item item = ForgeRegistries.ITEMS.getValue(loc);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item, recipe.count());
    }
}
