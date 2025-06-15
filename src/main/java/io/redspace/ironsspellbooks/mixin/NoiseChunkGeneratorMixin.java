package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.worldgen.AquiferHelper;
import io.redspace.ironsspellbooks.worldgen.IExtendedNoiseChunk;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @Inject(method = "createNoiseChunk", at = @At("RETURN"), cancellable = true)
    void irons_spellbooks$detectAquifers(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState random, CallbackInfoReturnable<NoiseChunk> cir) {
        var registry = structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE);
//        if (AquiferHelper.marked) {
//            ((IExtendedNoiseChunk) cir.getReturnValue()).setAquifierStatus(new IExtendedNoiseChunk.AquifierNuke());
//        }
        IExtendedNoiseChunk noisechunk = (IExtendedNoiseChunk) cir.getReturnValue();
        var reference = structureManager.startsForStructure(chunk.getPos(), structure -> true);
        var starts = structureManager.startsForStructure(chunk.getPos(), structure ->
                registry.getKey(structure).getNamespace().equals(IronsSpellbooks.MODID));
        if (!starts.isEmpty() //todo: expensive as hell check?
        ) {
            noisechunk.setAquifierStatus(new IExtendedNoiseChunk.AquifierNuke());
//            for(var start : starts){
//                for (var structurepiece : start.getPieces()){
//                    if (structurepiece instanceof PoolElementStructurePiece poolelementstructurepiece) {
//                        if (!(poolelementstructurepiece.getElement() instanceof IndividualTerrainStructurePoolElement ironElement)) {
//                            IronsSpellbooks.LOGGER.info("eingisegn");
//                        }
//                    }
//                }
//            }
            if(!AquiferHelper.marked){
                IronsSpellbooks.LOGGER.info("ergsgsg");
            }
        }
//        cir.setReturnValue();
        AquiferHelper.marked = false;
    }
}
