package store.aquateche.aqualumen.common.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Товары пчеловода (id вида «bee.*» в server_shop.json) продаёт только он сам, а не общее меню F4. */
public final class BeeKeeperProximity {

    public static final String OFFER_PREFIX = "bee.";
    public static final String TAB = "bees";

    private static final ResourceLocation KEEPER_ID = new ResourceLocation("aquatech_ui", "bee_keeper");

    private BeeKeeperProximity() {
    }

    public static boolean isBeeOffer(String offerId) {
        return offerId != null && offerId.toLowerCase(java.util.Locale.ROOT).startsWith(OFFER_PREFIX);
    }

    public static boolean isNear(Entity player) {
        return FishMerchantProximity.isNear(player, KEEPER_ID);
    }

    public static boolean allowPurchase(ServerPlayer player) {
        if (isNear(player)) {
            return true;
        }
        player.sendSystemMessage(Component.literal("[AquaTech] Пчёл и пасечные товары продаёт только пчеловод на спавне.")
                .withStyle(ChatFormatting.RED));
        return false;
    }
}
