package io.redspace.ironsspellbooks.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Marker as an effect that is affected by counterspell
 */
public class MagicMobEffect extends MobEffect implements IMobEffectEndCallback {
    public MagicMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    public MagicMobEffect(MobEffectCategory category, int color, ParticleOptions particle) {
        super(category, color, particle);
    }

    /**
     * VANILLA ANNOTATION: Called when a mob effect is added or updated (ie: I have poison, I get poisoned again)
     */
    @Override
    public void onEffectStarted(LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectStarted(pLivingEntity, pAmplifier);
    }

    /**
     * VANILLA ANNOTATION: Called when a mob effect is added for the first time (ie: I did not have poison, I get poisoned)
     */
    @Override
    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {

    }
}
