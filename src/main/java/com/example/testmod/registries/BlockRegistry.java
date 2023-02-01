package com.example.testmod.registries;


import com.example.testmod.TestMod;
import com.example.testmod.block.BloodCauldronBlock;
import com.example.testmod.block.BloodSlashBlock;
import com.example.testmod.block.inscription_table.InscriptionTableBlock;
import com.example.testmod.block.inscription_table.InscriptionTableTile;
import com.example.testmod.block.pedestal.PedestalBlock;
import com.example.testmod.block.pedestal.PedestalTile;
import com.example.testmod.block.scroll_forge.ScrollForgeBlock;
import com.example.testmod.block.scroll_forge.ScrollForgeTile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TestMod.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TestMod.MODID);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }

    public static final RegistryObject<Block> INSCRIPTION_TABLE_BLOCK = BLOCKS.register("inscription_table", InscriptionTableBlock::new);
    public static final RegistryObject<Block> SCROLL_FORGE_BLOCK = BLOCKS.register("scroll_forge", ScrollForgeBlock::new);
    public static final RegistryObject<Block> BLOOD_SLASH_BLOCK = BLOCKS.register("blood_slash", BloodSlashBlock::new);
    public static final RegistryObject<Block> PEDESTAL_BLOCK = BLOCKS.register("pedestal", PedestalBlock::new);
    public static final RegistryObject<Block> BLOOD_CAULDRON_BLOCK = BLOCKS.register("blood_cauldron", BloodCauldronBlock::new);

    public static final RegistryObject<BlockEntityType<InscriptionTableTile>> INSCRIPTION_TABLE_TILE = BLOCK_ENTITIES.register("inscription_table", () -> BlockEntityType.Builder.of(InscriptionTableTile::new, INSCRIPTION_TABLE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<ScrollForgeTile>> SCROLL_FORGE_TILE = BLOCK_ENTITIES.register("scroll_forge", () -> BlockEntityType.Builder.of(ScrollForgeTile::new, SCROLL_FORGE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<PedestalTile>> PEDESTAL_TILE = BLOCK_ENTITIES.register("pedestal", () -> BlockEntityType.Builder.of(PedestalTile::new, PEDESTAL_BLOCK.get()).build(null));
}
