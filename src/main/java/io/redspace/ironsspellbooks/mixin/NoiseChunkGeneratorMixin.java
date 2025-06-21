package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.worldgen.AquiferHelper;
import io.redspace.ironsspellbooks.worldgen.IExtendedNoiseChunk;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @Inject(method = "createNoiseChunk", at = @At("RETURN"))
    void irons_spellbooks$detectAquifers(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState random, CallbackInfoReturnable<NoiseChunk> cir) {
        if (ServerConfigs.SPEC.isLoaded() && !ServerConfigs.AQUIFER_DETECTION.get()) {
            return;
        }
        IExtendedNoiseChunk noisechunk = (IExtendedNoiseChunk) cir.getReturnValue();
        var starts = structureManager.startsForStructure(chunk.getPos(), structure -> AquiferHelper.getOrCacheStructures(structureManager).contains(structure));
        if (!starts.isEmpty()
        ) {
            BoundingBox[] boundingBoxes = starts.stream().flatMap(s -> s.getPieces().stream()).map(StructurePiece::getBoundingBox).toArray(BoundingBox[]::new);
            noisechunk.irons_spellbooks$setAquifierStatus(new IExtendedNoiseChunk.AquifierNuke(boundingBoxes));
        }
    }
}
