package net.aquatech.ui.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

/** Retired decorative block. Registry kept so old worlds do not lose the ID. */
public class AbyssalPortalBlock extends Block {

    public AbyssalPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(net.minecraft.world.level.block.state.BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
