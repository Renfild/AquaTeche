package net.aquatech.ui.capability;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.KelpPlantBlock;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Applies diving / bio skill passives and kelp harvest bonus.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SkillPassiveHandler {

    private SkillPassiveHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;

        SkillEffects.Snapshot skills = SkillEffects.snapshot(player);
        boolean inWater = player.isEyeInFluid(FluidTags.WATER) || player.isInWater();

        if (inWater && skills.waterBreathing()) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false, true));
        }
        if (inWater && skills.nightVisionWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false, true));
        }
        if (inWater && skills.regenInWater()) {
            int amp = skills.regenAmplifier();
            if (amp >= 0) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, amp, true, false, true));
            }
        }

        float swim = skills.swimSpeedBonus();
        if (swim > 0f && player.isInWater()) {
            if (!player.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                int amp = swim >= 0.25f ? 1 : 0;
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 30, amp, true, false, true));
            }
        }

        int extraAir = skills.extraAirTicks();
        if (extraAir > 0 && player.isEyeInFluid(FluidTags.WATER)) {
            int max = player.getMaxAirSupply() + extraAir;
            if (player.getAirSupply() < max && player.getAirSupply() > 0) {
                player.setAirSupply(Math.min(max, player.getAirSupply() + 2));
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;
        var state = event.getState();
        boolean kelp = state.getBlock() instanceof KelpBlock
                || state.getBlock() instanceof KelpPlantBlock
                || state.is(Blocks.KELP)
                || state.is(Blocks.KELP_PLANT)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.TALL_SEAGRASS);
        if (!kelp) return;

        float bonus = SkillEffects.snapshot(player).kelpHarvestBonus();
        if (bonus <= 0f) return;
        var random = player.level().getRandom();
        int extras = (int) bonus;
        float frac = bonus - extras;
        if (random.nextFloat() < frac) extras++;
        for (int i = 0; i < extras; i++) {
            var drop = state.getBlock().asItem().getDefaultInstance();
            if (!drop.isEmpty()) {
                player.getInventory().placeItemBackInInventory(drop);
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.isInWater() && !player.isEyeInFluid(FluidTags.WATER)) return;
        float reduce = SkillEffects.snapshot(player).waterDamageReduction();
        if (reduce <= 0f) return;
        event.setAmount(event.getAmount() * (1.0f - reduce));
    }
}
