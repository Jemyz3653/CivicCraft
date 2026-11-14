package studio.detemkur.civiccraft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

final class StorageBlock extends FurnitureBlock implements EntityBlock {
    StorageBlock(FurnitureKind kind, Properties properties) { super(kind, properties); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FurnitureStorage(pos, state);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                          Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FurnitureStorage storage) {
            player.openMenu(storage);
        }
        return InteractionResult.SUCCESS;
    }
}
