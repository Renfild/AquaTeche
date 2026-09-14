package net.aquatech.machines.registry;

import net.aquatech.machines.block.entity.FisherBlockEntity;
import net.aquatech.machines.block.entity.ExcavatorBlockEntity;
import net.aquatech.machines.block.entity.ExtractorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "aquatech_machines");

    public static final RegistryObject<BlockEntityType<FisherBlockEntity>> FISHER =
            BLOCK_ENTITIES.register("fisher",
                    () -> BlockEntityType.Builder.of(FisherBlockEntity::new, ModBlocks.FISHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ExcavatorBlockEntity>> EXCAVATOR =
            BLOCK_ENTITIES.register("excavator",
                    () -> BlockEntityType.Builder.of(ExcavatorBlockEntity::new, ModBlocks.EXCAVATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<ExtractorBlockEntity>> EXTRACTOR =
            BLOCK_ENTITIES.register("extractor",
                    () -> BlockEntityType.Builder.of(ExtractorBlockEntity::new, ModBlocks.EXTRACTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<net.aquatech.machines.block.entity.SynthesizerBlockEntity>> SYNTHESIZER =
            BLOCK_ENTITIES.register("synthesizer",
                    () -> BlockEntityType.Builder.of(net.aquatech.machines.block.entity.SynthesizerBlockEntity::new, ModBlocks.SYNTHESIZER.get()).build(null));

    public static final RegistryObject<BlockEntityType<net.aquatech.machines.block.entity.CentrifugeBlockEntity>> CENTRIFUGE =
            BLOCK_ENTITIES.register("centrifuge",
                    () -> BlockEntityType.Builder.of(net.aquatech.machines.block.entity.CentrifugeBlockEntity::new, ModBlocks.CENTRIFUGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<net.aquatech.machines.block.entity.FlowerCollectorBlockEntity>> FLOWER_COLLECTOR =
            BLOCK_ENTITIES.register("flower_collector",
                    () -> BlockEntityType.Builder.of(net.aquatech.machines.block.entity.FlowerCollectorBlockEntity::new, ModBlocks.FLOWER_COLLECTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<net.aquatech.machines.block.entity.ManaFabricatorBlockEntity>> MANA_FABRICATOR =
            BLOCK_ENTITIES.register("mana_fabricator",
                    () -> BlockEntityType.Builder.of(net.aquatech.machines.block.entity.ManaFabricatorBlockEntity::new, ModBlocks.MANA_FABRICATOR.get()).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
