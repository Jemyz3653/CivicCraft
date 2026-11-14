package studio.detemkur.civiccraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FurnitureBlock extends Block {
    static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    final FurnitureKind kind;

    FurnitureBlock(FurnitureKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return kind.shapes.get(state.getValue(FACING));
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                          Player player, BlockHitResult hit) {
        if (kind.seatHeight == 0 || player.isShiftKeyDown() || player.isPassenger()) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) return InteractionResult.PASS;
            if (!server.getEntitiesOfClass(Seat.class, new AABB(pos), Seat::isVehicle).isEmpty()) return InteractionResult.PASS;
            Seat seat = CivicCraft.SEAT.create(server, EntitySpawnReason.TRIGGERED);
            if (seat == null) return InteractionResult.PASS;
            seat.setPos(pos.getX()+0.5, pos.getY()+kind.seatHeight/16.0, pos.getZ()+0.5);
            seat.setYRot(state.getValue(FACING).toYRot());
            if (!server.addFreshEntity(seat)) return InteractionResult.PASS;
            if (!player.startRiding(seat, true, true)) {
                seat.discard();
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.SUCCESS;
    }
}
