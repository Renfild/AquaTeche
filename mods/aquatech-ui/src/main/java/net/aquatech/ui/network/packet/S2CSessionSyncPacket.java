package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.ClientUiState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C Packet to sync web-validated session token from server to client.
 * Sent after Mohist/Forge validates the session with aquateche.store on join.
 */
public class S2CSessionSyncPacket {

    private final String nick;
    private final String sessionId;
    private final int balance;
    private final String rankId;

    public S2CSessionSyncPacket(String nick, String sessionId, int balance, String rankId) {
        this.nick = nick;
        this.sessionId = sessionId;
        this.balance = balance;
        this.rankId = rankId != null ? rankId : "player";
    }

    public S2CSessionSyncPacket(FriendlyByteBuf buf) {
        this.nick = buf.readUtf(64);
        this.sessionId = buf.readUtf(256);
        this.balance = buf.readVarInt();
        this.rankId = buf.readUtf(32);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.nick, 64);
        buf.writeUtf(this.sessionId, 256);
        buf.writeVarInt(this.balance);
        buf.writeUtf(this.rankId, 32);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientUiState.setSession(sessionId, balance, rankId);
        }));
        ctx.setPacketHandled(true);
    }
}
