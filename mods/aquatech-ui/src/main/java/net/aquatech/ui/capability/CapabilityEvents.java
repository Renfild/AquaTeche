package net.aquatech.ui.capability;

import net.aquatech.ui.AquaTechUI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(OceanProgressCapability.INSTANCE).isPresent()) {
                event.addCapability(
                        new ResourceLocation(AquaTechUI.MOD_ID, "aqua_skills"),
                        new OceanProgressProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        // Carry ocean progress across respawn/dimension changes.
        event.getOriginal().getCapability(OceanProgressCapability.INSTANCE).ifPresent(oldCap ->
                event.getEntity().getCapability(OceanProgressCapability.INSTANCE).ifPresent(newCap ->
                        newCap.copyFrom(oldCap)));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        net.aquatech.ui.network.NetworkHandler.markJoined(serverPlayer);
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        server.tell(new TickTask(server.getTickCount() + net.aquatech.ui.network.NetworkHandler.LOGIN_READY_DELAY_TICKS, () -> {
            if (serverPlayer.hasDisconnected()) return;
            if (!net.aquatech.ui.network.NetworkHandler.canReceivePlayPackets(serverPlayer)) return;

            serverPlayer.getCapability(OceanProgressCapability.INSTANCE).ifPresent(cap -> {
                net.aquatech.ui.network.NetworkHandler.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new net.aquatech.ui.network.S2CSyncOceanProgressPacket(cap));

                if (!cap.isStarterKitReceived()) {
                    grantStarterKit(serverPlayer);
                    cap.markStarterKitReceived();
                    net.aquatech.ui.network.NetworkHandler.CHANNEL.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new net.aquatech.ui.network.S2CSyncOceanProgressPacket(cap));
                }
            });
        }));
    }

    // -------------------------------------------------------------------------
    // Starter kit contents (mirrors server/plugins/Essentials/kits.yml)
    // -------------------------------------------------------------------------
    private static void grantStarterKit(ServerPlayer player) {
        // Starter gear comes from Skyblock/Essentials kit + FTB quest book.
        // Ocean guide book removed from aquatech_ui.
    }
}
