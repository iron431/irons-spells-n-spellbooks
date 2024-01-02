package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class FortifyEffect extends MagicMobEffect {
    public FortifyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        pLivingEntity.setAbsorptionAmount(pLivingEntity.getAbsorptionAmount() - (float)(pAmplifier + 1));
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
    }

    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        pLivingEntity.setAbsorptionAmount(pLivingEntity.getAbsorptionAmount() + (float)(pAmplifier + 1));
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
    }
}
