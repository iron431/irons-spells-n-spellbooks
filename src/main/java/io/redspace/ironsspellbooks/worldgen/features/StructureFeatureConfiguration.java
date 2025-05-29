package io.redspace.ironsspellbooks.worldgen.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record StructureFeatureConfiguration(ResourceLocation structureTemplateLocation, int xsize, int ysize,
                                            int zsize, BlockPos offset) implements FeatureConfiguration {
    public static final Codec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("structure_piece").forGetter(StructureFeatureConfiguration::structureTemplateLocation),
            Codec.intRange(1, 16).fieldOf("x_size").forGetter(StructureFeatureConfiguration::xsize),
            Codec.intRange(1, 16).fieldOf("y_size").forGetter(StructureFeatureConfiguration::ysize),
            Codec.intRange(1, 16).fieldOf("z_size").forGetter(StructureFeatureConfiguration::zsize),
            BlockPos.CODEC.fieldOf("offset").forGetter(StructureFeatureConfiguration::offset)
    ).apply(builder, StructureFeatureConfiguration::new));
}