package studio.detemkur.civiccraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import studio.detemkur.civiccraft.FurnitureKind;
import studio.detemkur.civiccraft.FurnitureStorage;

final class ShelfRenderer implements BlockEntityRenderer<FurnitureStorage, ShelfRenderer.State> {
    private final BlockEntityRendererProvider.Context context;

    ShelfRenderer(BlockEntityRendererProvider.Context context) { this.context = context; }

    static final class State extends BlockEntityRenderState {
        final ItemStackRenderState[] items = new ItemStackRenderState[6];
        int visible;
        FurnitureKind kind;
        Direction facing;
        State() { for (int i = 0; i < items.length; i++) items[i] = new ItemStackRenderState(); }
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(FurnitureStorage block, State state, float partialTick,
                                              Vec3 camera, ModelFeatureRenderer.CrumblingOverlay crumble) {
        BlockEntityRenderer.super.extractRenderState(block, state, partialTick, camera, crumble);
        state.kind = block.kind();
        state.visible = state.kind.displaySlots;
        state.facing = block.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (int i = 0; i < state.visible; i++) {
            state.items[i].clear();
            var stack = block.getItem(i);
            if (!stack.isEmpty()) context.itemModelResolver().updateForTopItem(
                    state.items[i], stack, ItemDisplayContext.FIXED, block.getLevel(), null, i);
        }
    }

    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera) {
        if (state.visible == 0) return;
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.rotateDegrees(Axis.YP, 180F - state.facing.toYRot());
        for (int i = 0; i < state.visible; i++) {
            if (state.items[i].isEmpty()) continue;
            pose.pushPose();
            if (state.visible == 1) {
                switch (state.kind) {
                    case DISPLAY_CASE -> {
                        pose.translate(0, 0.52, 0);
                        pose.scale(0.53F, 0.53F, 0.53F);
                    }
                    case BOOK_STAND -> {
                        pose.translate(0, 0.92, -0.06);
                        pose.rotateDegrees(Axis.XP, 25F);
                        pose.scale(0.55F, 0.55F, 0.55F);
                    }
                    default -> {
                        pose.translate(0, 1.24, 0);
                        pose.scale(0.62F, 0.62F, 0.62F);
                    }
                }
            } else if (state.visible == 3) {
                double height = state.kind == FurnitureKind.WALL_SHELF ? 0.73 : 0.67;
                pose.translate(-0.26 + 0.26 * i, height, 0);
                pose.scale(0.36F, 0.36F, 0.36F);
            } else {
                pose.translate(-0.26 + 0.26 * (i % 3), 0.27 + 0.45 * (i / 3), -0.16);
                pose.scale(0.32F, 0.32F, 0.32F);
            }
            state.items[i].submit(pose, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        pose.popPose();
    }
}
