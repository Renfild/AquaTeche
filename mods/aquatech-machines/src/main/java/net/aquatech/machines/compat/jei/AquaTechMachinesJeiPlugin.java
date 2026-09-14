package net.aquatech.machines.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.aquatech.machines.block.entity.ExcavatorBlockEntity;
import net.aquatech.machines.block.entity.ExtractorBlockEntity;
import net.aquatech.machines.client.gui.ExcavatorScreen;
import net.aquatech.machines.client.gui.ExtractorScreen;
import net.aquatech.machines.client.gui.FisherScreen;
import net.aquatech.machines.compat.jei.category.ExcavatorRecipeCategory;
import net.aquatech.machines.compat.jei.category.ExtractorRecipeCategory;
import net.aquatech.machines.compat.jei.category.FisherRecipeCategory;
import net.aquatech.machines.compat.jei.recipe.ExcavatorJeiRecipe;
import net.aquatech.machines.compat.jei.recipe.ExtractorJeiRecipe;
import net.aquatech.machines.compat.jei.recipe.FisherJeiRecipe;
import net.aquatech.machines.registry.ModBlocks;
import net.aquatech.machines.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class AquaTechMachinesJeiPlugin implements IModPlugin {

    public static final ResourceLocation UID = new ResourceLocation("aquatech_machines", "jei_plugin");

    public static final RecipeType<ExtractorJeiRecipe> EXTRACTOR_TYPE =
            RecipeType.create("aquatech_machines", "extractor", ExtractorJeiRecipe.class);

    public static final RecipeType<ExcavatorJeiRecipe> EXCAVATOR_TYPE =
            RecipeType.create("aquatech_machines", "excavator", ExcavatorJeiRecipe.class);

    public static final RecipeType<FisherJeiRecipe> FISHER_TYPE =
            RecipeType.create("aquatech_machines", "fisher", FisherJeiRecipe.class);

    public static final RecipeType<net.aquatech.machines.compat.jei.recipe.SynthesizerJeiRecipe> SYNTHESIZER_TYPE =
            RecipeType.create("aquatech_machines", "synthesizer", net.aquatech.machines.compat.jei.recipe.SynthesizerJeiRecipe.class);

    public static final RecipeType<net.aquatech.machines.compat.jei.recipe.CentrifugeJeiRecipe> CENTRIFUGE_TYPE =
            RecipeType.create("aquatech_machines", "centrifuge", net.aquatech.machines.compat.jei.recipe.CentrifugeJeiRecipe.class);

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    public static boolean showRecipes(RecipeType<?> type) {
        if (runtime != null) {
            runtime.getRecipesGui().showTypes(List.of(type));
            return true;
        }
        return false;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ExtractorRecipeCategory(helper),
                new ExcavatorRecipeCategory(helper),
                new FisherRecipeCategory(helper),
                new net.aquatech.machines.compat.jei.category.SynthesizerRecipeCategory(helper),
                new net.aquatech.machines.compat.jei.category.CentrifugeRecipeCategory(helper)
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.EXTRACTOR.get()), EXTRACTOR_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.EXCAVATOR.get()), EXCAVATOR_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FISHER.get()), FISHER_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SYNTHESIZER.get()), SYNTHESIZER_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CENTRIFUGE.get()), CENTRIFUGE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Клик по прогресс-бару (24x17) открывает соответствующую категорию JEI
        registration.addRecipeClickArea(ExtractorScreen.class, 74, 36, 24, 17, EXTRACTOR_TYPE);
        registration.addRecipeClickArea(ExcavatorScreen.class, 48, 36, 24, 17, EXCAVATOR_TYPE);
        registration.addRecipeClickArea(FisherScreen.class, 74, 36, 24, 17, FISHER_TYPE);
        registration.addRecipeClickArea(net.aquatech.machines.client.gui.SynthesizerScreen.class, 92, 37, 24, 17, SYNTHESIZER_TYPE);
        registration.addRecipeClickArea(net.aquatech.machines.client.gui.CentrifugeScreen.class, 65, 37, 24, 17, CENTRIFUGE_TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(EXTRACTOR_TYPE, buildExtractorRecipes());
        registration.addRecipes(EXCAVATOR_TYPE, buildExcavatorRecipes());
        registration.addRecipes(FISHER_TYPE, buildFisherRecipes());
        registration.addRecipes(SYNTHESIZER_TYPE, buildSynthesizerRecipes());
        registration.addRecipes(CENTRIFUGE_TYPE, buildCentrifugeRecipes());
    }

    private List<ExtractorJeiRecipe> buildExtractorRecipes() {
        List<ExtractorJeiRecipe> list = new ArrayList<>();
        for (ExtractorBlockEntity.RecipeEntry r : ExtractorBlockEntity.RECIPE_LIST) {
            ItemStack in = item(r.inputId(), 1);
            ItemStack out = item(r.outputId(), r.count());
            if (!in.isEmpty() && !out.isEmpty()) {
                list.add(new ExtractorJeiRecipe(in, out, r.label()));
            }
        }
        return list;
    }

    private List<ExcavatorJeiRecipe> buildExcavatorRecipes() {
        List<ExcavatorJeiRecipe> list = new ArrayList<>();
        int totalWeight = 0;
        for (ExcavatorBlockEntity.Entry e : ExcavatorBlockEntity.POOL) {
            totalWeight += e.weight();
        }
        for (ExcavatorBlockEntity.Entry e : ExcavatorBlockEntity.POOL) {
            ItemStack stack = item(e.id(), e.max());
            if (!stack.isEmpty()) {
                double pct = (e.weight() * 100.0) / (double) totalWeight;
                String range = (e.min() == e.max()) ? (e.min() + " шт") : (e.min() + "-" + e.max() + " шт");
                list.add(new ExcavatorJeiRecipe(stack, e.weight(), pct, e.label(), range));
            }
        }
        return list;
    }

    private List<FisherJeiRecipe> buildFisherRecipes() {
        List<FisherJeiRecipe> list = new ArrayList<>();

        record RodDef(String id, int tier, String name) {}
        List<RodDef> rods = List.of(
                new RodDef("starcatcher:bamboo_rod", 1, "Бамбуковая удочка"),
                new RodDef("starcatcher:humble_rod", 2, "Скромная удочка"),
                new RodDef("starcatcher:good_old_rod", 3, "Старая добрая удочка"),
                new RodDef("starcatcher:boner_rod", 3, "Костяная удочка"),
                new RodDef("starcatcher:naturalist_rod", 4, "Удочка натуралиста"),
                new RodDef("starcatcher:slimed_rod", 5, "Слизневая удочка"),
                new RodDef("starcatcher:iceborn_rod", 6, "Ледяная удочка"),
                new RodDef("starcatcher:starcatcher_rod", 7, "Ловец Звёзд"),
                new RodDef("starcatcher:azure_crystal_rod", 8, "Лазурный кристалл"),
                new RodDef("starcatcher:sky_rod", 8, "Небесная удочка (AE2)"),
                new RodDef("starcatcher:sharktooth_rod", 9, "Акулий клык"),
                new RodDef("starcatcher:obsidian_rod", 10, "Обсидиановая удочка"),
                new RodDef("starcatcher:lush_glowberry_rod", 11, "Светящаяся ягода"),
                new RodDef("starcatcher:magmaforged_rod", 12, "Магматическая удочка"),
                new RodDef("starcatcher:alpha_rod", 13, "Альфа-удочка")
        );

        for (RodDef r : rods) {
            ItemStack rodStack = item(r.id(), 1);
            if (rodStack.isEmpty()) {
                rodStack = new ItemStack(Items.FISHING_ROD);
            }

            // 1. Режим руды/материалов (без ядра)
            List<ItemStack> oreSamples = getSampleOresForTier(r.tier(), r.id());
            if (!oreSamples.isEmpty()) {
                list.add(new FisherJeiRecipe(rodStack, r.tier(), false, oreSamples, "Добыча руд (Тир " + r.tier() + ")"));
            }

            // 2. Режим рыбы (с ядром)
            List<ItemStack> fishSamples = getSampleFishForTier(r.tier());
            if (!fishSamples.isEmpty()) {
                list.add(new FisherJeiRecipe(rodStack, r.tier(), true, fishSamples, "Ловля рыбы (Тир " + r.tier() + ")"));
            }
        }

        return list;
    }

    private static List<ItemStack> getSampleOresForTier(int tier, String rodId) {
        List<ItemStack> items = new ArrayList<>();
        if ("starcatcher:sky_rod".equals(rodId)) {
            addIfValid(items, "ae2:certus_quartz_crystal");
            addIfValid(items, "ae2:charged_certus_quartz_crystal");
            addIfValid(items, "ae2:sky_stone_block");
            addIfValid(items, "ae2:sky_dust");
            addIfValid(items, "ae2:fluix_crystal");
            return items;
        }
        if ("starcatcher:boner_rod".equals(rodId)) {
            items.add(new ItemStack(Items.BONE));
            items.add(new ItemStack(Items.STRING));
            items.add(new ItemStack(Items.GUNPOWDER));
            items.add(new ItemStack(Items.ENDER_PEARL));
            items.add(new ItemStack(Items.SLIME_BALL));
            return items;
        }

        items.add(new ItemStack(Items.IRON_ORE));
        items.add(new ItemStack(Items.COPPER_ORE));
        addIfValid(items, "industrialupgrade:classicore/tin");
        if (tier >= 3) {
            items.add(new ItemStack(Items.REDSTONE_ORE));
            addIfValid(items, "industrialupgrade:baseore/spinel");
        }
        if (tier >= 5) {
            addIfValid(items, "industrialupgrade:baseore/silver");
            addIfValid(items, "industrialupgrade:baseore/aluminium");
        }
        if (tier >= 7) {
            addIfValid(items, "industrialupgrade:baseore/tungsten");
            addIfValid(items, "industrialupgrade:preciousgem/sapphire_gem");
        }
        if (tier >= 9) {
            items.add(new ItemStack(Items.DIAMOND_ORE));
            addIfValid(items, "industrialupgrade:baseore/titanium");
        }
        if (tier >= 11) {
            items.add(new ItemStack(Items.ANCIENT_DEBRIS));
            addIfValid(items, "industrialupgrade:baseore/platinum");
        }
        if (tier >= 13) {
            items.add(new ItemStack(Items.NETHER_STAR));
            addIfValid(items, "industrialupgrade:baseore/iridium");
        }
        return items.subList(0, Math.min(5, items.size()));
    }

    private static List<ItemStack> getSampleFishForTier(int tier) {
        List<ItemStack> items = new ArrayList<>();
        addIfValid(items, "starcatcher:blossomfish");
        addIfValid(items, "starcatcher:pinfish");
        if (tier >= 3) {
            addIfValid(items, "starcatcher:rose_siamese_fish");
            addIfValid(items, "starcatcher:gold_goldfish");
        }
        if (tier >= 6) {
            addIfValid(items, "starcatcher:icetooth_sturgeon");
            addIfValid(items, "starcatcher:silverfin_pike");
        }
        if (tier >= 8) {
            addIfValid(items, "starcatcher:vesani");
            addIfValid(items, "starcatcher:sun_seeking_carp");
        }
        if (items.isEmpty()) {
            items.add(new ItemStack(Items.COD));
            items.add(new ItemStack(Items.SALMON));
        }
        return items.subList(0, Math.min(5, items.size()));
    }

    private static void addIfValid(List<ItemStack> list, String regName) {
        ItemStack st = item(regName, 1);
        if (!st.isEmpty()) {
            list.add(st);
        }
    }

    private List<net.aquatech.machines.compat.jei.recipe.SynthesizerJeiRecipe> buildSynthesizerRecipes() {
        List<net.aquatech.machines.compat.jei.recipe.SynthesizerJeiRecipe> list = new ArrayList<>();
        for (net.aquatech.machines.block.entity.SynthesizerBlockEntity.SynthRecipe r : net.aquatech.machines.block.entity.SynthesizerBlockEntity.RECIPES) {
            ItemStack inA = item(r.inputA(), 1);
            ItemStack inB = item(r.inputB(), 1);
            ItemStack out1 = item(r.out1(), r.count1());
            ItemStack out2 = item(r.out2(), r.count2());
            ItemStack bonus = item(r.bonus(), r.bonusCount());
            if (!inA.isEmpty() && !inB.isEmpty()) {
                list.add(new net.aquatech.machines.compat.jei.recipe.SynthesizerJeiRecipe(inA, inB, out1, out2, bonus, r.label()));
            }
        }
        return list;
    }

    private List<net.aquatech.machines.compat.jei.recipe.CentrifugeJeiRecipe> buildCentrifugeRecipes() {
        List<net.aquatech.machines.compat.jei.recipe.CentrifugeJeiRecipe> list = new ArrayList<>();
        List<ItemStack> minerals = List.of(
                new ItemStack(ModItems.SEA_SALT.get(), 2),
                new ItemStack(Items.REDSTONE, 2),
                new ItemStack(Items.GOLD_NUGGET, 3),
                new ItemStack(Items.PRISMARINE_SHARD, 1)
        );
        list.add(new net.aquatech.machines.compat.jei.recipe.CentrifugeJeiRecipe(
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.WATER_BUCKET), // Placeholder for pure distillate
                minerals,
                "Опреснение морской воды: 1000 mB → 800 mB Дистиллят + Морская соль и минералы"
        ));
        return list;
    }

    private static ItemStack item(String id, int count) {
        try {
            ResourceLocation loc = new ResourceLocation(id);
            Item it = ForgeRegistries.ITEMS.getValue(loc);
            if (it != null && it != Items.AIR) {
                return new ItemStack(it, count);
            }
        } catch (Throwable ignored) {
        }
        return ItemStack.EMPTY;
    }
}
