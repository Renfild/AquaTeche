package net.aquatech.ui.fishing;

import net.aquatech.ui.capability.SkillEffects;
import net.aquatech.ui.horizon.StormEvent;
import net.aquatech.ui.item.RateModItem;
import net.aquatech.ui.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "aquatech_ui")
public class FishingLootHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemFished(ItemFishedEvent event) {
        if (event.getEntity() == null || event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        FishingHook hook = event.getHookEntity();
        if (hook == null) return;

        ItemStack rodStack = ItemStack.EMPTY;
        AquaTechFishingRodItem.RodType rodType = null;
        ItemStack main = serverPlayer.getMainHandItem();
        ItemStack off = serverPlayer.getOffhandItem();
        if (FishingRodCompat.isSupportedRod(main)) {
            rodStack = main;
            rodType = FishingRodCompat.resolveRodType(main);
        } else if (FishingRodCompat.isSupportedRod(off)) {
            rodStack = off;
            rodType = FishingRodCompat.resolveRodType(off);
        }
        if (rodStack.isEmpty()) return;

        // Pin rate before SC shrinks bait; after reel put it back and spend 1 of ~10k uses.
        StarCatcherAttachments.ensureRatePersists(rodStack, false);
        final ItemStack rodRef = rodStack;
        if (serverPlayer.getServer() != null) {
            serverPlayer.getServer().execute(() -> {
                StarCatcherAttachments.ensureRatePersists(rodRef, false);
                StarCatcherAttachments.consumeRateCatch(rodRef);
            });
        }

        if (FishingRodCompat.isFishOnlyRod(rodStack)) {
            // Keep StarCatcher default fish behavior; still spend rod uses.
            RodDurability.wearOne(rodStack, serverPlayer);
            return;
        }
        if (rodType == null) return;

        // SC already ran minigame. Clear SC drops (do NOT cancel — cancel leaves ghost bob/cast).
        event.getDrops().clear();
        awardCatch(serverPlayer, rodType, rodStack, 1.0f, 70);
        // SC cleans bob after the event; force another pass next tick if attachment stuck.
        if (serverPlayer.getServer() != null) {
            serverPlayer.getServer().execute(() -> StarCatcherAttachments.forceReleaseBobber(serverPlayer));
        }
    }

    public static void awardCatch(ServerPlayer player, AquaTechFishingRodItem rodItem, ItemStack rodStack, float lootScale) {
        awardCatch(player, rodItem.getRodType(), rodStack, lootScale, 70);
    }

    public static boolean isForbiddenLoot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return false;
        String ns = id.getNamespace();
        String path = id.getPath();
        if ("avaritia".equals(ns) || "avaritia_armor".equals(ns)) return true;
        if (path.contains("inferno") || path.contains("infernal") || path.contains("crystal_core")
                || path.contains("crystal_matrix") || path.contains("crystal_pattern") || path.contains("crystal_helmet")
                || path.contains("crystal_chestplate") || path.contains("crystal_leggings") || path.contains("crystal_boots")) {
            return true;
        }
        return false;
    }

    public static void awardCatch(ServerPlayer player, AquaTechFishingRodItem.RodType type,
                                  ItemStack rodStack, float lootScale, int quality) {
        RodDurability.wearOne(rodStack, player);

        List<ItemStack> customDrops = generateLoot(type, player.getRandom(), rodStack, player);
        customDrops.removeIf(FishingLootHandler::isForbiddenLoot);

        if (lootScale < 0.99f || lootScale > 1.01f) {
            for (ItemStack drop : customDrops) {
                int scaled = Math.max(1, Math.round(drop.getCount() * lootScale));
                drop.setCount(Math.min(drop.getMaxStackSize(), scaled));
            }
        }

        // Copies for event (before inventory mutates stacks)
        List<ItemStack> awarded = new ArrayList<>(customDrops.size());
        for (ItemStack drop : customDrops) {
            awarded.add(drop.copy());
            if (!player.getInventory().add(drop)) {
                ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), drop);
                player.level().addFreshEntity(entity);
            }
        }

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.aquatech.ui.event.AquaFishCaughtEvent(player, type, awarded, lootScale, quality));

        int xpAmount = Math.round((30 + type.ordinal() * 30) * (0.85f + quality / 200f));
        player.getCapability(net.aquatech.ui.capability.AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            boolean levelUp = cap.addXp(xpAmount);
            if (levelUp) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§b[AquaTech] §a+1 Очко Навыков Океана! §8[Нажми K для меню]"), true);
            }
            net.aquatech.ui.network.NetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new net.aquatech.ui.network.S2CSyncSkillsPacket(cap));
        });
    }

    public static List<ItemStack> generateLoot(AquaTechFishingRodItem.RodType type, RandomSource random) {
        return generateLoot(type, random, ItemStack.EMPTY, null);
    }

    public static List<ItemStack> generateLoot(AquaTechFishingRodItem.RodType type, RandomSource random,
                                               ItemStack rodStack, @org.jetbrains.annotations.Nullable Player player) {
        return generateLoot(type, random, rodStack, player, 0);
    }

    /**
     * @param forceRate if &gt; 0, overrides rod-attached rate (used by AutoFisher rate slot).
     */
    public static List<ItemStack> generateLoot(AquaTechFishingRodItem.RodType type, RandomSource random,
                                               ItemStack rodStack, @org.jetbrains.annotations.Nullable Player player,
                                               int forceRate) {
        String rodId = FishingRodCompat.getRodId(rodStack);
        List<ItemStack> list = (rodId != null) ? rollStarCatcherRodLoot(rodId, random) : baseLoot(type, random);
        int rate = forceRate > 0 ? forceRate : readRateMultiplier(rodStack);
        applyRateMultiplier(list, Math.max(1, rate));

        if (player != null) {
            float mult = SkillEffects.catchMultiplier(player);
            if (mult > 1.0f && random.nextFloat() < (mult - 1.0f)) {
                List<ItemStack> extra = new ArrayList<>();
                for (ItemStack s : list) {
                    ItemStack c = s.copy();
                    c.setCount(Math.max(1, c.getCount() / 2));
                    extra.add(c);
                }
                list.addAll(extra);
            }

            float rare = SkillEffects.rareLootBonus(player);
            if (StormEvent.isActive()) rare = Math.min(0.85f, rare + 0.20f);
            if (rare > 0f && random.nextFloat() < rare) {
                list.add(rareTreasure(random));
            }

            int moon = player.level().getMoonPhase();
            if (moon == 0 && random.nextFloat() < 0.18f) {
                list.add(rareTreasure(random));
            } else if (moon == 4 && !list.isEmpty() && random.nextFloat() < 0.35f) {
                ItemStack first = list.get(0).copy();
                first.setCount(Math.min(first.getMaxStackSize(), first.getCount() + 1 + random.nextInt(2)));
                list.add(first);
            }
        }

        return list;
    }

    private static ItemStack rareTreasure(RandomSource random) {
        float r = random.nextFloat();
        if (r < 0.35f) return new ItemStack(Items.PRISMARINE_SHARD, 1 + random.nextInt(2));
        if (r < 0.55f) return new ItemStack(Items.PRISMARINE_CRYSTALS, 1 + random.nextInt(2));
        if (r < 0.70f) return new ItemStack(Items.GOLD_ORE, 1);
        if (r < 0.82f) return new ItemStack(Items.EMERALD, 1);
        if (r < 0.92f) return new ItemStack(Items.DIAMOND, 1);
        if (r < 0.97f) return new ItemStack(Items.NAUTILUS_SHELL, 1);
        return new ItemStack(Items.HEART_OF_THE_SEA, 1);
    }

    private static List<ItemStack> baseLoot(AquaTechFishingRodItem.RodType type, RandomSource random) {
        List<ItemStack> list = new ArrayList<>();
        switch (type) {
            case NOVICE -> {
                // 100% guaranteed survival block drop for raft building
                ItemStack guaranteedStarter;
                float rStart = random.nextFloat();
                if (rStart < 0.20f) guaranteedStarter = new ItemStack(Items.COBBLESTONE, 2 + random.nextInt(3));
                else if (rStart < 0.40f) guaranteedStarter = new ItemStack(Items.DIRT, 2 + random.nextInt(2));
                else if (rStart < 0.60f) guaranteedStarter = new ItemStack(Items.OAK_SAPLING, 1);
                else if (rStart < 0.75f) guaranteedStarter = new ItemStack(Items.GRAVEL, 2 + random.nextInt(2));
                else if (rStart < 0.88f) guaranteedStarter = new ItemStack(Items.CLAY_BALL, 2 + random.nextInt(3));
                else guaranteedStarter = new ItemStack(Items.SAND, 2 + random.nextInt(2));
                list.add(guaranteedStarter);

                // Plus ores & botania seeds pool
                List<ItemStack> pool = new ArrayList<>();
                if (random.nextFloat() < 0.65f) pool.add(new ItemStack(Items.COPPER_ORE, 1 + random.nextInt(2)));
                if (random.nextFloat() < 0.50f) {
                    pool.add(getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1 + random.nextInt(2)));
                }
                if (random.nextFloat() < 0.45f) pool.add(new ItemStack(Items.IRON_ORE, 1 + random.nextInt(2)));
                if (random.nextFloat() < 0.40f) pool.add(new ItemStack(Items.COAL_ORE, 1 + random.nextInt(2)));
                if (random.nextFloat() < 0.40f) pool.add(new ItemStack(Items.WHEAT_SEEDS, 1 + random.nextInt(2)));
                if (random.nextFloat() < 0.35f) pool.add(new ItemStack(Items.BONE_MEAL, 1 + random.nextInt(2)));
                if (random.nextFloat() < 0.30f) {
                    pool.add(getModItem("botania:fertilizer", Items.BONE_MEAL, 1 + random.nextInt(2)));
                }
                pickFromPool(list, pool, random, 1, 2);
            }
            case IRON -> {
                // Chance pool — ores & botania items
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.COPPER_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/titanium", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.35f, getModItem("botania:fertilizer", Items.BONE_MEAL, 2 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.WHEAT_SEEDS, 1 + random.nextInt(3)));
                maybeAdd(pool, random, 0.35f, getModItem("industrialupgrade:baseore2/yttrium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.35f, getModItem("industrialupgrade:baseore/spinel", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.30f, getModItem("industrialupgrade:baseore2/strontium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.30f, getModItem("industrialupgrade:baseore2/barium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.30f, getModItem("industrialupgrade:baseore2/thallium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.10f, getModItem("industrialupgrade:baseore2/polonium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.35f, new ItemStack(Items.REDSTONE_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.35f, new ItemStack(Items.COAL_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case GOLD -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore/tungsten", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore/chromium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore/aluminium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.COPPER_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.GOLD_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore/silver", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore/nickel", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.35f, getModItem("industrialupgrade:preciousgem/sapphire_gem", Items.LAPIS_LAZULI, 1));
                maybeAdd(pool, random, 0.35f, getModItem("industrialupgrade:preciousgem/topaz_gem", Items.AMETHYST_SHARD, 1));
                maybeAdd(pool, random, 0.25f, getModItem("industrialupgrade:blockpreciousore/sapphire_ore", Items.LAPIS_ORE, 1));
                maybeAdd(pool, random, 0.25f, getModItem("industrialupgrade:blockpreciousore/topaz_ore", Items.AMETHYST_BLOCK, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case DIAMOND -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.DIAMOND, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.COPPER_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.PRISMARINE_SHARD, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case EMERALD -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.50f, new ItemStack(Items.EMERALD, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:synthetic_rubber", Items.DRIED_KELP, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.REDSTONE, 2 + random.nextInt(3)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case NETHERITE -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.NETHERITE_SCRAP, 1));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.QUARTZ, 2 + random.nextInt(3)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.COPPER_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case PRISMARINE -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.50f, new ItemStack(Items.PRISMARINE_SHARD, 2 + random.nextInt(3)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.PRISMARINE_CRYSTALS, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.35f, getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.08f, new ItemStack(Items.HEART_OF_THE_SEA, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case THERMAL -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.55f, new ItemStack(Items.REDSTONE, 2 + random.nextInt(3)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.COPPER_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case KINETIC -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.50f, new ItemStack(Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.COPPER_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case ENDER -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.50f, new ItemStack(Items.ENDER_PEARL, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.GOLD_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case ABYSSAL -> {
                List<ItemStack> pool = new ArrayList<>();
                maybeAdd(pool, random, 0.50f, new ItemStack(Items.ECHO_SHARD, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.35f, getModItem("industrialupgrade:baseore/platinum", Items.GOLD_ORE, 1));
                maybeAdd(pool, random, 0.06f, new ItemStack(Items.NETHER_STAR, 1));
                maybeAdd(pool, random, 0.10f, new ItemStack(Items.HEART_OF_THE_SEA, 1));
                maybeAdd(pool, random, 0.08f, new ItemStack(Items.NETHERITE_SCRAP, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
        }
        // Extra IU rolls (also chance-based), then keep only a few stacks total
        rollIuResources(type, list, random);
        keepRandomStacks(list, random, 1, 3);
        scaleLootToRodRange(list, type, random);
        return list;
    }

    private static List<ItemStack> rollStarCatcherRodLoot(String rodId, RandomSource random) {

        List<ItemStack> list = new ArrayList<>();
        List<ItemStack> pool = new ArrayList<>();

        switch (rodId) {
            case "humble_rod" -> { // Tier 2: Humble — early ores (+ some starter leftovers)
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.COBBLESTONE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.35f, new ItemStack(Items.CLAY_BALL, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.65f, new ItemStack(Items.COPPER_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.COAL_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.60f, getModItem("industrialupgrade:baseore/titanium", Items.IRON_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "bamboo_rod" -> { // Tier 1 starter: clay / dirt / cobble / saplings / hevea / bamboo
                // Always one survival starter drop (raft / early game)
                float rStart = random.nextFloat();
                ItemStack guaranteed;
                if (rStart < 0.16f) {
                    guaranteed = new ItemStack(Items.COBBLESTONE, 2 + random.nextInt(3));
                } else if (rStart < 0.30f) {
                    guaranteed = new ItemStack(Items.DIRT, 2 + random.nextInt(3));
                } else if (rStart < 0.42f) {
                    guaranteed = new ItemStack(Items.CLAY_BALL, 2 + random.nextInt(3));
                } else if (rStart < 0.54f) {
                    guaranteed = new ItemStack(Items.BAMBOO, 2 + random.nextInt(3));
                } else if (rStart < 0.62f) {
                    guaranteed = new ItemStack(Items.OAK_SAPLING, 1 + random.nextInt(2));
                } else if (rStart < 0.70f) {
                    guaranteed = getModItem("industrialupgrade:sapling/rubber_sapling", Items.OAK_SAPLING, 1);
                } else if (rStart < 0.80f) {
                    guaranteed = new ItemStack(Items.GRAVEL, 2 + random.nextInt(2));
                } else if (rStart < 0.90f) {
                    guaranteed = new ItemStack(Items.SAND, 2 + random.nextInt(2));
                } else {
                    guaranteed = new ItemStack(Items.BIRCH_SAPLING, 1);
                }
                list.add(guaranteed);

                maybeAdd(pool, random, 0.50f, new ItemStack(Items.COBBLESTONE, 1 + random.nextInt(3)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.DIRT, 1 + random.nextInt(3)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.CLAY_BALL, 1 + random.nextInt(3)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.BAMBOO, 1 + random.nextInt(3)));
                maybeAdd(pool, random, 0.35f, new ItemStack(Items.OAK_SAPLING, 1));
                maybeAdd(pool, random, 0.25f, new ItemStack(Items.BIRCH_SAPLING, 1));
                maybeAdd(pool, random, 0.35f, getModItem("industrialupgrade:sapling/rubber_sapling", Items.OAK_SAPLING, 1));
                maybeAdd(pool, random, 0.30f, getModItem("industrialupgrade:raw_latex", Items.SLIME_BALL, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.25f, getModItem("industrialupgrade:blockresource/untreated_peat", Items.DIRT, 1));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.GRAVEL, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.35f, new ItemStack(Items.SAND, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.COPPER_ORE, 1));
                maybeAdd(pool, random, 0.30f, getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1));
                pickFromPool(list, pool, random, 1, 2);
            }
            case "good_old_rod" -> { // Tier 3: Good Old Rod (Early LV / Iron & Tin & Spinel)
                maybeAdd(pool, random, 0.55f, new ItemStack(Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.50f, new ItemStack(Items.REDSTONE_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.LAPIS_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:baseore/spinel", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore2/strontium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore2/yttrium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.35f, getModItem("industrialupgrade:baseore2/thallium", Items.IRON_ORE, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "naturalist_rod" -> { // Tier 4: Naturalist Rod (LV Ores & Barium)
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:baseore2/barium", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:classicore/tin", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.IRON_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "slimed_rod" -> { // Tier 5: Slimed Rod (LV Ores & Polonium)
                maybeAdd(pool, random, 0.55f, getModItem("industrialupgrade:baseore/spinel", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:baseore2/barium", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore2/polonium", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.IRON_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "iceborn_rod" -> { // Tier 6: Iceborn Rod (MV Silver & Aluminium)
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:baseore/aluminium", Items.IRON_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/silver", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/zinc", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.IRON_ORE, 1 + random.nextInt(2)));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "starcatcher_rod" -> { // Tier 7: StarCatcher Rod (MV Gold & Tungsten & Sapphire)
                maybeAdd(pool, random, 0.50f, new ItemStack(Items.GOLD_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.LAPIS_LAZULI, 2 + random.nextInt(4)));
                maybeAdd(pool, random, 0.35f, new ItemStack(Items.LAPIS_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/tungsten", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/chromium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:preciousgem/sapphire_gem", Items.LAPIS_LAZULI, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:preciousgem/topaz_gem", Items.AMETHYST_SHARD, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "azure_crystal_rod" -> { // Tier 8: Azure Crystal Rod (MV Crystals)
                maybeAdd(pool, random, 0.55f, new ItemStack(Items.LAPIS_LAZULI, 3 + random.nextInt(5)));
                maybeAdd(pool, random, 0.40f, new ItemStack(Items.LAPIS_ORE, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.55f, new ItemStack(Items.AMETHYST_SHARD, 2 + random.nextInt(3)));
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:preciousgem/sapphire_gem", Items.LAPIS_LAZULI, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:preciousgem/topaz_gem", Items.AMETHYST_SHARD, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:blockpreciousore/sapphire_ore", Items.LAPIS_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:mineral/crystal", Items.AMETHYST_BLOCK, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "sharktooth_rod" -> { // Tier 9: Sharktooth Rod (HV Heavy Ores)
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:baseore/titanium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/cobalt", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/manganese", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/nickel", Items.IRON_ORE, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "obsidian_rod" -> { // Tier 10: Obsidian Rod (HV Steel & Diamond)
                maybeAdd(pool, random, 0.55f, new ItemStack(Items.DIAMOND, 1));
                maybeAdd(pool, random, 0.55f, new ItemStack(Items.OBSIDIAN, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, getModItem("industrialupgrade:baseore/titanium", Items.IRON_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:alloyingot/stainless_steel", Items.IRON_INGOT, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "lush_glowberry_rod" -> { // Tier 11: Lush Glowberry Rod (HV Platinum & Ocean Crystals)
                maybeAdd(pool, random, 0.55f, new ItemStack(Items.PRISMARINE_SHARD, 2 + random.nextInt(3)));
                maybeAdd(pool, random, 0.50f, new ItemStack(Items.PRISMARINE_CRYSTALS, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore/platinum", Items.GOLD_ORE, 1));
                maybeAdd(pool, random, 0.20f, new ItemStack(Items.HEART_OF_THE_SEA, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "magmaforged_rod" -> { // Tier 12: Magmaforged Rod (EV Nuclear & Alloy Ores)
                maybeAdd(pool, random, 0.55f, new ItemStack(Items.QUARTZ, 2 + random.nextInt(3)));
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:crushed/uranium", Items.RAW_GOLD, 1 + random.nextInt(2)));
                maybeAdd(pool, random, 0.45f, new ItemStack(Items.NETHERITE_SCRAP, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:alloyingot/inconel", Items.NETHERITE_SCRAP, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            case "alpha_rod" -> { // Tier 13: Alpha Rod (Quantum Endgame Ores)
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:baseore/iridium", Items.DIAMOND, 1));
                maybeAdd(pool, random, 0.50f, getModItem("industrialupgrade:baseore1/osmium", Items.GOLD_ORE, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:baseore2/polonium", Items.EMERALD, 1));
                maybeAdd(pool, random, 0.40f, getModItem("industrialupgrade:alloyingot/osmiridium", Items.NETHERITE_INGOT, 1));
                maybeAdd(pool, random, 0.30f, getModItem("industrialupgrade:asteroidore/asteroid_adamantium_ore", Items.NETHERITE_SCRAP, 1));
                maybeAdd(pool, random, 0.20f, new ItemStack(Items.NETHER_STAR, 1));
                pickFromPool(list, pool, random, 1, 3);
            }
            default -> {
                return baseLoot(AquaTechFishingRodItem.RodType.IRON, random);
            }
        }
        return list;
    }

    private static void maybeAdd(List<ItemStack> pool, RandomSource random, float chance, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        if (random.nextFloat() < chance) pool.add(stack);
    }

    /** Shuffle pool and keep {@code min}–{@code max} stacks. */
    private static void pickFromPool(List<ItemStack> dest, List<ItemStack> pool, RandomSource random, int min, int max) {
        if (pool.isEmpty()) return;
        keepRandomStacks(pool, random, min, max);
        dest.addAll(pool);
    }

    private static void keepRandomStacks(List<ItemStack> list, RandomSource random, int min, int max) {
        if (list.size() <= min) return;
        // Fisher–Yates
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            ItemStack tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
        int keep = Math.min(list.size(), min + random.nextInt(Math.max(1, max - min + 1)));
        while (list.size() > keep) {
            list.remove(list.size() - 1);
        }
    }

    /** Tiered Industrial Upgrade drops — ocean fishing replaces pickaxe / world farming. */
    private static void rollIuResources(AquaTechFishingRodItem.RodType type, List<ItemStack> list, RandomSource random) {
        int tier = type.ordinal();
        rollIuBasics(list, random, tier);
        rollIuOres(list, random, tier);
        if (tier >= AquaTechFishingRodItem.RodType.GOLD.ordinal()) {
            rollGoldTierExtras(list, random);
            rollIuBeesAndRubber(list, random, tier);
        }
        if (tier >= AquaTechFishingRodItem.RodType.DIAMOND.ordinal()) {
            rollIuOil(list, random, tier);
        }
        if (tier >= AquaTechFishingRodItem.RodType.NETHERITE.ordinal()
                || tier >= AquaTechFishingRodItem.RodType.PRISMARINE.ordinal()) {
            rollIuMinerals(list, random, tier);
        }
    }

    private static void rollIuBasics(List<ItemStack> list, RandomSource random, int tier) {
        float m = 1.0f + tier * 0.04f;
        if (random.nextFloat() < 0.18f * m) list.add(new ItemStack(Items.IRON_ORE, 1));
        if (random.nextFloat() < 0.16f * m) list.add(new ItemStack(Items.COPPER_ORE, 1));
        addIuChance(list, random, 0.14f * m, "industrialupgrade:classicore/tin", Items.IRON_ORE, 1);
        if (tier >= AquaTechFishingRodItem.RodType.IRON.ordinal()) {
            rollStarterTerrainAndOres(list, random);
        }
    }

    /** Mid-tier extras — starcatcher (GOLD)+; ores / gems. */
    private static void rollGoldTierExtras(List<ItemStack> list, RandomSource random) {
        String[] mids = {
                "industrialupgrade:baseore/tungsten",
                "industrialupgrade:baseore/chromium",
                "industrialupgrade:baseore/aluminium",
                "industrialupgrade:baseore/nickel",
                "industrialupgrade:baseore/silver"
        };
        if (random.nextFloat() < 0.28f) {
            list.add(getModItem(mids[random.nextInt(mids.length)], Items.IRON_ORE, 1));
        }
        if (random.nextFloat() < 0.22f) list.add(new ItemStack(Items.GOLD_ORE, 1));
        if (random.nextFloat() < 0.22f) list.add(new ItemStack(Items.COPPER_ORE, 1));
        addIuChance(list, random, 0.16f, "industrialupgrade:preciousgem/sapphire_gem", Items.LAPIS_LAZULI, 1);
        addIuChance(list, random, 0.16f, "industrialupgrade:preciousgem/topaz_gem", Items.AMETHYST_SHARD, 1);
    }

    /** Soft early IU ore chances — first resource rod+. */
    private static void rollStarterTerrainAndOres(List<ItemStack> list, RandomSource random) {
        if (random.nextFloat() < 0.16f) list.add(new ItemStack(Items.REDSTONE_ORE, 1));
        if (random.nextFloat() < 0.14f) {
            Item[] ores = {Items.IRON_ORE, Items.COPPER_ORE, Items.COAL_ORE, Items.REDSTONE_ORE};
            list.add(new ItemStack(ores[random.nextInt(ores.length)], 1));
        }
        addIuChance(list, random, 0.14f, "industrialupgrade:baseore2/yttrium", Items.IRON_ORE, 1);
        addIuChance(list, random, 0.14f, "industrialupgrade:baseore/spinel", Items.IRON_ORE, 1);
        addIuChance(list, random, 0.14f, "industrialupgrade:classicore/tin", Items.IRON_ORE, 1);
        addIuChance(list, random, 0.12f, "industrialupgrade:baseore2/strontium", Items.IRON_ORE, 1);
        addIuChance(list, random, 0.12f, "industrialupgrade:baseore2/barium", Items.IRON_ORE, 1);
        addIuChance(list, random, 0.12f, "industrialupgrade:baseore2/thallium", Items.IRON_ORE, 1);
        addIuChance(list, random, 0.04f, "industrialupgrade:baseore2/polonium", Items.IRON_ORE, 1);
    }

    /** Ore-only metal rolls — never ingots / raw. */
    private static void rollIuOres(List<ItemStack> list, RandomSource random, int tier) {
        if (tier >= AquaTechFishingRodItem.RodType.IRON.ordinal() && random.nextFloat() < 0.22f) {
            list.add(getModItem("industrialupgrade:baseore/titanium", Items.IRON_ORE, 1));
        }

        String[] early = {
                "industrialupgrade:classicore/tin",
                "industrialupgrade:baseore/nickel",
                "industrialupgrade:baseore/zinc",
                "industrialupgrade:baseore/silver",
                "industrialupgrade:baseore2/strontium",
                "industrialupgrade:baseore2/barium",
                "industrialupgrade:baseore2/thallium"
        };
        if (random.nextFloat() < 0.28f + tier * 0.02f) {
            list.add(getModItem(early[random.nextInt(early.length)], Items.IRON_ORE, 1));
        }
        if (tier >= AquaTechFishingRodItem.RodType.IRON.ordinal() && random.nextFloat() < 0.04f) {
            list.add(getModItem("industrialupgrade:baseore2/polonium", Items.IRON_ORE, 1));
        }

        if (tier >= AquaTechFishingRodItem.RodType.GOLD.ordinal() && random.nextFloat() < 0.22f) {
            String[] mid = {
                    "industrialupgrade:baseore/aluminium",
                    "industrialupgrade:baseore/cobalt",
                    "industrialupgrade:baseore/nickel",
                    "industrialupgrade:baseore/chromium",
                    "industrialupgrade:baseore/tungsten"
            };
            list.add(getModItem(mid[random.nextInt(mid.length)], Items.IRON_ORE, 1));
        }

        if (tier >= AquaTechFishingRodItem.RodType.DIAMOND.ordinal() && random.nextFloat() < 0.18f) {
            String[] late = {
                    "industrialupgrade:baseore/chromium",
                    "industrialupgrade:baseore/tungsten",
                    "industrialupgrade:baseore/platinum",
                    "industrialupgrade:baseore/titanium"
            };
            list.add(getModItem(late[random.nextInt(late.length)], Items.GOLD_ORE, 1));
        }

        if (tier >= AquaTechFishingRodItem.RodType.NETHERITE.ordinal() && random.nextFloat() < 0.12f) {
            String[] end = {
                    "industrialupgrade:baseore/tungsten",
                    "industrialupgrade:baseore/platinum"
            };
            list.add(getModItem(end[random.nextInt(end.length)], Items.NETHERITE_SCRAP, 1));
        }
    }

    private static void rollIuEarly(List<ItemStack> list, RandomSource random, float chanceMult) {
        rollIuBasics(list, random, Math.round((chanceMult - 1.0f) / 0.08f));
    }

    private static void rollIuBeesAndRubber(List<ItemStack> list, RandomSource random, int tier) {
        // Disabled — fishing drops ores exclusively
    }

    private static void rollIuOil(List<ItemStack> list, RandomSource random, int tier) {
        // Disabled — fishing drops ores exclusively
    }

    private static void rollIuMinerals(List<ItemStack> list, RandomSource random, int tier) {
        float chance = tier >= AquaTechFishingRodItem.RodType.ABYSSAL.ordinal() ? 0.25f : 0.12f;
        addIuChance(list, random, chance, "industrialupgrade:mineral/crystal", Items.AMETHYST_SHARD, 1);
        if (random.nextFloat() < chance) {
            String[] minerals = {
                    "industrialupgrade:mineral/wolframite",
                    "industrialupgrade:mineral/coltan",
                    "industrialupgrade:mineral/tetrahedrite",
                    "industrialupgrade:mineral/arsenopyrite",
                    "industrialupgrade:mineral/celestine",
                    "industrialupgrade:mineral/zircon",
                    "industrialupgrade:mineral/xenotime",
                    "industrialupgrade:mineral/germanite"
            };
            list.add(getModItem(minerals[random.nextInt(minerals.length)], Items.RAW_IRON, 1));
        }
    }

    private static void addIuChance(List<ItemStack> list, RandomSource random, float chance,
                                    String regName, Item fallback, int count) {
        if (chance >= 1.0f || random.nextFloat() < chance) {
            list.add(getModItem(regName, fallback, Math.max(1, count)));
        }
    }

    /**
     * Scales stack counts so total items fall in {@link RodLootRanges} for this rod
     * (same numbers shown on SHIFT tooltip).
     */
    private static void scaleLootToRodRange(List<ItemStack> list, AquaTechFishingRodItem.RodType type, RandomSource random) {
        if (list.isEmpty()) return;
        int current = 0;
        for (ItemStack s : list) current += s.getCount();
        if (current <= 0) return;

        int target = RodLootRanges.rollTotal(type, random);
        if (target == current) return;

        // Distribute proportionally; keep at least 1 on each existing stack when possible
        int remaining = target;
        for (int i = 0; i < list.size(); i++) {
            ItemStack s = list.get(i);
            int share;
            if (i == list.size() - 1) {
                share = Math.max(1, remaining);
            } else {
                share = Math.max(1, Math.round(target * (s.getCount() / (float) current)));
                share = Math.min(share, remaining - (list.size() - i - 1));
            }
            share = Math.min(s.getMaxStackSize(), Math.max(1, share));
            s.setCount(share);
            remaining -= share;
        }
        // If still short (max-stack caps), spill extras onto first expandable stack / new copies
        while (remaining > 0) {
            boolean placed = false;
            for (ItemStack s : list) {
                int room = s.getMaxStackSize() - s.getCount();
                if (room <= 0) continue;
                int add = Math.min(room, remaining);
                s.setCount(s.getCount() + add);
                remaining -= add;
                placed = true;
                if (remaining <= 0) break;
            }
            if (!placed) {
                ItemStack extra = list.get(0).copy();
                int add = Math.min(extra.getMaxStackSize(), remaining);
                extra.setCount(add);
                list.add(extra);
                remaining -= add;
            }
        }
    }

    private static void applyRateMultiplier(List<ItemStack> list, int rate) {
        if (rate <= 1 || list.isEmpty()) return;
        List<ItemStack> spill = new ArrayList<>();
        for (ItemStack s : list) {
            long total = (long) s.getCount() * rate;
            if (total <= s.getMaxStackSize()) {
                s.setCount((int) total);
            } else {
                s.setCount(s.getMaxStackSize());
                total -= s.getMaxStackSize();
                while (total > 0) {
                    ItemStack extra = s.copy();
                    int add = (int) Math.min(extra.getMaxStackSize(), total);
                    extra.setCount(add);
                    spill.add(extra);
                    total -= add;
                }
            }
        }
        list.addAll(spill);
    }

    public static int readRateMultiplier(ItemStack rodStack) {
        if (rodStack == null || rodStack.isEmpty()) return 1;
        int best = StarCatcherAttachments.readRateMultiplier(rodStack);

        // Legacy AquaTech TackleInventory NBT (old Shift+RMB menu)
        if (rodStack.hasTag()) {
            CompoundTag root = rodStack.getTag();
            if (root != null && root.contains("TackleInventory")) {
                ItemStackHandler handler = new ItemStackHandler(4);
                handler.deserializeNBT(root.getCompound("TackleInventory"));
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack s = handler.getStackInSlot(i);
                    if (s.getItem() instanceof RateModItem rate) {
                        best = Math.max(best, rate.getMultiplier());
                    }
                }
            }
        }
        return Math.max(1, best);
    }

    /**
     * Ease bonus for the Rhythm Hook mini-game from rate mods (0..~0.1).
     */
    public static float fishingGearEase(@org.jetbrains.annotations.Nullable Player player, ItemStack rodStack) {
        float ease = 0f;
        if (readRateMultiplier(rodStack) > 1) ease += 0.03f;
        return ease;
    }

    private static ItemStack getModItem(String regName, Item fallback, int count) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(regName));
            if (item != null && item != Items.AIR) {
                return new ItemStack(item, count);
            }
        } catch (Exception ignored) {
        }
        return new ItemStack(fallback, count);
    }
}
