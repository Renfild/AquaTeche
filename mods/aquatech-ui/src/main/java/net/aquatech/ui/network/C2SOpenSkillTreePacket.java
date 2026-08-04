package net.aquatech.ui.network;

import net.aquatech.ui.capability.AquaSkillCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class C2SOpenSkillTreePacket {

    public C2SOpenSkillTreePacket() {
    }

    public C2SOpenSkillTreePacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncSkillsPacket(cap));
                });
            }
        });
        return true;
    }
}
