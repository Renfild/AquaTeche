package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.block.entity.KeepnetBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AquaTechUI.MOD_ID);

    public static final RegistryObject<BlockEntityType<KeepnetBlockEntity>> FISH_KEEPNET = BLOCK_ENTITIES.register("fish_keepnet",
            () -> BlockEntityType.Builder.of(KeepnetBlockEntity::new, ModBlocks.FISH_KEEPNET.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
