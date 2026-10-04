package store.aquateche.aqualumen.common.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Рыбу скупает только торговец на спавне. Мод aquatech_ui с торговцем и этот мод не связаны на этапе компиляции,
 * поэтому торговца ищем по id типа сущности.
 */
public final class FishMerchantProximity {

    private static final ResourceLocation MERCHANT_ID = new ResourceLocation("aquatech_ui", "fish_merchant");
    /** Окно торговца остаётся открытым, пока игрок рядом: чуть больше дистанции обычного взаимодействия. */
    public static final double RANGE_BLOCKS = 10.0;

    private FishMerchantProximity() {
    }

    /** Проверка продажи: рядом должен стоять торговец, иначе игроку приходит подсказка. */
    public static boolean allowSale(ServerPlayer player) {
        if (isNearMerchant(player)) {
            return true;
        }
        player.sendSystemMessage(Component.literal("[AquaTech] Рыбу можно продать только торговцу рыбой на спавне.")
                .withStyle(ChatFormatting.RED));
        return false;
    }

    static boolean isNearMerchant(ServerPlayer player) {
        return isNear(player);
    }

    /** Работает и на клиенте: игрок и мир те же, что видит сторона. Без мода торговца ограничения нет. */
    public static boolean isNear(Entity player) {
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(MERCHANT_ID)) {
            return true;
        }
        EntityType<?> merchant = ForgeRegistries.ENTITY_TYPES.getValue(MERCHANT_ID);
        return !player.level().getEntities((Entity) null, player.getBoundingBox().inflate(RANGE_BLOCKS),
                entity -> entity.getType() == merchant).isEmpty();
    }
}
