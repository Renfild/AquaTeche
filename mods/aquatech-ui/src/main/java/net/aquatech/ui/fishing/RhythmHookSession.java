package net.aquatech.ui.fishing;

import net.aquatech.ui.capability.SkillEffects;
import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.network.NetworkHandler;
import net.aquatech.ui.network.S2CStartRhythmHookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side sessions for the StarCatcher-style vertical-bar fishing mini-game.
 */
@Mod.EventBusSubscriber(modid = "aquatech_ui", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RhythmHookSession {

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    public record Pending(
            AquaTechFishingRodItem.RodType rodType,
            ItemStack rodStack,
            long startedAtMs,
            int seed,
            int fishHp,
            float spotSize,
            float yellowPad,
            float pointerSpeed,
            float decay,
            boolean elite,
            boolean treasure
    ) {
    }

    private RhythmHookSession() {
    }

    public static boolean hasPending(UUID playerId) {
        return PENDING.containsKey(playerId);
    }

    public static void start(ServerPlayer player, AquaTechFishingRodItem rodItem, ItemStack rodStack) {
        start(player, rodItem.getRodType(), rodStack);
    }

    public static void start(ServerPlayer player, AquaTechFishingRodItem.RodType rodType, ItemStack rodStack) {
        if (!ModConfig.FISHING_MINIGAME.get()) {
            FishingLootHandler.awardCatch(player, rodType, rodStack, 1.0f, 70);
            return;
        }
        if (hasPending(player.getUUID())) {
            return;
        }

        float rodEase = rodType.ordinal() / (float) Math.max(1,
                AquaTechFishingRodItem.RodType.values().length - 1);
        float skill = SkillEffects.fishingSpeedBonus(player);
        float gear = FishingLootHandler.fishingGearEase(player, rodStack);
        float ease = Mth.clamp(rodEase * 0.55f + skill * 0.70f + gear, 0f, 1.15f);

        float baseChance = 0.10f;
        float tierBonus = rodEase * 0.12f;
        float skillBonus = SkillEffects.rareLootBonus(player) * 0.15f;
        float gearBonus = gear * 0.08f;
        float eliteChance = Mth.clamp(baseChance + tierBonus + skillBonus + gearBonus, 0.08f, 0.45f);
        boolean elite = player.getRandom().nextFloat() < eliteChance;
        boolean treasure = elite && player.getRandom().nextFloat() < 0.65f;

        int fishHp = 20;
        float spotSize = Mth.clamp(24f + ease * 10f - (elite ? 3f : 0f), 18f, 38f);
        float yellowPad = Mth.clamp(10f + ease * 5f, 8f, 18f);
        float pointerSpeed = Mth.clamp(2.0f - ease * 0.55f + (elite ? 0.35f : 0f), 1.0f, 2.8f);
        float decay = Mth.clamp(0.055f - ease * 0.018f, 0.028f, 0.06f);
        int seed = player.getRandom().nextInt();

        Pending pending = new Pending(
                rodType,
                rodStack.copy(),
                System.currentTimeMillis(),
                seed,
                fishHp,
                spotSize,
                yellowPad,
                pointerSpeed,
                decay,
                elite,
                treasure
        );
        PENDING.put(player.getUUID(), pending);

        NetworkHandler.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new S2CStartRhythmHookPacket(seed, fishHp, spotSize, yellowPad,
                        pointerSpeed, decay, elite, treasure)
        );
        player.level().playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_SPLASH,
                SoundSource.PLAYERS, 0.8f, 1.2f);
    }

    public static void complete(ServerPlayer player, boolean success, int quality,
                                int hitsGreen, int hitsYellow) {
        Pending pending = PENDING.remove(player.getUUID());
        if (pending == null) return;

        long elapsed = System.currentTimeMillis() - pending.startedAtMs;
        long maxMs = 35_000L;
        long minMs = success ? 2_500L : 0L;
        if (elapsed > maxMs) {
            fail(player);
            return;
        }
        if (success && elapsed < minMs) {
            fail(player);
            return;
        }

        quality = Math.max(0, Math.min(100, quality));
        hitsGreen = Math.max(0, hitsGreen);
        hitsYellow = Math.max(0, hitsYellow);

        if (success) {
            if (hitsGreen + hitsYellow < 8) {
                fail(player);
                return;
            }
            int expectedAvg = hitsGreen + hitsYellow > 0
                    ? Math.round((hitsGreen * 100f + hitsYellow * 55f) / (hitsGreen + hitsYellow))
                    : 0;
            if (quality > expectedAvg + 20) {
                quality = expectedAvg;
            }
        }

        if (!success || quality < 8) {
            fail(player);
            return;
        }

        float lootScale = 0.70f + (quality / 100f) * 0.55f;
        if (pending.treasure() && quality >= 85) {
            lootScale += 0.15f;
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack rod = pending.rodStack();
        AquaTechFishingRodItem.RodType type = pending.rodType();
        if (FishingRodCompat.isSupportedRod(main)
                && FishingRodCompat.resolveRodType(main) == type) {
            rod = main;
        } else if (FishingRodCompat.isSupportedRod(off)
                && FishingRodCompat.resolveRodType(off) == type) {
            rod = off;
        }

        FishingLootHandler.awardCatch(player, type, rod, lootScale, quality);
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.7f, 1.15f);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "hud.aquatech_ui.rhythm_hook.success", quality), true);

        player.getCapability(net.aquatech.ui.capability.AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            long day = player.level().getDayTime() / 24000L;
            cap.ensureDaily(day);
            if (cap.currentContract() == net.aquatech.ui.horizon.HorizonRoute.DailyContract.FISH
                    && !cap.isDailyClaimed()) {
                cap.addDailyProgress(1);
            }
        });
    }

    private static void fail(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE,
                SoundSource.PLAYERS, 0.7f, 0.6f);
        ItemEntity kelp = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(),
                new ItemStack(Items.KELP, 1 + player.getRandom().nextInt(2)));
        player.level().addFreshEntity(kelp);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "hud.aquatech_ui.rhythm_hook.fail"), true);
    }

    public static void clear(UUID playerId) {
        PENDING.remove(playerId);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            clear(event.getEntity().getUUID());
        }
    }
}
