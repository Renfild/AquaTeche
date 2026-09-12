package net.aquatech.machines.registry;

import net.aquatech.machines.AquaTechMachinesMod;
import net.aquatech.machines.block.ExcavatorBlock;
import net.aquatech.machines.block.ExtractorBlock;
import net.aquatech.machines.block.FisherBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AquaTechMachinesMod.MOD_ID);

    public static final RegistryObject<Block> FISHER = BLOCKS.register("fisher",
            () -> new FisherBlock(metal().lightLevel(s -> 6)));

    public static final RegistryObject<Block> EXCAVATOR = BLOCKS.register("excavator",
            () -> new ExcavatorBlock(metal().lightLevel(s -> 6)));

    public static final RegistryObject<Block> EXTRACTOR = BLOCKS.register("extractor",
            () -> new ExtractorBlock(metal().lightLevel(s -> 6)));

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion();
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
