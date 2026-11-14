package studio.detemkur.civiccraft;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class StanchionBlock extends Block {
    private static final Map<Direction, BooleanProperty> SIDES = Map.of(
            Direction.NORTH, BlockStateProperties.NORTH, Direction.EAST, BlockStateProperties.EAST,
            Direction.SOUTH, BlockStateProperties.SOUTH, Direction.WEST, BlockStateProperties.WEST);
    private static final VoxelShape POST = Shapes.or(box(4,0,4,12,2,12), box(6,2,6,10,15,10));
    private static final Map<Direction,VoxelShape> ROPES = Shapes.rotateHorizontal(box(7.25,10.5,0,8.75,13.25,8));
    private static final Map<Direction,VoxelShape> BARRIERS = Shapes.rotateHorizontal(box(7,0,0,9,24,8));
    private final Map<BlockState,VoxelShape> outlines = new HashMap<>();
    private final Map<BlockState,VoxelShape> collisions = new HashMap<>();

    StanchionBlock(Properties properties) {
        super(properties);
        var initial = stateDefinition.any();
        for (var property : SIDES.values()) initial = initial.setValue(property, false);
        registerDefaultState(initial);
        for (var state : stateDefinition.getPossibleStates()) {
            VoxelShape outline = POST;
            VoxelShape collision = POST;
            for (var side : SIDES.entrySet()) {
                if (!state.getValue(side.getValue())) continue;
                outline = Shapes.or(outline, ROPES.get(side.getKey()));
                collision = Shapes.or(collision, BARRIERS.get(side.getKey()));
            }
            outlines.put(state, outline.optimize());
            collisions.put(state, collision.optimize());
        }
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
        builder.add(SIDES.values().toArray(BooleanProperty[]::new));
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = defaultBlockState();
        for (var side : SIDES.entrySet()) {
            state = state.setValue(side.getValue(), context.getLevel().getBlockState(context.getClickedPos().relative(side.getKey())).is(this));
        }
        return state;
    }

    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                              BlockPos pos, Direction direction, BlockPos neighborPos,
                                              BlockState neighbor, RandomSource random) {
        var property = SIDES.get(direction);
        return property == null ? state : state.setValue(property, neighbor.is(this));
    }

    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        var result = state;
        for (var side : SIDES.entrySet()) result = result.setValue(SIDES.get(rotation.rotate(side.getKey())), state.getValue(side.getValue()));
        return result;
    }

    @Override protected BlockState mirror(BlockState state, Mirror mirror) {
        var result = state;
        for (var side : SIDES.entrySet()) result = result.setValue(SIDES.get(mirror.mirror(side.getKey())), state.getValue(side.getValue()));
        return result;
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outlines == null ? POST : outlines.getOrDefault(state, POST);
    }

    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return collisions == null ? POST : collisions.getOrDefault(state, POST);
    }
}
