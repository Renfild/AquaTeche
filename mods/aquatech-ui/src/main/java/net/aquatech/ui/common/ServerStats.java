package net.aquatech.ui.common;

import net.aquatech.ui.util.UtfSafe;
import net.minecraft.network.FriendlyByteBuf;

public record ServerStats(
        int online,
        int maxPlayers,
        int staffOnline,
        float tps,
        String serverName,
        String domain
) {
    public static ServerStats read(FriendlyByteBuf buf) {
        return new ServerStats(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readUtf(64),
                buf.readUtf(64)
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(online);
        buf.writeVarInt(maxPlayers);
        buf.writeVarInt(staffOnline);
        buf.writeFloat(tps);
        UtfSafe.write(buf, serverName, 64);
        UtfSafe.write(buf, domain, 64);
    }
}
