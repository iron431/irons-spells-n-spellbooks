package io.redspace.ironsspellbooks.effect;

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

    /**
     * @return VANILLA ANNOTATION: True if the effect should continue, or false if the effect should end and be removed
     */
    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        return super.applyEffectTick(pLivingEntity, pAmplifier);
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
