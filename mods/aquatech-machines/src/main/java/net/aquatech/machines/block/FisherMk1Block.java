package net.aquatech.machines.block;

import net.aquatech.machines.block.entity.BaseMachineBlockEntity;
import net.aquatech.machines.block.entity.FisherMk1BlockEntity;
import net.aquatech.machines.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Рыболов MK-1: тот же блок и анимация, что у MK-2, но с упрощённой начинкой. */
public class FisherMk1Block extends FisherBlock {

    public FisherMk1Block(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FisherMk1BlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.FISHER_MK1.get(),
                (l, p, s, be) -> BaseMachineBlockEntity.serverTick(be));
    }
}
