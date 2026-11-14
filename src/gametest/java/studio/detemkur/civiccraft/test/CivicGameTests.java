package studio.detemkur.civiccraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import studio.detemkur.civiccraft.CivicCraft;
import studio.detemkur.civiccraft.FurnitureStorage;

public final class CivicGameTests {
    private static final BlockPos CENTER = new BlockPos(1, 1, 1);

    @GameTest public void allFurnitureRecipesLoad(GameTestHelper test) {
        test.assertTrue(CivicCraft.BLOCKS.size() == 342, "Incorrect count of furniture variants");
        for (String id : CivicCraft.BLOCKS.keySet()) {
            var key = ResourceKey.create(Registries.RECIPE, CivicCraft.id(id));
            test.assertTrue(test.getLevel().recipeAccess().byKey(key).isPresent(), "Missing recipe " + id);
        }
        test.succeed();
    }

    @GameTest public void ropeConnectsAndDetaches(GameTestHelper test) {
        var redPost = CivicCraft.BLOCKS.get("red_museum_stanchion");
        var east = CENTER.east();
        test.setBlock(CENTER, redPost);
        test.setBlock(east, redPost);
        test.assertBlockProperty(CENTER, BlockStateProperties.EAST, true);
        test.assertBlockProperty(east, BlockStateProperties.WEST, true);
        test.setBlock(east, CivicCraft.BLOCKS.get("blue_museum_stanchion"));
        test.assertBlockProperty(CENTER, BlockStateProperties.EAST, false);
        test.setBlock(east, redPost);
        test.setBlock(east, Blocks.AIR);
        test.assertBlockProperty(CENTER, BlockStateProperties.EAST, false);
        test.succeed();
    }

    @GameTest public void storagePersistsAndDropsContents(GameTestHelper test) {
        test.setBlock(CENTER, CivicCraft.BLOCKS.get("oak_display_case"));
        var level = test.getLevel();
        var pos = test.absolutePos(CENTER);
        var storage = (FurnitureStorage) level.getBlockEntity(pos);
        test.assertTrue(storage != null && storage.getContainerSize() == 27, "Display case has no inventory");
        storage.setItem(0, new ItemStack(Items.APPLE, 7));
        storage.setItem(26, new ItemStack(Items.DIAMOND, 4));
        var persisted = storage.saveWithoutMetadata(level.registryAccess());
        var visible = storage.getUpdateTag(level.registryAccess());
        storage.clearContent();
        storage.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), visible));
        test.assertTrue(storage.getItem(0).getCount() == 1 && storage.getItem(26).isEmpty(), "Excess slots synchronized");
        storage.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), persisted));
        test.assertTrue(storage.getItem(0).getCount() == 7 && storage.getItem(26).getCount() == 4, "Lost stored items");
        test.setBlock(CENTER, Blocks.AIR);
        test.assertItemEntityCountIs(Items.APPLE, CENTER, 2, 7);
        test.assertItemEntityCountIs(Items.DIAMOND, CENTER, 2, 4);
        test.succeed();
    }

    @GameTest public void chairsMountAndRelease(GameTestHelper test) {
        test.setBlock(CENTER, CivicCraft.BLOCKS.get("oak_chair"));
        var player = test.makeMockServerPlayerInLevel();
        var level = test.getLevel();
        var pos = test.absolutePos(CENTER);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
        player.setPos(Vec3.atBottomCenterOf(pos.north()));
        level.getBlockState(pos).useWithoutItem(level, player, hit);
        test.assertTrue(player.isPassenger(), "Chair failed to mount player");
        test.setBlock(CENTER, Blocks.AIR);
        test.runAfterDelay(2, () -> {
            test.assertTrue(!player.isPassenger(), "Removing chair left player mounted");
            test.assertEntityNotPresent(CivicCraft.SEAT);
            test.succeed();
        });
    }
}
