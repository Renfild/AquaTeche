package net.aquatech.ui.network;

import net.aquatech.ui.capability.AquaSkillCapability;
import net.aquatech.ui.capability.SkillTreeData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class C2SUnlockSkillPacket {

    private final String skillId;

    public C2SUnlockSkillPacket(String skillId) {
        this.skillId = skillId;
    }

    public C2SUnlockSkillPacket(FriendlyByteBuf buf) {
        this.skillId = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(skillId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                player.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
                    if (!SkillTreeData.canUnlock(skillId, cap.getUnlockedSkills())) {
                        return;
                    }
                    if (cap.getSkillPoints() < SkillTreeData.costOf(skillId)) {
                        return;
                    }
                    if (cap.unlockSkill(skillId)) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.2F);
                        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncSkillsPacket(cap));
                    }
                });
            }
        });
        return true;
    }
}
