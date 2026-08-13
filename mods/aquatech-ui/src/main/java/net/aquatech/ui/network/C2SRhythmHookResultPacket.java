package net.aquatech.ui.network;

import net.aquatech.ui.fishing.RhythmHookSession;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C2S: Rhythm Hook mini-game finished. */
public class C2SRhythmHookResultPacket {

    private final boolean success;
    private final int quality;
    private final int hitsGreen;
    private final int hitsYellow;

    public C2SRhythmHookResultPacket(boolean success, int quality, int hitsGreen, int hitsYellow) {
        this.success = success;
        this.quality = quality;
        this.hitsGreen = hitsGreen;
        this.hitsYellow = hitsYellow;
    }

    public C2SRhythmHookResultPacket(FriendlyByteBuf buf) {
        this.success = buf.readBoolean();
        this.quality = buf.readVarInt();
        this.hitsGreen = buf.readVarInt();
        this.hitsYellow = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(success);
        buf.writeVarInt(quality);
        buf.writeVarInt(hitsGreen);
        buf.writeVarInt(hitsYellow);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            RhythmHookSession.complete(player, success, quality, hitsGreen, hitsYellow);
        });
        return true;
    }
}
