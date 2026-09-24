package net.aquatech.machines.block;

import net.aquatech.machines.block.entity.ExcavatorBlockEntity;
import net.aquatech.machines.block.entity.BaseMachineBlockEntity;
import net.aquatech.machines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class ExcavatorBlock extends MachineBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ExcavatorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, Boolean.FALSE);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, net.minecraft.world.level.Level level,
                                                      BlockPos pos, net.minecraft.world.entity.player.Player player,
                                                      net.minecraft.world.InteractionHand hand,
                                                      net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BaseMachineBlockEntity machine) {
                net.minecraftforge.network.NetworkHooks.openScreen(
                        (net.minecraft.server.level.ServerPlayer) player, machine, pos);
            }
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExcavatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level,
                                                                  BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.EXCAVATOR.get(),
                (l, p, s, be) -> BaseMachineBlockEntity.serverTick(be));
    }

    @Override
    public void animateTick(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!state.getValue(LIT)) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        if (random.nextDouble() < 0.3) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.POOF,
                    x + (random.nextDouble() - 0.5) * 0.4, y - 0.2, z + (random.nextDouble() - 0.5) * 0.4,
                    0.0, -0.02, 0.0);
        }
        if (random.nextDouble() < 0.2) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIT,
                    x + (random.nextDouble() - 0.5) * 0.4, y + 0.1, z + (random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.05, 0.0);
        }
    }
}
