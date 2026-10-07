package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.entity.BeeKeeperEntity;
import net.aquatech.ui.entity.FishMerchantEntity;
import net.aquatech.ui.entity.FishNeighborEntity;
import net.aquatech.ui.entity.OldLeyEntity;
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

    public static final RegistryObject<EntityType<BeeKeeperEntity>> BEE_KEEPER = ENTITY_TYPES.register(
            "bee_keeper",
            () -> EntityType.Builder.of(BeeKeeperEntity::new, MobCategory.MISC)
                    .sized(0.8F, 2.0F)
                    .clientTrackingRange(8)
                    .build("bee_keeper"));

    public static final RegistryObject<EntityType<FishNeighborEntity>> FISH_NEIGHBOR = ENTITY_TYPES.register(
            "fish_neighbor",
            () -> EntityType.Builder.of(FishNeighborEntity::new, MobCategory.MISC)
                    .sized(1.6F, 1.7F)
                    .clientTrackingRange(10)
                    .build("fish_neighbor"));

    public static final RegistryObject<EntityType<OldLeyEntity>> OLD_LEY = ENTITY_TYPES.register(
            "old_ley",
            () -> EntityType.Builder.of(OldLeyEntity::new, MobCategory.MISC)
                    .sized(2.4F, 1.2F)
                    .clientTrackingRange(12)
                    .noSave()
                    .build("old_ley"));

    private ModEntities() {
    }

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
        bus.addListener(ModEntities::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(FISH_MERCHANT.get(), FishMerchantEntity.createAttributes().build());
        event.put(BEE_KEEPER.get(), BeeKeeperEntity.createAttributes().build());
        event.put(FISH_NEIGHBOR.get(), FishNeighborEntity.createAttributes().build());
        event.put(OLD_LEY.get(), OldLeyEntity.createAttributes().build());
    }
}
