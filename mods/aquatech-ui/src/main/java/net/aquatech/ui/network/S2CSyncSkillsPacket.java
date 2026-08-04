package net.aquatech.ui.network;

import net.aquatech.ui.capability.AquaSkillCapability;
import net.aquatech.ui.client.ClientItemActions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncSkillsPacket {

    private final CompoundTag tag;

    public S2CSyncSkillsPacket(AquaSkillCapability capability) {
        this.tag = capability.serializeNBT();
    }

    public S2CSyncSkillsPacket(FriendlyByteBuf buf) {
        this.tag = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        if (tag == null) {
            return true;
        }
        NetworkEvent.Context ctx = supplier.get();
        CompoundTag synced = tag;
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientItemActions.applySyncedSkills(synced)));
        return true;
    }
}
