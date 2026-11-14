package studio.detemkur.civiccraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.ProblemReporter;

public final class FurnitureStorage extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    FurnitureStorage(BlockPos pos, BlockState state) { super(CivicCraft.STORAGE, pos, state); }

    public FurnitureKind kind() { return ((FurnitureBlock)getBlockState().getBlock()).kind; }
    @Override public int getContainerSize() { return items.size(); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return getBlockState().getBlock().getName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(27, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) Containers.dropContents(level, pos, this);
        super.preRemoveSideEffects(pos, state);
    }

    @Override public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide() && kind().displaySlots > 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var visible = NonNullList.withSize(27, ItemStack.EMPTY);
        for (int i = 0; i < kind().displaySlots; i++) visible.set(i, items.get(i).copyWithCount(1));
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        ContainerHelper.saveAllItems(output, visible);
        return output.buildResult();
    }

    @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return kind().displaySlots == 0 ? null : ClientboundBlockEntityDataPacket.create(this);
    }
}
