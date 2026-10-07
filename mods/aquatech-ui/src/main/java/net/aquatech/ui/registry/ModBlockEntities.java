package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.block.entity.FamePlaqueBlockEntity;
import net.aquatech.ui.block.entity.KeepnetBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AquaTechUI.MOD_ID);

    public static final RegistryObject<BlockEntityType<KeepnetBlockEntity>> FISH_KEEPNET = BLOCK_ENTITIES.register("fish_keepnet",
            () -> BlockEntityType.Builder.of(KeepnetBlockEntity::new,
                    ModBlocks.FISH_KEEPNET.get(), ModBlocks.FISH_KEEPNET_T2.get(), ModBlocks.FISH_KEEPNET_T3.get(),
                    ModBlocks.FISH_KEEPNET_T4.get(), ModBlocks.FISH_KEEPNET_T5.get(), ModBlocks.FISH_KEEPNET_T6.get()).build(null));

    public static final RegistryObject<BlockEntityType<FamePlaqueBlockEntity>> FAME_PLAQUE = BLOCK_ENTITIES.register("fame_plaque",
            () -> BlockEntityType.Builder.of(FamePlaqueBlockEntity::new, ModBlocks.FAME_PLAQUE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
