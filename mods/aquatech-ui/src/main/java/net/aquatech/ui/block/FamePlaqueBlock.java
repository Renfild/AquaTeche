package net.aquatech.ui.block;

import net.aquatech.ui.block.entity.FamePlaqueBlockEntity;
import net.aquatech.ui.fishing.FameLogic;
import net.aquatech.ui.fishing.FameService;
import net.aquatech.ui.fishing.FishCatchInfo;
import net.aquatech.ui.fishing.FishNames;
import net.aquatech.ui.fishing.FishWeight;
import net.aquatech.ui.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Табличка Стены славы: вешается на стену и показывает рекорд сервера по одному виду рыбы.
 * Вид привязывает оператор, нажав ПКМ рыбой нужного вида; остальные игроки по ПКМ видят подробности рекорда.
 */
public class FamePlaqueBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Глубина таблички в шестнадцатых блока: модель занимает 3.9 пикселя у стены. */
    private static final double DEPTH = 4.0;
    private static final VoxelShape NORTH = Block.box(0, 0, 16 - DEPTH, 16, 16, 16);
    private static final VoxelShape SOUTH = Block.box(0, 0, 0, 16, 16, DEPTH);
    private static final VoxelShape EAST = Block.box(0, 0, 0, DEPTH, 16, 16);
    private static final VoxelShape WEST = Block.box(16 - DEPTH, 0, 0, 16, 16, 16);

    public FamePlaqueBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3.0F).sound(SoundType.METAL).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis().isVertical()) {
            return null;
        }
        BlockState state = defaultBlockState().setValue(FACING, face);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FamePlaqueBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.FAME_PLAQUE.get(), FamePlaqueBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof FamePlaqueBlockEntity plaque)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean operator = player.hasPermissions(2);
        if (operator && !held.isEmpty()) {
            String species = FishCatchInfo.speciesId(held);
            if (FishWeight.isFish(species)) {
                plaque.bind(species);
                player.sendSystemMessage(Component.literal("§6[Стена славы] §fТабличка привязана: §b" + held.getHoverName().getString()));
                return InteractionResult.CONSUME;
            }
        }
        if (operator && held.isEmpty()) {
            if (player.isShiftKeyDown()) {
                plaque.bind("");
                player.sendSystemMessage(Component.literal("§6[Стена славы] §7Табличка очищена."));
            } else {
                int next = plaque.rank() >= FamePlaqueBlockEntity.MAX_RANK ? 1 : plaque.rank() + 1;
                plaque.setRank(next);
                player.sendSystemMessage(Component.literal("§6[Стена славы] §fАвтоматический режим: §bместо №" + next
                        + " §fсреди рекордов по весу. ПКМ пустой рукой переключает место, рыбой привязывает вид."));
            }
            return InteractionResult.CONSUME;
        }
        describe(player, plaque);
        return InteractionResult.CONSUME;
    }

    private static void describe(Player player, FamePlaqueBlockEntity plaque) {
        String species = plaque.species();
        if (species.isEmpty()) {
            player.sendSystemMessage(Component.literal(plaque.rank() > 0
                    ? "§6[Стена славы] §7Место №" + plaque.rank() + " пока свободно: нужно больше рекордов."
                    : "§6[Стена славы] §7Табличка пока пуста."));
            return;
        }
        var item = BuiltInRegistries.ITEM.get(new ResourceLocation(species));
        String fish = FishNames.strip(new ItemStack(item).getHoverName().getString());
        FameLogic.Record record = FameService.get(species);
        if (record == null) {
            player.sendSystemMessage(Component.literal("§6[Стена славы] §f" + fish + "§7: место свободно. Поймайте рыбу тяжелее среднего!"));
            return;
        }
        player.sendSystemMessage(Component.literal("§6[Стена славы] §f" + fish + "§7: §e" + record.name + " §7— "
                + FishWeight.format(record.grams) + ", " + record.cm + " см, " + record.date));
    }
}
