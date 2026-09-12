package net.aquatech.machines.block.entity;

import net.aquatech.machines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * Экстрактор: перерабатывает ресурсы. Вход [0], выход [1].
 * 1 руда/кристалл → 2–3 производных ресурса (быстрая добыча материалов под МЭ).
 */
public class ExtractorBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private static final Map<String, String> RECIPES = Map.ofEntries(
            Map.entry("ae2:certus_quartz_crystal", "ae2:quartz_dust"),
            Map.entry("minecraft:quartz", "ae2:quartz_dust"),
            Map.entry("minecraft:sand", "minecraft:glass"),
            Map.entry("botania:manasteel_ingot", "botania:elementium_ingot"),
            Map.entry("industrialupgrade:baseore/spinel", "industrialupgrade:preciousgem/sapphire_gem"),
            Map.entry("industrialupgrade:baseore2/strontium", "industrialupgrade:classicore/tin"),
            Map.entry("industrialupgrade:baseore2/barium", "industrialupgrade:itemingots/copper_ingot"),
            Map.entry("minecraft:lapis_lazuli", "botania:mana_pearl"),
            Map.entry("minecraft:prismarine_crystals", "minecraft:heart_of_the_sea")
    );

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTRACTOR.get(), pos, state, 2, 100000, 256, 140, 45);
    }

    @Override
    protected net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new net.aquatech.machines.inventory.ExtractorMenu(id, inv, this);
    }

    @Override
    protected int outputSlots() {
        return 1;
    }

    private String outputId() {
        ItemStack in = items.getStackInSlot(SLOT_INPUT);
        if (in.isEmpty()) return null;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(in.getItem());
        return RECIPES.get(id.toString());
    }

    @Override
    protected boolean hasWork() {
        String out = outputId();
        if (out == null) return false;
        ItemStack result = resultStack(out);
        ItemStack cur = items.getStackInSlot(SLOT_OUTPUT);
        return cur.isEmpty() || (ItemStack.isSameItemSameTags(cur, result)
                && cur.getCount() + result.getCount() <= cur.getMaxStackSize());
    }

    @Override
    protected void craftOnce() {
        String out = outputId();
        if (out == null) return;
        ItemStack result = resultStack(out);
        ItemStack rest = items.insertItem(SLOT_OUTPUT, result, false);
        if (rest.isEmpty()) {
            items.extractItem(SLOT_INPUT, 1, false);
        }
    }

    private ItemStack resultStack(String id) {
        ResourceLocation loc = new ResourceLocation(id);
        Item item = ForgeRegistries.ITEMS.getValue(loc);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        // экстракция даёт двойной выход — смысл машины в скорости добычи материалов
        return new ItemStack(item, 2);
    }
}
