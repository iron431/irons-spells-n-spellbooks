package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsAttributeCompatMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

/**
 * Marker as an effect that is affected by counterspell
 */
public class MagicMobEffect extends MobEffect implements IMobEffectEndCallback , IBackwardsAttributeCompatMobEffect {
    public MagicMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {

    }

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        onEffectAdded(pLivingEntity, pAmplifier);
    }

}
