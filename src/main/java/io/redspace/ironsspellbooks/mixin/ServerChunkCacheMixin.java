package io.redspace.ironsspellbooks.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static io.redspace.ironsspellbooks.entity.spells.pocket_dimension_portal.PocketDimensionIdManager.POCKET_DIMENSION;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
    @Final
    @Shadow
    private DistanceManager distanceManager;
    @Final
    @Shadow
    public ServerLevel level;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    void irons_spellbooks$pocketDimensionSimulationDistance(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper, StructureTemplateManager structureManager, Executor dispatcher, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progressListener, ChunkStatusUpdateListener chunkStatusListener, Supplier overworldDataStorage, CallbackInfo ci) {
        //todo: profile?
        if (level.dimension().equals(POCKET_DIMENSION)) {
            distanceManager.updateSimulationDistance(3);
        }
    }

    @Inject(method = "setSimulationDistance", at = @At(value = "HEAD"), cancellable = true)
    void irons_spellbooks$pocketDimensionCancelSD(int simulationDistance, CallbackInfo ci) {
        //todo: profile?
        if (level.dimension().equals(POCKET_DIMENSION)) {
            ci.cancel();
        }
    }
}
