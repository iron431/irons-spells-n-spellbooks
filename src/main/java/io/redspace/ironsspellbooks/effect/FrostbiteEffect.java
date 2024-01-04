package io.redspace.ironsspellbooks.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class FrostbiteEffect extends MagicMobEffect {

    public FrostbiteEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(livingEntity, pAttributeMap, pAmplifier);
        //livingEntity.getMobType()
        //PlayerMagicData.getPlayerMagicData(livingEntity).getSyncedData().setHasAbyssalShroud(false);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        //PlayerMagicData.getPlayerMagicData(pLivingEntity).getSyncedData().setHasAbyssalShroud(true);
    }


    public static boolean doEffect(LivingEntity livingEntity, DamageSource damageSource) {

        return true;
    }
}
