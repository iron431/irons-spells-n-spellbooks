package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.worldgen.AquiferHelper;
import io.redspace.ironsspellbooks.worldgen.IndividualTerrainStructurePoolElement;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Beardifier.class)
public class BeardifierMixin /*implements IExtendedBeardifier*/ {
//    @Unique
//    private NoiseChunk irons_spellbooks$noiseChunk;

    /**
     * Intercept our own structure elements and apply custom processing as needed. Most is a mirror of default method.
     */
    @Inject(
            method = {"lambda$forStructuresInChunk$2", "m_223930_"},
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void irons_spellbooks$injectCustomTerrainAdaptation(ChunkPos pChunkPos, ObjectList<Beardifier.Rigid> list, int i, int j, ObjectList<JigsawJunction> junctions, StructureStart p_223936_, CallbackInfo ci) {
        boolean didwork = false;
        for (StructurePiece structurepiece : p_223936_.getPieces()) {
            if (structurepiece instanceof PoolElementStructurePiece poolelementstructurepiece) {
                if (poolelementstructurepiece.getElement() instanceof IndividualTerrainStructurePoolElement ironElement) {
                    AquiferHelper.marked = true;
                    if (structurepiece.isCloseToChunk(pChunkPos, 12)) {
                        StructureTemplatePool.Projection structuretemplatepool$projection = ironElement.getProjection();
                        if (structuretemplatepool$projection == StructureTemplatePool.Projection.RIGID) {
                            list.add(new Beardifier.Rigid(poolelementstructurepiece.getBoundingBox(), ironElement.getTerrainAdjustment(), ironElement.getGroundLevelDelta()));
                        }

                        //from default beardifier
                        for (JigsawJunction jigsawjunction : poolelementstructurepiece.getJunctions()) {
                            int k = jigsawjunction.getSourceX();
                            int l = jigsawjunction.getSourceZ();
                            if (k > i - 12 && l > j - 12 && k < i + 15 + 12 && l < j + 15 + 12) {
                                junctions.add(jigsawjunction);
                            }
                        }
                        didwork = true;
                    }
                }
            }
        }
        if (didwork) {
            ci.cancel();
        }
    }
//
//    @Inject(method = "compute", at = @At(value = "HEAD"))
//    private void irons_spellbooks$changeAquifierStatus(DensityFunction.FunctionContext context, CallbackInfoReturnable<Double> cir) {
//        var noiseChunk = getNoiseChunk();
//        if (noiseChunk != null) {
//            ((IExtendedNoiseChunk) noiseChunk).setAquifierStatus(new IExtendedNoiseChunk.AquifierNuke());
//        }
//    }
//
//    @Override
//    public void setNoiseChunk(NoiseChunk noiseChunk) {
//        this.irons_spellbooks$noiseChunk = noiseChunk;
//    }
//
//    @Override
//    public NoiseChunk getNoiseChunk() {
//        return irons_spellbooks$noiseChunk;
//    }
}
