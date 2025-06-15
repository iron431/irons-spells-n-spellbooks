package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.worldgen.IExtendedNoiseChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin implements IExtendedNoiseChunk {
    @Unique
    AquifierNuke irons_spellbooks$aquifierNuke = null;

//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void irons_spellbooks$attachChunkToBeardifier(int cellCountXZ, RandomState random, int firstNoiseX, int firstNoiseZ, NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blendifier, CallbackInfo ci) {
//        if (beardifier instanceof Beardifier brd) {
//            ((IExtendedBeardifier) brd).setNoiseChunk((NoiseChunk) (Object) this);
//        }
//    }

    @Inject(method = "getInterpolatedState", at = @At("RETURN"),cancellable = true)
    private void irons_spellbooks$cancelAquifierGeneration(CallbackInfoReturnable<BlockState> cir) {
        var state = cir.getReturnValue();
        if (state == null) {
            return;
        }
        var nuke = getAquifierStatus();
        if (nuke == null) {
            return;
        }
        if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            cir.setReturnValue(Blocks.EMERALD_BLOCK.defaultBlockState());
        }
    }

    @Override
    public AquifierNuke getAquifierStatus() {
        return irons_spellbooks$aquifierNuke;
    }

    @Override
    public void setAquifierStatus(AquifierNuke nuke) {
        this.irons_spellbooks$aquifierNuke = nuke;
    }
}
