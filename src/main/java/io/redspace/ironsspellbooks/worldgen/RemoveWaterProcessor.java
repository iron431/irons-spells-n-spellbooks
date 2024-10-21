package io.redspace.ironsspellbooks.worldgen;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.registries.StructureProcessorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class RemoveWaterProcessor extends StructureProcessor {

    public static final Codec<RemoveWaterProcessor> CODEC = Codec.unit(RemoveWaterProcessor::new);

    public RemoveWaterProcessor() {

    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(@Nonnull LevelReader level, @Nonnull BlockPos jigsawPiecePos, @Nonnull BlockPos jigsawPieceBottomCenterPos, @Nonnull StructureTemplate.StructureBlockInfo blockInfoLocal, @Nonnull StructureTemplate.StructureBlockInfo blockInfoGlobal, @Nonnull StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        if (blockInfoGlobal.state().hasProperty(BlockStateProperties.WATERLOGGED) && !blockInfoGlobal.state().getValue(BlockStateProperties.WATERLOGGED)) {
            ChunkPos chunkPos = new ChunkPos(blockInfoGlobal.pos());
            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
            int sectionIndex = chunk.getSectionIndex(blockInfoGlobal.pos().getY());

            // index can return -1
            if (sectionIndex >= 0  && sectionIndex < chunk.getSections().length) {
                LevelChunkSection section = chunk.getSection(sectionIndex);
                // if we are waterlogged, reset us to our original state
                if (this.getFluidState(section, blockInfoGlobal.pos()).is(FluidTags.WATER)) {
                    this.setBlock(section, blockInfoGlobal.pos(), blockInfoGlobal.state());
                }
            }
        }

        return blockInfoGlobal;
    }

    private void setBlock(LevelChunkSection section, BlockPos pos, BlockState state) {
        section.setBlockState(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()), state);
    }

    private FluidState getFluidState(LevelChunkSection section, BlockPos pos) {
        return section.getFluidState(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()));
    }

    @Nonnull
    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorRegistry.REMOVE_WATER.get();
    }
}