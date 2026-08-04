package net.aquatech.ui.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class NeptuneTridentItem extends Item {

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCA8A5FD7D5");
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public NeptuneTridentItem(Properties properties) {
        super(properties.durability(2500).fireResistant());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_UUID, "Weapon modifier", 11.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_UUID, "Weapon modifier", -2.6, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Vec3 look = player.getLookAngle();
            BlockPos targetPos = player.blockPosition().offset((int) (look.x * 12), (int) (look.y * 12), (int) (look.z * 12));

            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightning != null) {
                lightning.moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                serverLevel.addFreshEntity(lightning);
            }

            if (player.isInWater() || player.isEyeInFluid(FluidTags.WATER)) {
                AABB box = player.getBoundingBox().inflate(6.0);
                List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                for (LivingEntity e : victims) {
                    e.hurt(player.damageSources().magic(), 8.0f);
                    Vec3 away = e.position().subtract(player.position()).normalize().scale(0.85).add(0, 0.25, 0);
                    e.setDeltaMovement(e.getDeltaMovement().add(away));
                    e.hurtMarked = true;
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.2F, 0.75F);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 0.9F);
            }

            stack.hurtAndBreak(5, player, p -> p.broadcastBreakEvent(hand));
            player.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.isInWater() || attacker.isEyeInFluid(FluidTags.WATER)) {
            target.hurt(attacker.damageSources().magic(), 6.0f);
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.28, 0));
            target.hurtMarked = true;
        }
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(attacker.getUsedItemHand()));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§eПКМ — молния по взгляду (КД 2с)"));
        tooltip.add(Component.literal("§bПод водой ПКМ — ударная волна 6м"));
        tooltip.add(Component.literal("§cУрон в ближнем бою: 12 (+6 шок в воде)"));
    }
}
