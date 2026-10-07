package net.aquatech.ui.registry;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.block.FamePlaqueBlock;
import net.aquatech.ui.block.KeepnetBlock;
import net.aquatech.ui.block.KeepnetTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AquaTechUI.MOD_ID);

    public static final RegistryObject<Block> FISH_KEEPNET = BLOCKS.register("fish_keepnet",
            () -> new KeepnetBlock(KeepnetTier.T1, MapColor.WOOD, SoundType.WOOD, 2.5F));
    public static final RegistryObject<Block> FISH_KEEPNET_T2 = BLOCKS.register("fish_keepnet_t2",
            () -> new KeepnetBlock(KeepnetTier.T2, MapColor.COLOR_ORANGE, SoundType.METAL, 3.5F));
    public static final RegistryObject<Block> FISH_KEEPNET_T3 = BLOCKS.register("fish_keepnet_t3",
            () -> new KeepnetBlock(KeepnetTier.T3, MapColor.GOLD, SoundType.METAL, 4.5F));
    public static final RegistryObject<Block> FISH_KEEPNET_T4 = BLOCKS.register("fish_keepnet_t4",
            () -> new KeepnetBlock(KeepnetTier.T4, MapColor.DIAMOND, SoundType.METAL, 5.5F));
    public static final RegistryObject<Block> FISH_KEEPNET_T5 = BLOCKS.register("fish_keepnet_t5",
            () -> new KeepnetBlock(KeepnetTier.T5, MapColor.EMERALD, SoundType.METAL, 6.5F));
    public static final RegistryObject<Block> FISH_KEEPNET_T6 = BLOCKS.register("fish_keepnet_t6",
            () -> new KeepnetBlock(KeepnetTier.T6, MapColor.COLOR_BLACK, SoundType.METAL, 7.5F));

    public static final RegistryObject<Block> FAME_PLAQUE = BLOCKS.register("fame_plaque", FamePlaqueBlock::new);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
