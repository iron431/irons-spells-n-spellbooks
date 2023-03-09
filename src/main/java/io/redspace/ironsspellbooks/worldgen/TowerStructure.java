//package com.example.irons_spellbooks.worldgen;
//
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Holder;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.LevelHeightAccessor;
//import net.minecraft.world.level.NoiseColumn;
//import net.minecraft.world.level.levelgen.Heightmap;
//import net.minecraft.world.level.levelgen.LegacyRandomSource;
//import net.minecraft.world.level.levelgen.WorldgenRandom;
//import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
//import net.minecraft.world.level.levelgen.structure.Structure;
//import net.minecraft.world.level.levelgen.structure.StructureType;
//import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
//import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Optional;
//
//public class TowerStructure extends Structure {
//
//    public static final Codec<TowerStructure> CODEC = RecordCodecBuilder.<TowerStructure>mapCodec(instance ->
//            instance.group(Structure.settingsCodec(instance),
//                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
//                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
//                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
//                    HeightPFrovider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
//                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
//                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
//            ).apply(instance, PortalStructure::new)).codec();
//
//    private final Holder<StructureTemplatePool> startPool;
//    private final Optional<ResourceLocation> startJigsawName;
//    private final int size;
//    private final HeightProvider startHeight;
//    private final Optional<Heightmap.Types> projectStartToHeightmap;
//    private final int maxDistanceFromCenter;
//
//    public PortalStructure(Structure.StructureSettings config,
//                           Holder<StructureTemplatePool> startPool,
//                           Optional<ResourceLocation> startJigsawName,
//                           int size,
//                           HeightProvider startHeight,
//                           Optional<Heightmap.Types> projectStartToHeightmap,
//                           int maxDistanceFromCenter) {
//        super(config);
//        this.startPool = startPool;
//        this.startJigsawName = startJigsawName;
//        this.size = size;
//        this.startHeight = startHeight;
//        this.projectStartToHeightmap = projectStartToHeightmap;
//        this.maxDistanceFromCenter = maxDistanceFromCenter;
//    }
//
//    private static boolean extraSpawningChecks(Structure.GenerationContext context) {
//        // Grabs the chunk position we are at
//        ChunkPos chunkpos = context.chunkPos();
//
//        // Checks to make sure our structure does not spawn above land that's higher than y = 150
//        // to demonstrate how this method is good for checking extra conditions for spawning
//        return context.chunkGenerator().getFirstOccupiedHeight(
//                chunkpos.getMinBlockX(),
//                chunkpos.getMinBlockZ(),
//                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
//                context.heightAccessor(),
//                context.randomState()) < 150;
//    }
//
//    @Override
//    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
//        boolean overworld = !(context.chunkGenerator() instanceof MysteriousChunkGenerator);
//
//        // Check if the spot is valid for our structure. This is just as another method for cleanness.
//        // Returning an empty optional tells the game to skip this spot as it will not generate the structure.
//        if (!PortalStructure.extraSpawningChecks(context)) {
//            return Optional.empty();
//        }
//
//        // Turns the chunk coordinates into actual coordinates we can use. (center of that chunk)
//        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);
//
//        if (overworld) {
//            // If we are generating for the overworld we want our portal to spawn underground. Preferably in an open area
//            blockpos = findSuitableSpot(context, blockpos);
//        } else {
//            blockpos = blockpos.atY(40);
//        }
//
//        // Create a randomgenerator that depends on the current chunk location. That way if the world is recreated
//        // with the same seed the feature will end up at the same spot
//        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(context.seed()));
//        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
//
//        LevelHeightAccessor heightAccessor = context.heightAccessor();
//
//        // Pick a random y location between a low and a high point
//        int y = blockpos.getY() - 10;
//        int minY = heightAccessor.getMinBuildHeight() + 20;
//        if (y > minY) {
//            y = worldgenrandom.nextIntBetweenInclusive(minY, y);
//        }
//
//        // Go down until we find a spot that has air. Then go down until we find a spot that is solid again
//        NoiseColumn baseColumn = context.chunkGenerator().getBaseColumn(blockpos.getX(), blockpos.getZ(), heightAccessor, context.randomState());
//        int yy = y; // Remember 'y' because we will just use this if we can't find an air bubble
//        int lower = heightAccessor.getMinBuildHeight() + 3; // Lower limit, don't go below this
//        while (yy > lower && !baseColumn.getBlock(yy).isAir()) {
//            yy--;
//        }
//        // If we found air we go down until we find a non-air block
//        if (yy > lower) {
//            while (yy > lower && baseColumn.getBlock(yy).isAir()) {
//                yy--;
//            }
//            if (yy > lower) {
//                // We found a possible spawn spot
//                y = yy + 1;
//            }
//        }
//
//        // Grabs column of blocks at given position. In overworld, this column will be made of stone, water, and air.
//        // In nether, it will be netherrack, lava, and air. End will only be endstone and air. It depends on what block
//        // the chunk generator will place for that dimension.
//        ChunkPos chunkPos = context.chunkPos();
//        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), y, chunkPos.getMinBlockZ());
//
//        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
//        return JigsawPlacement.addPieces(
//                context, // Used for JigsawPlacement to get all the proper behaviors done.
//                this.startPool, // The starting pool to use to create the structure layout from
//                this.startJigsawName, // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
//                this.size, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
//                blockPos, // Where to spawn the structure.
//                false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
//                Optional.empty(), // Don't add the terrain height's y value to the passed in blockpos's y value
//                // Here, blockpos's y value is 60 which means the structure spawn 60 blocks above terrain height.
//                // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
//                // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
//                this.maxDistanceFromCenter); // Maximum
//    }
//
//    @NotNull
//    private static BlockPos findSuitableSpot(GenerationContext context, BlockPos blockpos) {
//        LevelHeightAccessor heightAccessor = context.heightAccessor();
//
//        // Get the top y location that is solid
//        int y = context.chunkGenerator().getBaseHeight(blockpos.getX(), blockpos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, context.randomState());
//
//        // Create a randomgenerator that depends on the current chunk location. That way if the world is recreated
//        // with the same seed the feature will end up at the same spot
//        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(context.seed()));
//        worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
//
//        // Pick a random y location between a low and a high point
//        y = worldgenrandom.nextIntBetweenInclusive(heightAccessor.getMinBuildHeight()+20, y - 10);
//
//        // Go down until we find a spot that has air. Then go down until we find a spot that is solid again
//        NoiseColumn baseColumn = context.chunkGenerator().getBaseColumn(blockpos.getX(), blockpos.getZ(), heightAccessor, context.randomState());
//        int yy = y; // Remember 'y' because we will just use this if we can't find an air bubble
//        int lower = heightAccessor.getMinBuildHeight() + 3; // Lower limit, don't go below this
//        while (yy > lower && !baseColumn.getBlock(yy).isAir()) {
//            yy--;
//        }
//        // If we found air we go down until we find a non-air block
//        if (yy > lower) {
//            while (yy > lower && baseColumn.getBlock(yy).isAir()) {
//                yy--;
//            }
//            if (yy > lower) {
//                // We found a possible spawn spot
//                y = yy + 1;
//            }
//        }
//
//        return blockpos.atY(y);
//    }
//
//    @Override
//    public StructureType<?> type() {
//        return Registration.PORTAL.get();
//    }
//}
