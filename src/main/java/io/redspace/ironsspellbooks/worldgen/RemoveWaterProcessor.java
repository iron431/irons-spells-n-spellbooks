package io.redspace.ironsspellbooks.worldgen;

//TODO: 1.21: this is now a vanilla process
//public class RemoveWaterProcessor extends StructureProcessor {
//
//    public static final Codec<RemoveWaterProcessor> CODEC = Codec.unit(RemoveWaterProcessor::new);
//
//    public RemoveWaterProcessor() {
//
//    }
//
//    @Nullable
//    @Override
//    public StructureTemplate.StructureBlockInfo process(@Nonnull LevelReader level, @Nonnull BlockPos jigsawPiecePos, @Nonnull BlockPos jigsawPieceBottomCenterPos, @Nonnull StructureTemplate.StructureBlockInfo blockInfoLocal, @Nonnull StructureTemplate.StructureBlockInfo blockInfoGlobal, @Nonnull StructurePlaceSettings settings, @Nullable StructureTemplate template) {
//        if (blockInfoGlobal.state().hasProperty(BlockStateProperties.WATERLOGGED) && !blockInfoGlobal.state().getValue(BlockStateProperties.WATERLOGGED)) {
//            ChunkPos chunkPos = new ChunkPos(blockInfoGlobal.pos());
//            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
//            int sectionIndex = chunk.getSectionIndex(blockInfoGlobal.pos().getY());
//
//            // if section index is < 0 we are out of bounds
//            if (sectionIndex >= 0) {
//                LevelChunkSection section = chunk.getSection(sectionIndex);
//                // if we are waterlogged, reset us to our original state
//                if (this.getFluidState(section, blockInfoGlobal.pos()).is(FluidTags.WATER)) {
//                    this.setBlock(section, blockInfoGlobal.pos(), blockInfoGlobal.state());
//                }
//            }
//        }
//
//        return blockInfoGlobal;
//    }
//
//    private void setBlock(LevelChunkSection section, BlockPos pos, BlockState state) {
//        section.setBlockState(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()), state);
//    }
//
//    private FluidState getFluidState(LevelChunkSection section, BlockPos pos) {
//        return section.getFluidState(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()));
//    }
//
//    @Nonnull
//    @Override
//    protected StructureProcessorType<?> getType() {
//        return StructureProcessorRegistry.REMOVE_WATER.get();
//    }
//}