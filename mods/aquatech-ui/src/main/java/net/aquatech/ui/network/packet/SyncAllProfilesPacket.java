package net.aquatech.ui.network.packet;

import net.aquatech.ui.client.ClientUiState;
import net.aquatech.ui.common.PlayerProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record SyncAllProfilesPacket(List<PlayerProfile> profiles) {
    public static void encode(SyncAllProfilesPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.profiles.size());
        for (PlayerProfile profile : packet.profiles) {
            profile.write(buf);
        }
    }

    public static SyncAllProfilesPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<PlayerProfile> profiles = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            profiles.add(PlayerProfile.read(buf));
        }
        return new SyncAllProfilesPacket(profiles);
    }

    public static void handle(SyncAllProfilesPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientUiState.updateProfiles(packet.profiles));
        ctx.get().setPacketHandled(true);
    }
}
