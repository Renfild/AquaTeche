package com.casesmod.event;

import com.casesmod.CasesMod;
import com.casesmod.network.NetworkHandler;
import com.casesmod.network.packets.OpenFishMarketS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Opens Fish Market when a player right-clicks a vendor NPC.
 * Match: scoreboard tag {@code fish_market} OR custom name containing
 * "рыб" / "рынок" / "fish market" / "fish trader" (case-insensitive).
 */
@Mod.EventBusSubscriber(modid = CasesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FishMarketNpcInteract {
    private FishMarketNpcInteract() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        if (event.getLevel().isClientSide()) return;
        Entity target = event.getTarget();
        if (!isFishVendor(target)) return;

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                new OpenFishMarketS2CPacket());
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    public static boolean isFishVendor(Entity entity) {
        if (entity == null) return false;
        if (entity.getTags().contains("fish_market") || entity.getTags().contains("aquatech_fish_market")) {
            return true;
        }
        Component name = entity.getCustomName();
        if (name == null) return false;
        String s = name.getString().toLowerCase();
        // strip formatting codes
        s = s.replaceAll("§.", "");
        return s.contains("рыб") || s.contains("рынок") || s.contains("fish market")
                || s.contains("fish trader") || (s.contains("торгов") && s.contains("рыб"));
    }
}
