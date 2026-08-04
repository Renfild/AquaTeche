package com.casesmod.network.packets;

import com.casesmod.data.KitDefinition;
import com.casesmod.data.KitManager;
import com.casesmod.util.PermissionsHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClaimKitC2SPacket {
    private final String kitId;
    public ClaimKitC2SPacket(String kitId) { this.kitId = kitId; }

    public static void encode(ClaimKitC2SPacket msg, FriendlyByteBuf buf) { buf.writeUtf(msg.kitId); }
    public static ClaimKitC2SPacket decode(FriendlyByteBuf buf) { return new ClaimKitC2SPacket(buf.readUtf()); }

    public static void handle(ClaimKitC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            KitDefinition kit = KitManager.INSTANCE.get(msg.kitId);
            if (kit == null) return;

            if (!kit.permission.isEmpty() && !PermissionsHelper.hasPermission(player, kit.permission)) {
                player.sendSystemMessage(Component.literal(
                        "§cУ вас нет прав на этот набор (требуется: §f" + kit.permission + "§c)."));
                return;
            }

            long remain = KitManager.INSTANCE.secondsUntilAvailable(player.getUUID(), kit);
            if (remain > 0) {
                player.sendSystemMessage(Component.literal("§cПодождите ещё " + remain + " сек. до следующего кита!"));
                return;
            }

            for (KitDefinition.KitItem ki : kit.items) {
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(ki.itemId));
                ItemStack stack = new ItemStack(item, ki.count);
                if (!player.getInventory().add(stack)) player.drop(stack, false);
            }
            KitManager.INSTANCE.markClaimed(player.getUUID(), kit);
            player.sendSystemMessage(Component.literal("§aВы получили набор: " + kit.displayName));
        });
        ctx.get().setPacketHandled(true);
    }
}
