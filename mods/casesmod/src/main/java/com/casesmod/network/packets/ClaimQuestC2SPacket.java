package com.casesmod.network.packets;

import com.casesmod.data.QuestDefinition;
import com.casesmod.data.QuestManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClaimQuestC2SPacket {
    private final String questId;
    public ClaimQuestC2SPacket(String questId) { this.questId = questId; }

    public static void encode(ClaimQuestC2SPacket msg, FriendlyByteBuf buf) { buf.writeUtf(msg.questId); }
    public static ClaimQuestC2SPacket decode(FriendlyByteBuf buf) { return new ClaimQuestC2SPacket(buf.readUtf()); }

    public static void handle(ClaimQuestC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            QuestDefinition q = QuestManager.INSTANCE.get(msg.questId);
            if (q == null) return;

            if (QuestManager.INSTANCE.isClaimed(player.getUUID(), q.id)) return;
            if (!QuestManager.INSTANCE.isComplete(player.getUUID(), q)) {
                player.sendSystemMessage(Component.literal("§cКвест ещё не выполнен!"));
                return;
            }

            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(q.rewardItemId));
            ItemStack stack = new ItemStack(item, q.rewardCount);
            if (!player.getInventory().add(stack)) player.drop(stack, false);

            if (q.rewardCommand != null && !q.rewardCommand.isEmpty()) {
                String cmd = q.rewardCommand.replace("%player%", player.getGameProfile().getName());
                player.getServer().getCommands().performPrefixedCommand(
                        player.getServer().createCommandSourceStack(), cmd);
            }

            QuestManager.INSTANCE.markClaimed(player.getUUID(), q.id);
            player.sendSystemMessage(Component.literal("§aНаграда за квест получена: " + q.displayName));
        });
        ctx.get().setPacketHandled(true);
    }
}
