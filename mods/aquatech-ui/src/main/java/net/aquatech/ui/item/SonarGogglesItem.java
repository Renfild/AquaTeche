package net.aquatech.ui.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SonarGogglesItem extends ArmorItem {

    public SonarGogglesItem(Properties properties) {
        super(ArmorMaterials.TURTLE, Type.HELMET, properties);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && player.isInWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 220, 0, false, false, true));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§bПод водой: Ночное Зрение и Сила Кондуита"));
        tooltip.add(Component.literal("§3Присед под водой: сонар-импульс (КД 8с)"));
        tooltip.add(Component.literal("§8Подсвечивает существ в радиусе 24 блоков"));
    }
}
