package net.aquatech.ui.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds AquaTech fishing rod requirements to all ores and strips legacy IU vein descriptions.
 */
@Mod.EventBusSubscriber(modid = "aquatech_ui", value = Dist.CLIENT)
public final class FishingOreTooltips {
    private static final Map<String, RodHint> BY_ID = new HashMap<>();
    private static final Map<Item, RodHint> BY_VANILLA = new HashMap<>();

    static {
        // Vanilla Ores & Resources
        hintVanilla(Items.COPPER_ORE, RodHint.HUMBLE);
        hintVanilla(Items.DEEPSLATE_COPPER_ORE, RodHint.HUMBLE);
        hintVanilla(Items.RAW_COPPER, RodHint.HUMBLE);
        hintVanilla(Items.RAW_COPPER_BLOCK, RodHint.HUMBLE);

        hintVanilla(Items.COAL_ORE, RodHint.BAMBOO);
        hintVanilla(Items.DEEPSLATE_COAL_ORE, RodHint.BAMBOO);
        hintVanilla(Items.COAL, RodHint.BAMBOO);

        hintVanilla(Items.IRON_ORE, RodHint.GOOD_OLD);
        hintVanilla(Items.DEEPSLATE_IRON_ORE, RodHint.GOOD_OLD);
        hintVanilla(Items.RAW_IRON, RodHint.GOOD_OLD);
        hintVanilla(Items.RAW_IRON_BLOCK, RodHint.GOOD_OLD);
        hintVanilla(Items.REDSTONE_ORE, RodHint.GOOD_OLD);
        hintVanilla(Items.DEEPSLATE_REDSTONE_ORE, RodHint.GOOD_OLD);
        hintVanilla(Items.REDSTONE, RodHint.GOOD_OLD);

        hintVanilla(Items.GOLD_ORE, RodHint.STARCATCHER);
        hintVanilla(Items.DEEPSLATE_GOLD_ORE, RodHint.STARCATCHER);
        hintVanilla(Items.RAW_GOLD, RodHint.STARCATCHER);
        hintVanilla(Items.RAW_GOLD_BLOCK, RodHint.STARCATCHER);
        hintVanilla(Items.LAPIS_ORE, RodHint.STARCATCHER);
        hintVanilla(Items.DEEPSLATE_LAPIS_ORE, RodHint.STARCATCHER);
        hintVanilla(Items.LAPIS_LAZULI, RodHint.STARCATCHER);
        hintVanilla(Items.EMERALD_ORE, RodHint.STARCATCHER);
        hintVanilla(Items.DEEPSLATE_EMERALD_ORE, RodHint.STARCATCHER);
        hintVanilla(Items.EMERALD, RodHint.STARCATCHER);

        hintVanilla(Items.DIAMOND_ORE, RodHint.OBSIDIAN);
        hintVanilla(Items.DEEPSLATE_DIAMOND_ORE, RodHint.OBSIDIAN);
        hintVanilla(Items.DIAMOND, RodHint.OBSIDIAN);

        hintVanilla(Items.PRISMARINE_SHARD, RodHint.LUSH);
        hintVanilla(Items.PRISMARINE_CRYSTALS, RodHint.LUSH);
        hintVanilla(Items.HEART_OF_THE_SEA, RodHint.LUSH);

        hintVanilla(Items.NETHER_QUARTZ_ORE, RodHint.MAGMA);
        hintVanilla(Items.QUARTZ, RodHint.MAGMA);
        hintVanilla(Items.ANCIENT_DEBRIS, RodHint.MAGMA);
        hintVanilla(Items.NETHERITE_SCRAP, RodHint.MAGMA);

        // Industrial Upgrade Ores & Resources
        hintIu("classicore/tin", RodHint.HUMBLE);
        hintIu("raw_latex", RodHint.HUMBLE);

        hintIu("synthetic_rubber", RodHint.BAMBOO);
        hintIu("sapling/rubber_sapling", RodHint.NATURALIST);
        hintIu("blockresource/untreated_peat", RodHint.NATURALIST);
        hintIu("blockresource/peat", RodHint.NATURALIST);

        hintIu("baseore2/strontium", RodHint.GOOD_OLD);
        hintIu("baseore2/yttrium", RodHint.GOOD_OLD);
        hintIu("baseore2/thallium", RodHint.GOOD_OLD);

        hintIu("baseore/spinel", RodHint.NATURALIST);
        hintIu("baseore2/barium", RodHint.NATURALIST);
        hintIu("baseore2/polonium", RodHint.SLIMED);

        hintIu("baseore/aluminium", RodHint.ICEBORN);
        hintIu("baseore/silver", RodHint.ICEBORN);
        hintIu("baseore/zinc", RodHint.ICEBORN);
        hintIu("itemcoolupgrade/azote", RodHint.ICEBORN);

        hintIu("baseore/tungsten", RodHint.STARCATCHER);
        hintIu("baseore/chromium", RodHint.STARCATCHER);
        hintIu("preciousgem/sapphire_gem", RodHint.STARCATCHER);
        hintIu("preciousgem/topaz_gem", RodHint.STARCATCHER);
        hintIu("blockpreciousore/sapphire_ore", RodHint.STARCATCHER);
        hintIu("blockpreciousore/topaz_ore", RodHint.STARCATCHER);

        hintIu("mineral/crystal", RodHint.AZURE);

        hintIu("baseore/titanium", RodHint.SHARKTOOTH);
        hintIu("baseore/cobalt", RodHint.SHARKTOOTH);
        hintIu("baseore/manganese", RodHint.SHARKTOOTH);
        hintIu("baseore/nickel", RodHint.SHARKTOOTH);

        hintIu("alloyingot/stainless_steel", RodHint.OBSIDIAN);
        hintIu("baseore/platinum", RodHint.LUSH);

        hintIu("crushed/uranium", RodHint.MAGMA);
        hintIu("bucket/sour_light_oil", RodHint.MAGMA);
        hintIu("alloyingot/inconel", RodHint.MAGMA);

        hintIu("baseore/iridium", RodHint.ALPHA);
        hintIu("baseore1/osmium", RodHint.ALPHA);
        hintIu("alloyingot/osmiridium", RodHint.ALPHA);
        hintIu("asteroidore/asteroid_adamantium_ore", RodHint.ALPHA);
    }

    private FishingOreTooltips() {
    }

    private static void hintVanilla(Item item, RodHint hint) {
        BY_VANILLA.put(item, hint);
    }

    private static void hintIu(String path, RodHint hint) {
        BY_ID.put("industrialupgrade:" + path, hint);
    }

    private static boolean isOreItem(ResourceLocation id, Item item) {
        if (BY_VANILLA.containsKey(item)) return true;
        if (id == null) return false;
        String full = id.toString().toLowerCase();
        String path = id.getPath().toLowerCase();

        if (BY_ID.containsKey(full)) return true;

        // Exclude non-ore categories: tools, armors, buckets, machines, plates, ingots, nuggets, hammers
        if (path.contains("hammer") || path.contains("sword") || path.contains("pickaxe") || path.contains("axe")
                || path.contains("shovel") || path.contains("hoe") || path.contains("helmet") || path.contains("chestplate")
                || path.contains("leggings") || path.contains("boots") || path.contains("armor") || path.contains("bucket")
                || path.contains("pipe") || path.contains("cable") || path.contains("machine") || path.contains("generator")
                || path.contains("plate") || path.contains("ingot") || path.contains("nugget")) {
            return false;
        }

        // Must be an explicit ore, raw ore, gem, or mineral
        return path.contains("ore") || path.contains("raw_") || path.contains("gem") || path.contains("crystal") || path.contains("shard");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // 1. Remove ALL old IU vein/location descriptions, bracketed ore lists, and Shift prompts
        event.getToolTip().removeIf(component -> {
            String text = component.getString().toLowerCase();
            return text.contains("жила") || text.contains("жилах") || text.contains("жиле")
                    || text.contains("жилы") || text.contains("жилу") || text.contains("жил")
                    || text.contains("ищите") || text.contains("камни")
                    || text.contains("добывается") || text.contains("удерживайте")
                    || text.contains("shift") || text.contains("подробной информации")
                    || text.contains("можно найти") || text.contains("генерируется")
                    || (text.startsWith("[") && text.endsWith("]"));
        });

        // 2. Resolve AquaTech fishing rod requirement line ONLY for items with explicit rod hints
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return;

        RodHint hint = BY_VANILLA.get(stack.getItem());
        if (hint == null) {
            hint = BY_ID.get(id.toString());
        }

        if (hint == null) return;

        event.getToolTip().add(Component.empty());
        event.getToolTip().add(Component.literal("AquaTech  ·  Рыбалка")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        event.getToolTip().add(Component.literal("Ловится  удочкой:  ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(hint.rodName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        event.getToolTip().add(Component.literal(hint.tierLine)
                .withStyle(ChatFormatting.GRAY));
    }

    private enum RodHint {
        HUMBLE("Удочка  скромности  (Humble Rod)", "С  1-й  ресурсной  удочки  и  выше"),
        BAMBOO("Бамбуковая  удочка  (Bamboo Rod)", "С  2-й  ресурсной  удочки  и  выше"),
        GOOD_OLD("Старая  добрая  удочка  (Good Old Rod)", "С  3-й  ресурсной  удочки  (Железо  /  Олово  /  Редстоун)"),
        NATURALIST("Удочка  натуралиста  (Naturalist Rod)", "С  4-й  ресурсной  удочки  (Шпинель  /  Торф  /  Барий)"),
        SLIMED("Слизневая  удочка  (Slimed Rod)", "С  5-й  ресурсной  удочки  (Полоний  /  Слизь)"),
        ICEBORN("Удочка  ледянорожденного  (Iceborn Rod)", "С  6-й  ресурсной  удочки  (Алюминий  /  Серебро  /  Цинк)"),
        STARCATCHER("Удочка  ловца  звезд  (StarCatcher Rod)", "С  7-й  ресурсной  удочки  (Золото  /  Вольфрам  /  Хром)"),
        AZURE("Лазурная  удочка  (Azure Crystal Rod)", "С  8-й  ресурсной  удочки  (Топаз  /  Кристаллы)"),
        SHARKTOOTH("Удочка  из  акульего  зуба  (Sharktooth Rod)", "С  9-й  ресурсной  удочки  (Титан  /  Кобальт  /  Никель)"),
        OBSIDIAN("Обсидиановая  удочка  (Obsidian Rod)", "С  10-й  ресурсной  удочки  (Алмазы  /  Сталь)"),
        LUSH("Удочка  из  светящихся  ягод  (Lush Glowberry Rod)", "С  11-й  ресурсной  удочки  (Платина  /  Сердце  моря)"),
        MAGMA("Магмовая  удочка  (Magmaforged Rod)", "С  12-й  ресурсной  удочки  (Кварц  /  Уран  /  Незерит  /  Нефть)"),
        ALPHA("Альфа  удочка  (Alpha Rod)", "С  13-й  ресурсной  удочки  (Иридий  /  Осмий  /  Адамантий)");

        final String rodName;
        final String tierLine;

        RodHint(String rodName, String tierLine) {
            this.rodName = rodName;
            this.tierLine = tierLine;
        }
    }
}


