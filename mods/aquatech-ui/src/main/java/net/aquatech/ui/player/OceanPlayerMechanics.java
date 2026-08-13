package net.aquatech.ui.player;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.capability.SkillEffects;
import net.aquatech.ui.item.SonarGogglesItem;
import net.aquatech.ui.server.PressureBridge;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Unusual ocean player systems: crush pressure, undertow currents,
 * bioluminescent wake, and sonar goggle pulse.
 */
@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OceanPlayerMechanics {

    private static final Map<UUID, Integer> SONAR_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Integer> CRUSH_TICK = new HashMap<>();
    private static final Map<UUID, Integer> CURRENT_TICK = new HashMap<>();

    private OceanPlayerMechanics() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) {
            clientBiolume(player);
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        tickCooldowns(serverPlayer.getUUID());

        boolean inWater = serverPlayer.isInWater() || serverPlayer.isEyeInFluid(FluidTags.WATER);
        if (!inWater) {
            CRUSH_TICK.remove(serverPlayer.getUUID());
            CURRENT_TICK.remove(serverPlayer.getUUID());
            return;
        }

        PressureBridge.PressureInfo pressure = PressureBridge.fromPlayer(serverPlayer);
        int effective = pressure.effective();
        int depth = pressure.depth();

        applyCrush(serverPlayer, effective);
        applyUndertow(serverPlayer, effective);
        serverBiolume(serverPlayer, depth);
        trySonarPulse(serverPlayer);
    }

    private static void tickCooldowns(UUID id) {
        SONAR_COOLDOWN.computeIfPresent(id, (k, v) -> v <= 1 ? null : v - 1);
    }

    private static boolean wearingSonar(Player player) {
        ItemStack helmet = player.getInventory().getArmor(3);
        return helmet.getItem() instanceof SonarGogglesItem;
    }

    /** Pressure damage / fatigue that matches HUD severity bands. */
    private static void applyCrush(ServerPlayer player, int effective) {
        if (effective <= 5) return;
        UUID id = player.getUUID();
        int tick = CRUSH_TICK.getOrDefault(id, 0) + 1;
        CRUSH_TICK.put(id, tick);

        if (effective <= 10) {
            // Soft zone: occasional Mining Fatigue I
            if (tick % 80 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, true, false, true));
            }
            return;
        }

        if (effective <= 15) {
            if (tick % 60 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 50, 0, true, false, true));
            }
            return;
        }

        // High / extreme: crush damage
        int interval = effective > 25 ? 40 : 55;
        if (tick % interval == 0) {
            float dmg = effective > 25 ? 2.5f : 1.5f;
            dmg *= (1.0f - SkillEffects.snapshot(player).waterDamageReduction());
            if (dmg > 0.2f) {
                player.hurt(player.damageSources().drown(), dmg);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.BUBBLE,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            12, 0.4, 0.5, 0.4, 0.02);
                }
                if (effective > 20 && tick % (interval * 3) == 0) {
                    player.displayClientMessage(Component.literal("§c⚠ Давление сжимает грудную клетку..."), true);
                }
            }
        }
    }

    /** Random undertow shove — only when effective pressure is actually high. */
    private static void applyUndertow(ServerPlayer player, int effective) {
        if (effective < 10) return;
        UUID id = player.getUUID();
        int tick = CURRENT_TICK.getOrDefault(id, 0) + 1;
        CURRENT_TICK.put(id, tick);

        int period = effective > 25 ? 70 : (effective > 15 ? 110 : 160);
        if (tick % period != 0) return;

        double power = 0.35 + Math.min(0.55, effective * 0.02);
        double angle = player.getRandom().nextDouble() * Math.PI * 2;
        Vec3 push = new Vec3(Math.cos(angle) * power, (player.getRandom().nextDouble() - 0.4) * 0.15, Math.sin(angle) * power);
        player.setDeltaMovement(player.getDeltaMovement().add(push));
        player.hurtMarked = true;

        player.level().playSound(null, player.blockPosition(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE,
                SoundSource.AMBIENT, 0.55f, 0.7f + player.getRandom().nextFloat() * 0.3f);
        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CURRENT_DOWN,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    18, 0.6, 0.4, 0.6, 0.01);
        }
    }

    private static void serverBiolume(ServerPlayer player, int depth) {
        if (depth < 12) return;
        if (player.tickCount % 4 != 0) return;
        if (!(player.level() instanceof ServerLevel sl)) return;
        double speed = player.getDeltaMovement().horizontalDistance();
        if (speed < 0.02 && !player.isSprinting()) return;
        sl.sendParticles(ParticleTypes.GLOW_SQUID_INK,
                player.getX(), player.getY() + 0.3, player.getZ(),
                1, 0.05, 0.05, 0.05, 0.0);
        sl.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 0.2, player.getZ(),
                1, 0.08, 0.05, 0.08, 0.0);
    }

    private static void clientBiolume(Player player) {
        if (!player.isInWater()) return;
        if (player.tickCount % 5 != 0) return;
        double speed = player.getDeltaMovement().horizontalDistance();
        if (speed < 0.03) return;
        if (player.getY() > 50) return;
        player.level().addParticle(ParticleTypes.GLOW,
                player.getX(), player.getY() + 0.2, player.getZ(),
                0, 0.01, 0);
    }

    /** Crouch + sonar goggles: ping nearby living entities with Glowing. */
    private static void trySonarPulse(ServerPlayer player) {
        if (!player.isCrouching() || !wearingSonar(player)) return;
        UUID id = player.getUUID();
        if (SONAR_COOLDOWN.containsKey(id)) return;

        SONAR_COOLDOWN.put(id, 160); // 8 seconds

        AABB box = player.getBoundingBox().inflate(24.0);
        List<LivingEntity> found = player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive());

        int marked = 0;
        for (LivingEntity e : found) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, true, true));
            marked++;
        }

        player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_LISTENING,
                SoundSource.PLAYERS, 0.7f, 1.4f);
        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SONIC_BOOM,
                    player.getX(), player.getY() + 1.2, player.getZ(),
                    1, 0, 0, 0, 0);
            // Expanding ring of bubbles
            for (int i = 0; i < 36; i++) {
                double a = i * Math.PI * 2 / 36;
                sl.sendParticles(ParticleTypes.BUBBLE,
                        player.getX() + Math.cos(a) * 2.5,
                        player.getY() + 1.0,
                        player.getZ() + Math.sin(a) * 2.5,
                        1, 0, 0, 0, 0);
            }
        }
        player.displayClientMessage(Component.literal("§b⌁ Сонар: обнаружено существ — §f" + marked), true);
    }
}
