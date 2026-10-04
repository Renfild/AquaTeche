package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.FishMerchantEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AquaTechUI.MOD_ID);

    public static final RegistryObject<EntityType<FishMerchantEntity>> FISH_MERCHANT = ENTITY_TYPES.register(
            "fish_merchant",
            () -> EntityType.Builder.of(FishMerchantEntity::new, MobCategory.MISC)
                    .sized(0.8F, 2.0F)
                    .clientTrackingRange(8)
                    .build("fish_merchant"));

    private ModEntities() {
    }

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
        bus.addListener(ModEntities::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(FISH_MERCHANT.get(), FishMerchantEntity.createAttributes().build());
    }
}
