package studio.detemkur.civiccraft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import studio.detemkur.civiccraft.CivicCraft;

public final class CivicCraftClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        EntityRendererRegistry.register(CivicCraft.SEAT, NoopRenderer::new);
        BlockEntityRendererRegistry.register(CivicCraft.STORAGE, ShelfRenderer::new);
    }
}
