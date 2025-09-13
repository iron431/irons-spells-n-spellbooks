package io.redspace.ironsspellbooks.worldgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.StructureElementRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IndividualTerrainStructurePoolElement extends SinglePoolElement {

    public static final Codec<IndividualTerrainStructurePoolElement> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(templateCodec(), processorsCodec(), projectionCodec(), TerrainAdjustment.CODEC.optionalFieldOf("terrain_adjustment").forGetter(element -> {
            return Optional.ofNullable(element.terrainAdjustment);
        })).apply(instance, (either, processorListHolder, projection, terrainAdjustment) -> {
            return new IndividualTerrainStructurePoolElement(either, processorListHolder, projection, terrainAdjustment.orElse(null));
        });
    });

    private final @Nullable TerrainAdjustment terrainAdjustment;

    public IndividualTerrainStructurePoolElement(Either<ResourceLocation, StructureTemplate> resourceLocation, Holder<StructureProcessorList> processors, StructureTemplatePool.Projection projection, @Nullable TerrainAdjustment terrainAdjustment) {
        super(resourceLocation, processors, projection);
        this.terrainAdjustment = terrainAdjustment;
    }

    @Override
    public boolean place(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, BlockPos p_227306_, BlockPos p_227307_, Rotation pRotation, BoundingBox pBox, RandomSource pRandom, boolean p_227311_) {
        IronsSpellbooks.LOGGER.debug("IndividualTerrainStructurePoolElement.place: {}", p_227306_);
        return super.place(pStructureTemplateManager, pLevel, pStructureManager, pGenerator, p_227306_, p_227307_, pRotation, pBox, pRandom, p_227311_);
    }

    public TerrainAdjustment getTerrainAdjustment() {
        return this.terrainAdjustment != null ? this.terrainAdjustment : TerrainAdjustment.NONE;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructureElementRegistry.INDIVIDUAL_TERRAIN_ELEMENT.get();
    }
}
