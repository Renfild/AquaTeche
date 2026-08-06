package com.casesmod.network.packets;

import com.casesmod.data.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Клиент просит сервер открыть кейс. Сервер:
 *  1) проверяет, что кейс сейчас доступен (сезонное окно availableFrom/availableUntil);
 *  2) списывает стоимость из внутриигровой валюты;
 *  3) крутит рулетку по весам — с учётом pity-системы (гарантия редкого приза после N неудач подряд);
 *  4) выдаёт приз в инвентарь (или на землю, если инвентарь полон);
 *  5) шлёт результат обратно клиенту для анимации и обновляет ленту последних выигрышей.
 */
public class OpenCaseC2SPacket {
    private final String caseId;

    public OpenCaseC2SPacket(String caseId) { this.caseId = caseId; }

    public static void encode(OpenCaseC2SPacket msg, FriendlyByteBuf buf) { buf.writeUtf(msg.caseId); }
    public static OpenCaseC2SPacket decode(FriendlyByteBuf buf) { return new OpenCaseC2SPacket(buf.readUtf()); }

    public static void handle(OpenCaseC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            CaseDefinition def = CaseManager.INSTANCE.get(msg.caseId);
            if (def == null || def.items.isEmpty()) return;

            if (!CaseManager.INSTANCE.isAvailable(def)) {
                player.sendSystemMessage(Component.literal("§cЭтот кейс сейчас недоступен (сезонное ограничение)."));
                return;
            }

            // Сначала тратим выданный/купленный кейс; иначе списываем валюту (или бесплатно при price=0).
            PlayerAccount acc = PlayerAccountManager.INSTANCE.get(player.getUUID());
            boolean usedOwned = acc.consumeCase(msg.caseId);
            if (usedOwned) {
                PlayerAccountManager.INSTANCE.save(player.getUUID());
            } else if (def.price > 0 && !CurrencyManager.INSTANCE.tryCharge(player.getUUID(), def.price)) {
                player.sendSystemMessage(Component.literal(
                        "§cНет кейса и недостаточно средств! Нужно §f" + def.price + "§c, у вас §f"
                                + CurrencyManager.INSTANCE.getBalance(player.getUUID())));
                return;
            }

            CaseItem won = rollWithPity(def, player);
            if (won == null) return;

            if (won.itemId != null && !won.itemId.isEmpty() && !won.itemId.equals("minecraft:air") && !won.itemId.equals("air")) {
                ResourceLocation itemLoc = new ResourceLocation(won.itemId);
                Item item = BuiltInRegistries.ITEM.get(itemLoc);
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    ItemStack reward = new ItemStack(item, won.count);
                    if (!player.getInventory().add(reward)) {
                        player.drop(reward, false);
                    }
                }
            }
            if (won.command != null && !won.command.isEmpty()) {
                String cmd = won.command.replace("%player%", player.getGameProfile().getName());
                player.getServer().getCommands().performPrefixedCommand(
                        player.getServer().createCommandSourceStack(), cmd);
            }

            long newBalance = CurrencyManager.INSTANCE.getBalance(player.getUUID());
            com.casesmod.network.NetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new CaseResultS2CPacket(msg.caseId, won.itemId, won.count, won.rarity, won.displayName, newBalance));
            // Обновляем счётчик «У вас» на клиенте.
            com.casesmod.network.NetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    CaseListSyncS2CPacket.buildForPlayer(player));

            com.casesmod.data.RecentWinsManager.INSTANCE.addWin(
                    player.getGameProfile().getName(), won.displayName, won.rarity);
            com.casesmod.network.NetworkHandler.broadcastRecentWins(player.getServer());
        });
        ctx.get().setPacketHandled(true);
    }

    /** Обычный roll(), либо гарантированный roll с минимальной редкостью, если pity-порог достигнут. */
    private static CaseItem rollWithPity(CaseDefinition def, ServerPlayer player) {
        Random random = new Random();
        if (def.pityThreshold <= 0) {
            return CaseManager.INSTANCE.roll(def, random);
        }

        CaseItem.Rarity pityRarity;
        try { pityRarity = CaseItem.Rarity.valueOf(def.pityRarity.toUpperCase()); }
        catch (Exception e) { pityRarity = CaseItem.Rarity.EPIC; }

        int count = PityManager.INSTANCE.getCount(player.getUUID(), def.id);
        CaseItem won;
        if (count >= def.pityThreshold - 1) {
            won = CaseManager.INSTANCE.rollWithMinRarity(def, random, pityRarity);
        } else {
            won = CaseManager.INSTANCE.roll(def, random);
        }
        if (won == null) return null;

        if (won.rarityEnum().ordinal() >= pityRarity.ordinal()) {
            PityManager.INSTANCE.reset(player.getUUID(), def.id);
        } else {
            PityManager.INSTANCE.increment(player.getUUID(), def.id);
        }
        return won;
    }
}
