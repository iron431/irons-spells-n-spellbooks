package io.redspace.ironsspellbooks.worldgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.StructureElementRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IndividualTerrainStructurePoolElement extends SinglePoolElement {

    public static final Codec<IndividualTerrainStructurePoolElement> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec(), TerrainAdjustment.CODEC.optionalFieldOf("terrain_adjustment").forGetter(element -> {
            return Optional.ofNullable(element.terrainAdjustment);
        })).apply(instance, (either, processorListHolder, projection, liquidSettings, terrainAdjustment) -> {
            return new IndividualTerrainStructurePoolElement(either, processorListHolder, projection, liquidSettings, terrainAdjustment.orElse(null));
        });
    });

    private final @Nullable TerrainAdjustment terrainAdjustment;

    public IndividualTerrainStructurePoolElement(Either<ResourceLocation, StructureTemplate> resourceLocation, Holder<StructureProcessorList> processors, StructureTemplatePool.Projection projection, Optional<LiquidSettings> liquidSettings, @Nullable TerrainAdjustment terrainAdjustment) {
        super(resourceLocation, processors, projection, liquidSettings);
        this.terrainAdjustment = terrainAdjustment;
    }

    public TerrainAdjustment getTerrainAdjustment() {
        return this.terrainAdjustment != null ? this.terrainAdjustment : TerrainAdjustment.NONE;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructureElementRegistry.INDIVIDUAL_TERRAIN_ELEMENT.get();
    }
}
