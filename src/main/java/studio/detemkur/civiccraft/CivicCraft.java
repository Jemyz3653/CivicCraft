package studio.detemkur.civiccraft;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.function.Function;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class CivicCraft implements ModInitializer {
    public static final String ID = "civiccraft";
    public static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    public static BlockEntityType<FurnitureStorage> STORAGE;
    public static EntityType<Seat> SEAT;
    private static final List<String> WOODS = List.of("oak", "spruce", "birch", "jungle", "acacia",
            "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped", "pale_oak", "poplar");
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ID, path); }

    @Override public void onInitialize() {
        var storageBlocks = new ArrayList<Block>();
        for (String wood : WOODS) {
            for (FurnitureKind kind : FurnitureKind.values()) {
                var properties = Properties.of().strength(1.5F, 3.0F).sound(SoundType.WOOD).noOcclusion();
                if (!wood.equals("crimson") && !wood.equals("warped")) properties.ignitedByLava();
                Block block = register(wood + "_" + kind.path, properties,
                        p -> kind.storage ? new StorageBlock(kind, p) : new FurnitureBlock(kind, p));
                if (kind.storage) storageBlocks.add(block);
            }
        }
        for (String color : List.of("red", "blue", "black", "white")) {
            register(color + "_museum_stanchion", Properties.of().strength(1.5F).sound(SoundType.METAL).noOcclusion(),
                    StanchionBlock::new);
        }
        STORAGE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("storage"),
                FabricBlockEntityTypeBuilder.create(FurnitureStorage::new, storageBlocks.toArray(Block[]::new)).build());
        var seatKey = ResourceKey.create(Registries.ENTITY_TYPE, id("seat"));
        SEAT = Registry.register(BuiltInRegistries.ENTITY_TYPE, seatKey,
                EntityType.Builder.<Seat>of(Seat::new, MobCategory.MISC).sized(0.1F, 0.1F)
                        .passengerAttachments(0F).clientTrackingRange(6).updateInterval(20)
                        .noSummon().build(seatKey));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("furniture"), FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.civiccraft"))
                .icon(() -> new ItemStack(BLOCKS.get("oak_chair")))
                .displayItems((parameters, entries) -> BLOCKS.values().forEach(entries::accept))
                .build());
    }

    private static Block register(String name, Properties properties, Function<Properties, Block> factory) {
        var key = ResourceKey.create(Registries.BLOCK, id(name));
        Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
        var itemKey = ResourceKey.create(Registries.ITEM, id(name));
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        BLOCKS.put(name, block);
        return block;
    }
}
