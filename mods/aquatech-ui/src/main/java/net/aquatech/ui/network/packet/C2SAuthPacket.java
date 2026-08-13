package net.aquatech.ui.network.packet;

import net.aquatech.ui.server.auth.ServerAuthTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SAuthPacket {
    private final String nick;
    private final String sessionToken;

    public C2SAuthPacket(String nick, String sessionToken) {
        this.nick = nick != null ? nick : "";
        this.sessionToken = sessionToken != null ? sessionToken : "";
    }

    public C2SAuthPacket(FriendlyByteBuf buf) {
        this.nick = buf.readUtf(64);
        this.sessionToken = buf.readUtf(256);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.nick, 64);
        buf.writeUtf(this.sessionToken, 256);
    }

    public static void handle(C2SAuthPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                ServerAuthTracker.onAuthPacketReceived(player, msg.nick, msg.sessionToken);
            }
        });
        ctx.setPacketHandled(true);
    }
}
