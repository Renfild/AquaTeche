package com.casesmod.network.packets;

import com.casesmod.data.CurrencyManager;
import com.casesmod.data.FishPriceCalculator;
import com.casesmod.network.NetworkHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S Packet to sell fish from hand or full inventory for Dubloons.
 */
public class C2SSellFishPacket {

    private final boolean sellHandOnly;

    public C2SSellFishPacket(boolean sellHandOnly) {
        this.sellHandOnly = sellHandOnly;
    }

    public static void encode(C2SSellFishPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.sellHandOnly);
    }

    public static C2SSellFishPacket decode(FriendlyByteBuf buf) {
        return new C2SSellFishPacket(buf.readBoolean());
    }

    public static void handle(C2SSellFishPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            long totalEarned = 0L;
            int fishCount = 0;
            boolean hasGolden = false;

            if (packet.sellHandOnly) {
                ItemStack handStack = player.getMainHandItem();
                FishPriceCalculator.PriceResult result = FishPriceCalculator.calculatePrice(handStack);
                if (result.isFish && result.finalPrice > 0) {
                    totalEarned += result.finalPrice;
                    fishCount += handStack.getCount();
                    if (result.isGolden) hasGolden = true;
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                }
            } else {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    FishPriceCalculator.PriceResult result = FishPriceCalculator.calculatePrice(stack);
                    if (result.isFish && result.finalPrice > 0) {
                        totalEarned += result.finalPrice;
                        fishCount += stack.getCount();
                        if (result.isGolden) hasGolden = true;
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                }
            }

            if (totalEarned > 0) {
                CurrencyManager.INSTANCE.add(player.getUUID(), totalEarned);
                long newBalance = CurrencyManager.INSTANCE.getBalance(player.getUUID());
                NetworkHandler.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                        new BalanceSyncS2CPacket(newBalance));

                if (hasGolden) {
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.literal("★ ")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                            .append(Component.literal("[Рынок Рыбы] ")
                                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                            .append(Component.literal("Продано " + fishCount + " рыб (включая Золотой Трофей!) на сумму ")
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal("+" + totalEarned + " Дублонов")
                                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                            .append(Component.literal("! Новый баланс: ")
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(newBalance + " 💰")
                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
                } else {
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
                    player.sendSystemMessage(Component.literal("[Рынок Рыбы] ")
                            .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                            .append(Component.literal("Успешно продано " + fishCount + " рыб на сумму ")
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal("+" + totalEarned + " Дублонов")
                                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                            .append(Component.literal("! Ваш баланс: ")
                                    .withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(newBalance + " 💰")
                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
                }
            } else {
                player.sendSystemMessage(Component.literal("[Рынок Рыбы] ")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                        .append(Component.literal("В " + (packet.sellHandOnly ? "руке" : "инвентаре") + " нет улова для продажи.")
                                .withStyle(ChatFormatting.RED)));
            }
        });
        ctx.setPacketHandled(true);
    }
}
