package studio.detemkur.civiccraft.test;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import studio.detemkur.civiccraft.Seat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import studio.detemkur.civiccraft.CivicCraft;
import studio.detemkur.civiccraft.FurnitureStorage;

public final class CivicClientTest implements FabricClientGameTest {
    private static Block block(String id) { return BuiltInRegistries.BLOCK.getValue(Identifier.parse(id)); }

    private static BlockState coloredPot(String color, String plant) {
        var pot = block("coloredm:potted_" + plant);
        for (var property : pot.getStateDefinition().getProperties()) {
            if (property.getName().equals("color")) {
                return setColor(pot.defaultBlockState(), property, color);
            }
        }
        throw new AssertionError("No color property for coloredm:potted_" + plant);
    }

    private static <T extends Comparable<T>> BlockState setColor(BlockState state,
            net.minecraft.world.level.block.state.properties.Property<T> property, String color) {
        return state.setValue(property, property.getValue(color).orElseThrow());
    }

    @Override public void runTest(ClientGameTestContext context) {
        var loader = FabricLoader.getInstance();
        boolean bloom = loader.isModLoaded("bloomdepth");
        boolean colored = loader.isModLoaded("coloredm");
        context.runOnClient(mc -> {
            var models = mc.getModelManager().getBlockStateModelSet();
            int checked = 0;
            for (var block : CivicCraft.BLOCKS.values()) {
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    if (models.get(state) == models.missingModel()) throw new AssertionError("Missing furniture model " + state);
                    checked++;
                }

            }
            System.out.println("CIVICCRAFT: checked " + checked + " furniture states.");
            if (bloom && colored) {
                int checkedPots = 0;
                for (String color : List.of("white","orange","magenta","light_blue","yellow","lime","pink","gray",
                        "light_gray","cyan","purple","blue","brown","green","red","black")) {
                    for (String plant : List.of("dandelion","golden_dandelion","poppy","blue_orchid","allium","azure_bluet",
                            "red_tulip","orange_tulip","white_tulip","pink_tulip","oxeye_daisy","cornflower",
                            "lily_of_the_valley","wither_rose","torchflower","open_eyeblossom","closed_eyeblossom",
                            "flowering_azalea_bush","mangrove_propagule")) {
                        var pot = coloredPot(color, plant);
                        var model = models.get(pot);
                        if (model == models.missingModel()) throw new AssertionError("Missing colored pot " + color + " " + plant);
                        var parts = new ArrayList<BlockStateModelPart>();
                        model.collectParts(RandomSource.create(1), parts);
                        int quads = parts.stream().mapToInt(part -> part.getQuads(null).size()).sum();
                        if (quads < 35) throw new AssertionError("Flat flower geometry for " + color + " " + plant + ": " + quads);
                        String potTexture = model.particleMaterial().sprite().contents().name().toString();
                        if (!potTexture.equals("coloredm:block/" + color + "_flower_pot")) {
                            throw new AssertionError("Wrong pot texture " + potTexture);
                        }
                        boolean hasPetals = parts.stream().flatMap(part -> part.getQuads(null).stream()).anyMatch(
                                quad -> quad.materialInfo().sprite().contents().name().getNamespace().equals("bloomdepth"));
                        if (!hasPetals) throw new AssertionError("No BloomDepth textures in colored pot");
                        checkedPots++;
                    }
                }
                System.out.println("BLOOMDEPTH: checked " + checkedPots + " ColoredM pots with 3D geometry.");
            }
        });
        try (var world = context.worldBuilder().create()) {
            var server = world.getServer();
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamemode creative @a");
            server.runCommand("fill -5 64 -3 20 64 20 minecraft:smooth_quartz");
            server.runCommand("fill -5 65 -3 20 75 20 minecraft:air");
            server.runCommand("tp @a 5.5 68 1 0 23");
            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                String[] objects = {"oak_chair", "oak_table", "spruce_cabinet", "spruce_bookcase",
                        "cherry_sofa", "birch_market_stand", "birch_display_case", "oak_waiting_seat", "pale_oak_wall_shelf",
                        "spruce_dresser", "oak_kitchen_unit", "dark_oak_writing_desk", "mangrove_display_rack", "poplar_cafe_table"};
                for (int i = 0; i < objects.length; i++) {
                    var pos = new BlockPos(1+(i%5)*2, 65, 5+(i/5)*3);
                    level.setBlockAndUpdate(pos, CivicCraft.BLOCKS.get(objects[i]).defaultBlockState());
                    if (level.getBlockEntity(pos) instanceof FurnitureStorage storage) {
                        storage.setItem(0, new ItemStack(Items.APPLE));
                        storage.setItem(1, new ItemStack(Items.BREAD));
                        storage.setItem(2, new ItemStack(Items.EMERALD));
                        storage.setItem(3, new ItemStack(Items.BOOK));
                        storage.setItem(4, new ItemStack(Items.GOLDEN_APPLE));
                    }
                }
                for (int x = 3; x <= 8; x++) level.setBlockAndUpdate(new BlockPos(x,65,12),
                        CivicCraft.BLOCKS.get("red_museum_stanchion").defaultBlockState());
                if (bloom) {
                    for (int i = 0; i < 8; i++) {
                        String[] plants = {"poppy","allium","pink_tulip","blue_orchid","sunflower","rose_bush","dandelion","lily_of_the_valley"};
                        var pos = new BlockPos(i*2,65,14);
                        level.setBlockAndUpdate(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState());
                        var state = block("minecraft:" + plants[i]).defaultBlockState();
                        level.setBlockAndUpdate(pos, state);
                        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                            level.setBlockAndUpdate(pos.above(), state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
                        }
                        if (colored && (i < 4 || i > 5)) {
                            level.setBlockAndUpdate(new BlockPos(i*2,65,13),
                                    coloredPot(List.of("red","blue","yellow","cyan","white","black","lime","purple").get(i), plants[i]));
                        }
                    }
                }
            });
            world.getConnection().waitForChunksRender();
            context.waitTicks(10);
            context.runOnClient(mc -> {
                for (var block : CivicCraft.BLOCKS.values()) {
                    var rendered = new ItemStackRenderState();
                    mc.getItemModelResolver().updateForTopItem(rendered, new ItemStack(block), ItemDisplayContext.GUI, null, null, 0);
                    var material = rendered.pickParticleMaterial(RandomSource.create(1));
                    if (rendered.isEmpty() || material == null || material.sprite().contents().name().getPath().equals("missingno")) {
                        throw new AssertionError("Missing furniture item model " + block);
                    }
                }
                System.out.println("CIVICCRAFT: checked 342 inventory models.");
            });
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            context.waitTicks(2);
            context.takeScreenshot("civiccraft-furniture");
            server.runCommand("tp @a 5.5 67.4 4 0 23");
            world.getConnection().waitForChunksRender();
            context.waitTicks(6);
            context.takeScreenshot("civiccraft-1.0.0");
            if (bloom) {
                server.runCommand("tp @a 7 67.5 7.6 0 20");
                world.getConnection().waitForChunksRender();
                context.waitTicks(8);
                context.takeScreenshot("bloomdepth-colored-pots");
            }
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var pos = new BlockPos(9,65,5);
                var level = s.overworld();
                level.getBlockState(pos).useWithoutItem(level, player,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
            });
            context.waitFor(mc -> mc.player != null && mc.player.getVehicle() instanceof Seat, 100);
            server.runCommand("setblock 9 65 5 minecraft:air");
            context.waitFor(mc -> mc.player != null && !mc.player.isPassenger(), 100);
            System.out.println("CIVICCRAFT: real client mounted and dismounted.");
        }
    }
}
