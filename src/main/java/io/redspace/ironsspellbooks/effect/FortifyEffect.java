package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class FortifyEffect extends MagicMobEffect {
    public FortifyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void onEffectStarted(LivingEntity pLivingEntity, int pAmplifier) {
        super.onEffectStarted(pLivingEntity, pAmplifier);
        pLivingEntity.setAbsorptionAmount(pLivingEntity.getAbsorptionAmount() + (float) (pAmplifier + 1));
    }
}
