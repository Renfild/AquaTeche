package com.casesmod.data;

import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.BalanceSyncS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

/**
 * Server-side fish sell: hand or full inventory → Дублоны.
 * Never called from login events.
 */
public final class FishSellService {
    private FishSellService() {}

    public static long sell(ServerPlayer player, boolean handOnly) {
        long totalEarned = 0L;
        int fishCount = 0;
        boolean hasGolden = false;

        if (handOnly) {
            ItemStack hand = player.getMainHandItem();
            FishPriceCalculator.PriceResult result = FishPriceCalculator.calculatePrice(hand);
            if (result.isFish() && result.finalPrice() > 0) {
                totalEarned += result.finalPrice();
                fishCount += hand.getCount();
                if (result.isGolden()) hasGolden = true;
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        } else {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                FishPriceCalculator.PriceResult result = FishPriceCalculator.calculatePrice(stack);
                if (result.isFish() && result.finalPrice() > 0) {
                    totalEarned += result.finalPrice();
                    fishCount += stack.getCount();
                    if (result.isGolden()) hasGolden = true;
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }

        if (totalEarned <= 0) {
            player.sendSystemMessage(Component.literal("[Рынок Рыбы] ")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                    .append(Component.literal("В " + (handOnly ? "руке" : "инвентаре") + " нет улова StarCatcher для продажи.")
                            .withStyle(ChatFormatting.RED)));
            return 0L;
        }

        CurrencyManager.INSTANCE.add(player.getUUID(), totalEarned);
        long newBalance = CurrencyManager.INSTANCE.getBalance(player.getUUID());
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new BalanceSyncS2CPacket(newBalance));

        if (hasGolden) {
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(Component.literal("★ ")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    .append(Component.literal("[Рынок Рыбы] ")
                            .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                    .append(Component.literal("Продано " + fishCount + " рыб (включая Золотой Трофей!) на ")
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("+" + totalEarned + " Дублонов")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .append(Component.literal(". Баланс: ")
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.valueOf(newBalance))
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        } else {
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
            player.sendSystemMessage(Component.literal("[Рынок Рыбы] ")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                    .append(Component.literal("Продано " + fishCount + " рыб на ")
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("+" + totalEarned + " Дублонов")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .append(Component.literal(". Баланс: ")
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.valueOf(newBalance))
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        }
        return totalEarned;
    }
}
