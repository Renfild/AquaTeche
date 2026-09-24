package net.aquatech.machines.block;

import net.aquatech.machines.block.entity.BaseMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * База всех механизмов: при разрушении блока содержимое слотов выпадает в мир,
 * пока BlockEntity ещё жив. Без этого содержимое терялось при каждом break.
 */
public abstract class MachineBlock extends BaseEntityBlock {

    protected MachineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof BaseMachineBlockEntity machine) {
                machine.dropContents();
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
