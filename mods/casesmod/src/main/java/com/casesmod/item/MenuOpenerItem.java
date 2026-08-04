package com.casesmod.item;

import com.casesmod.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** ПКМ по этому предмету открывает главное меню сервера. Выдаётся при входе или командой /casesmod givemenu. */
public class MenuOpenerItem extends Item {
    public MenuOpenerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            NetworkHandler.openMenuFor(sp);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
