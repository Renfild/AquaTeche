package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.block.AbyssalPortalBlock;
import net.aquatech.ui.block.AutoFisherBlock;
import net.aquatech.ui.block.HydroReactorBlock;
import net.aquatech.ui.block.OceanAltarBlock;
import net.aquatech.ui.block.OceanFilterBlock;
import net.aquatech.ui.block.SeabedDredgerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AquaTechUI.MOD_ID);

    public static final RegistryObject<Block> AUTO_FISHER = BLOCKS.register("auto_fisher",
            () -> new AutoFisherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> OCEAN_FILTER = BLOCKS.register("ocean_filter",
            () -> new OceanFilterBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> SEABED_DREDGER = BLOCKS.register("seabed_dredger",
            () -> new SeabedDredgerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> HYDRO_REACTOR = BLOCKS.register("hydro_reactor",
            () -> new HydroReactorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion().lightLevel(state -> 8)));

    public static final RegistryObject<Block> OCEAN_ALTAR = BLOCKS.register("ocean_altar",
            () -> new OceanAltarBlock(BlockBehaviour.Properties.copy(Blocks.PRISMARINE).noOcclusion().lightLevel(state -> 6)));

    public static final RegistryObject<Block> ABYSSAL_PORTAL = BLOCKS.register("abyssal_portal",
            () -> new AbyssalPortalBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).noOcclusion().lightLevel(state -> 10)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
