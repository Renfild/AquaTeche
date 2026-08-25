package net.aquatech.ui.common;

import net.aquatech.ui.util.UtfSafe;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record PlayerProfile(
        UUID uuid,
        String name,
        String rankId,
        String rankDisplay,
        int rankWeight,
        int ping,
        boolean staff,
        boolean afk,
        boolean inWater,
        int waterDepth,
        int effectivePressure,
        long coins
) {
    public static PlayerProfile read(FriendlyByteBuf buf) {
        return new PlayerProfile(
                buf.readUUID(),
                buf.readUtf(64),
                buf.readUtf(32),
                buf.readUtf(96),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarLong()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        UtfSafe.write(buf, name, 64);
        UtfSafe.write(buf, rankId, 32);
        UtfSafe.write(buf, rankDisplay, 96);
        buf.writeVarInt(rankWeight);
        buf.writeVarInt(ping);
        buf.writeBoolean(staff);
        buf.writeBoolean(afk);
        buf.writeBoolean(inWater);
        buf.writeVarInt(waterDepth);
        buf.writeVarInt(effectivePressure);
        buf.writeVarLong(coins);
    }
}
