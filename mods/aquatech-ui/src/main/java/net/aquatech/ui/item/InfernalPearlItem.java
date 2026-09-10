package net.aquatech.ui.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Жемчужина Пламени: ПКМ — Ад ↔ точка входа. Кулдаун 10 мин. */
public class InfernalPearlItem extends Item {

    public InfernalPearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InfernalPearlHandler.apply(player, level, this);
        return InteractionResultHolder.success(stack);
    }
}
