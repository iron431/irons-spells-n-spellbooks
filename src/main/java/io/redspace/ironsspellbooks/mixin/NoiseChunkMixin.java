package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.worldgen.IExtendedNoiseChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin implements IExtendedNoiseChunk {
    @Unique
    AquifierNuke irons_spellbooks$aquifierNuke = null;

    @Unique
    BlockState irons_spellbooks$defaultBlockState;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void irons_spellbooks$captureDefaultBlockstate(int cellCountXZ, RandomState random, int firstNoiseX, int firstNoiseZ, NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blendifier, CallbackInfo ci) {
        this.irons_spellbooks$defaultBlockState = noiseGeneratorSettings.defaultBlock();
    }

    @Inject(method = "getInterpolatedState", at = @At("RETURN"), cancellable = true)
    private void irons_spellbooks$cancelAquifierGeneration(CallbackInfoReturnable<BlockState> cir) {
        var state = cir.getReturnValue();
        if (state == null) {
            return;
        }
        var nuke = irons_spellbooks$getAquifierStatus();
        if (nuke == null) {
            return;
        }

        if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            NoiseChunk chunk = (NoiseChunk) (Object) this;
            var x = chunk.blockX();
            var y = chunk.blockY();
            var z = chunk.blockZ();
            for (BoundingBox box : nuke.boundingBoxes()) {
                //todo: it is likely most bb's aren't even in the chunk. can do prelimiary checks
                int dx = 0;
                if (x < box.minX()) {
                    dx = box.minX() - x;
                } else if (x > box.maxX()) {
                    dx = x - box.maxX();
                }

                int dy = 0;
                if (y < box.minY()) {
                    dy = box.minY() - y;
                } else if (y > box.maxY()) {
                    dy = y - box.maxY();
                }

                int dz = 0;
                if (z < box.minZ()) {
                    dz = box.minZ() - z;
                } else if (z > box.maxZ()) {
                    dz = z - box.maxZ();
                }

                int manhattanDistance = dx + dy + dz;
                if (manhattanDistance <= 5) {
                    if (manhattanDistance <= 3) {
                        cir.setReturnValue(Blocks.CAVE_AIR.defaultBlockState());
                    } else {
                        cir.setReturnValue(this.irons_spellbooks$defaultBlockState);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public AquifierNuke irons_spellbooks$getAquifierStatus() {
        return irons_spellbooks$aquifierNuke;
    }

    @Override
    public void irons_spellbooks$setAquifierStatus(AquifierNuke nuke) {
        this.irons_spellbooks$aquifierNuke = nuke;
    }
}
