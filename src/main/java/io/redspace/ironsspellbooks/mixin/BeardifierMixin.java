package io.redspace.ironsspellbooks.mixin;

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
public class BeardifierMixin {

    /**
     * Intercept our own structure elements and apply custom processing as needed. Most is a mirror of default method.
     */
    @Inject(
            method = {"lambda$forStructuresInChunk$2","m_223930_"},
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void injectCustomTerrainAdaptation(ChunkPos pChunkPos, ObjectList<Beardifier.Rigid> list, int i, int j, ObjectList<JigsawJunction> junctions, StructureStart p_223936_, CallbackInfo ci) {
        //TODO: could make custom registered structure as well, and filter by only our structures for better performance
        for (StructurePiece structurepiece : p_223936_.getPieces()) {
            if (structurepiece.isCloseToChunk(pChunkPos, 12)) {
                if (structurepiece instanceof PoolElementStructurePiece poolelementstructurepiece) {
                    var element = poolelementstructurepiece.getElement();
                    if (element instanceof IndividualTerrainStructurePoolElement ironElement) {
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
                        ci.cancel();
                    }
                }
            }
        }
    }
}
