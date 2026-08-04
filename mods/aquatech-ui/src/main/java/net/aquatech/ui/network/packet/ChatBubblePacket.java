package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.bubble.ChatBubbleManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record ChatBubblePacket(UUID sender, String message, int durationTicks) {
    public static void encode(ChatBubblePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.sender);
        net.aquatech.ui.util.UtfSafe.write(buf, packet.message, 256);
        buf.writeVarInt(packet.durationTicks);
    }

    public static ChatBubblePacket decode(FriendlyByteBuf buf) {
        return new ChatBubblePacket(buf.readUUID(), buf.readUtf(256), buf.readVarInt());
    }

    public static void handle(ChatBubblePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChatBubbleManager.addBubble(packet.sender, packet.message, packet.durationTicks));
        ctx.get().setPacketHandled(true);
    }
}
