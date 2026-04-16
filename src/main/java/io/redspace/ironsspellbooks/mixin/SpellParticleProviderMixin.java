package io.redspace.ironsspellbooks.mixin;

import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.particle.FadingTrialOmenSpellParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpellParticle.Provider.class)
public class SpellParticleProviderMixin {
    @Shadow
    @Final
    private SpriteSet sprite;

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void irons_spellbooks$fadingTrialOmen(
        SimpleParticleType type,
        ClientLevel level,
        double x,
        double y,
        double z,
        double xSpeed,
        double ySpeed,
        double zSpeed,
        CallbackInfoReturnable<Particle> cir
    ) {
        if (ClientConfigs.REDUCE_TRIAL_OMEN_PARTICLE.get() && type == ParticleTypes.TRIAL_OMEN) {
            cir.setReturnValue(new FadingTrialOmenSpellParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite));
            cir.cancel();
        }
    }
}
