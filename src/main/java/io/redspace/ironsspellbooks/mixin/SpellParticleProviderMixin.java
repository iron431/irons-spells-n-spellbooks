package io.redspace.ironsspellbooks.mixin;

import net.minecraft.client.particle.SpellParticle;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SpellParticle.Provider.class)
public class SpellParticleProviderMixin {
    /*
    Vanlla particle doesn't exist on 1.20.1
     */
//    @Shadow
//    @Final
//    private SpriteSet sprite;
//
//    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
//    private void irons_spellbooks$fadingTrialOmen(
//        SimpleParticleType type,
//        ClientLevel level,
//        double x,
//        double y,
//        double z,
//        double xSpeed,
//        double ySpeed,
//        double zSpeed,
//        CallbackInfoReturnable<Particle> cir
//    ) {
//        if (ClientConfigs.REDUCE_TRIAL_OMEN_PARTICLE.get() && type == ParticleRegistry.TRIAL_OMEN_PARTICLE.get()) {
//            cir.setReturnValue(new FadingTrialOmenSpellParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite));
//            cir.cancel();
//        }
//    }
}
