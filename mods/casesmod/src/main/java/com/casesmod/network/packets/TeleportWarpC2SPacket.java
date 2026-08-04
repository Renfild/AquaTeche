package com.casesmod.network.packets;

import com.casesmod.data.WarpDefinition;
import com.casesmod.data.WarpManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TeleportWarpC2SPacket {
    private final String warpId;
    public TeleportWarpC2SPacket(String warpId) { this.warpId = warpId; }

    public static void encode(TeleportWarpC2SPacket msg, FriendlyByteBuf buf) { buf.writeUtf(msg.warpId); }
    public static TeleportWarpC2SPacket decode(FriendlyByteBuf buf) { return new TeleportWarpC2SPacket(buf.readUtf()); }

    public static void handle(TeleportWarpC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            WarpDefinition warp = WarpManager.INSTANCE.get(msg.warpId);
            if (warp == null) return;

            ResourceLocation dimLoc = new ResourceLocation(warp.dimension);
            ServerLevel level = player.getServer().getLevel(
                    net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimLoc));
            if (level == null) {
                player.sendSystemMessage(Component.literal("§cИзмерение варпа не найдено!"));
                return;
            }
            player.teleportTo(level, warp.x + 0.5, warp.y, warp.z + 0.5, warp.yaw, warp.pitch);
            player.sendSystemMessage(Component.literal("§bТелепортация: " + warp.displayName));
        });
        ctx.get().setPacketHandled(true);
    }
}
