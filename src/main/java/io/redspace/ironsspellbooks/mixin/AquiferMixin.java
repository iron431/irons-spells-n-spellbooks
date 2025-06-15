//package io.redspace.ironsspellbooks.mixin;
//
//import io.redspace.ironsspellbooks.worldgen.IExtendedNoiseChunk;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.levelgen.Aquifer;
//import net.minecraft.world.level.levelgen.NoiseChunk;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(Aquifer.NoiseBasedAquifer.class)
//public class AquiferMixin {
//
//    @Shadow
//    NoiseChunk noiseChunk;
//
//    @Inject(method = "computeFluid", at = @At("HEAD"), cancellable = true)
//    private void irons_spellbooks$cancelAquiferGeneration(int x, int y, int z, CallbackInfoReturnable<Aquifer.FluidStatus> cir) {
//        IExtendedNoiseChunk extChunk = (IExtendedNoiseChunk) noiseChunk;
//        var aquifer = extChunk.getAquifierStatus();
//        if (aquifer != null) {
//            cir.setReturnValue(new Aquifer.FluidStatus(y, Blocks.AIR.defaultBlockState()));
//        }
//    }
//}
