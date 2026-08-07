package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.block.entity.AutoFisherBlockEntity;
import net.aquatech.ui.block.entity.OceanFilterBlockEntity;
import net.aquatech.ui.block.entity.SeabedDredgerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AquaTechUI.MOD_ID);

    public static final RegistryObject<BlockEntityType<AutoFisherBlockEntity>> AUTO_FISHER = BLOCK_ENTITIES.register("auto_fisher",
            () -> BlockEntityType.Builder.of(AutoFisherBlockEntity::new, ModBlocks.AUTO_FISHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<OceanFilterBlockEntity>> OCEAN_FILTER = BLOCK_ENTITIES.register("ocean_filter",
            () -> BlockEntityType.Builder.of(OceanFilterBlockEntity::new, ModBlocks.OCEAN_FILTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<SeabedDredgerBlockEntity>> SEABED_DREDGER = BLOCK_ENTITIES.register("seabed_dredger",
            () -> BlockEntityType.Builder.of(SeabedDredgerBlockEntity::new, ModBlocks.SEABED_DREDGER.get()).build(null));

    public static final RegistryObject<BlockEntityType<net.aquatech.ui.block.entity.OceanAltarBlockEntity>> OCEAN_ALTAR = BLOCK_ENTITIES.register("ocean_altar",
            () -> BlockEntityType.Builder.of(net.aquatech.ui.block.entity.OceanAltarBlockEntity::new, ModBlocks.OCEAN_ALTAR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
