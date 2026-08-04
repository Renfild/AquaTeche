package net.aquatech.ui.capability;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = AquaTechUI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(AquaSkillCapability.INSTANCE).isPresent()) {
                event.addCapability(
                        new ResourceLocation(AquaTechUI.MOD_ID, "aqua_skills"),
                        new AquaSkillProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        // Carry skills across respawn/dimension changes so the tree never resets.
        event.getOriginal().getCapability(AquaSkillCapability.INSTANCE).ifPresent(oldCap ->
                event.getEntity().getCapability(AquaSkillCapability.INSTANCE).ifPresent(newCap ->
                        newCap.copyFrom(oldCap)));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        serverPlayer.getCapability(AquaSkillCapability.INSTANCE).ifPresent(cap -> {
            // 1. Sync skill tree state to client on every login.
            net.aquatech.ui.network.NetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new net.aquatech.ui.network.S2CSyncSkillsPacket(cap));

            // 2. Fail-safe starter kit — granted exactly once if the player has not
            //    received it yet (guards against the Essentials kit: tools bug and
            //    re-grants after a server migration).
            if (!cap.isStarterKitReceived()) {
                grantStarterKit(serverPlayer);
                cap.markStarterKitReceived();
                // Re-sync so the client sees the updated flag immediately.
                net.aquatech.ui.network.NetworkHandler.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new net.aquatech.ui.network.S2CSyncSkillsPacket(cap));
            }
        });
    }

    // -------------------------------------------------------------------------
    // Starter kit contents (mirrors server/plugins/Essentials/kits.yml)
    // -------------------------------------------------------------------------
    private static void grantStarterKit(ServerPlayer player) {
        List<ItemStack> items = new ArrayList<>();
        // Light once-kit: SB starter_inventory already gives IU tools + rod.
        // Only ensure guide book is present for first login (dedupe with SB).
        items.add(new ItemStack(ModItems.OCEAN_GUIDE_BOOK.get()));

        for (ItemStack stack : items) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }
}
