package net.aquatech.ui.network;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.network.packet.ChatBubblePacket;
import net.aquatech.ui.network.packet.SyncAllProfilesPacket;
import net.aquatech.ui.network.packet.SyncServerStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "6";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AquaTechUI.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        // HUD / TAB profile sync (S2C)
        CHANNEL.messageBuilder(SyncAllProfilesPacket.class, id())
                .encoder(SyncAllProfilesPacket::encode)
                .decoder(SyncAllProfilesPacket::decode)
                .consumerMainThread(SyncAllProfilesPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncServerStatsPacket.class, id())
                .encoder(SyncServerStatsPacket::encode)
                .decoder(SyncServerStatsPacket::decode)
                .consumerMainThread(SyncServerStatsPacket::handle)
                .add();

        CHANNEL.messageBuilder(ChatBubblePacket.class, id())
                .encoder(ChatBubblePacket::encode)
                .decoder(ChatBubblePacket::decode)
                .consumerMainThread(ChatBubblePacket::handle)
                .add();

        // Skill tree packets
        CHANNEL.messageBuilder(S2CSyncSkillsPacket.class, id())
                .encoder(S2CSyncSkillsPacket::toBytes)
                .decoder(S2CSyncSkillsPacket::new)
                .consumerMainThread(S2CSyncSkillsPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SUnlockSkillPacket.class, id())
                .encoder(C2SUnlockSkillPacket::toBytes)
                .decoder(C2SUnlockSkillPacket::new)
                .consumerMainThread(C2SUnlockSkillPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SOpenSkillTreePacket.class, id())
                .encoder(C2SOpenSkillTreePacket::toBytes)
                .decoder(C2SOpenSkillTreePacket::new)
                .consumerMainThread(C2SOpenSkillTreePacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CStartRhythmHookPacket.class, id())
                .encoder(S2CStartRhythmHookPacket::toBytes)
                .decoder(S2CStartRhythmHookPacket::new)
                .consumerMainThread(S2CStartRhythmHookPacket::handle)
                .add();

        CHANNEL.messageBuilder(C2SRhythmHookResultPacket.class, id())
                .encoder(C2SRhythmHookResultPacket::toBytes)
                .decoder(C2SRhythmHookResultPacket::new)
                .consumerMainThread(C2SRhythmHookResultPacket::handle)
                .add();
    }

    public static void sendToAll(Object packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}
