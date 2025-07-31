package io.redspace.ironsspellbooks.worldgen;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

public interface IExtendedNoiseChunk {

    record AquifierNuke(BoundingBox[] boundingBoxes){}

//            //todo: as long as this is stored-perchunk, i don't think it can be efficient to organize them into a data structure
//            (BoundingBox[] allBoundingBoxes, Map<ChunkPos, BoundingBox[]> byChunk) {
//        public AquifierNuke(List<BoundingBox> boundingBoxes) {
//            this(boundingBoxes.toArray(BoundingBox[]::new), )
//        }
//        private static Map<ChunkPos,BoundingBox[]> createChunkMap(List<BoundingBox> boundingBoxes){
//            Map<ChunkPos, List<BoundingBox>> builder = new HashMap<>();
//
//        }
//    }

    AquifierNuke irons_spellbooks$getAquifierStatus();

    void irons_spellbooks$setAquifierStatus(AquifierNuke nuke);
}
