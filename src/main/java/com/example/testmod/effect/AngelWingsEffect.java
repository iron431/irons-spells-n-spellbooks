package com.example.testmod.effect;

import com.example.testmod.TestMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import top.theillusivec4.caelus.api.CaelusApi;

public class AngelWingsEffect extends MobEffect {

    private LivingEntity livingEntity;

    public AngelWingsEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

//    @Override
//    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
//        if(pLivingEntity.getAttributes().hasAttribute(CaelusApi.getInstance().getFlightAttribute())){
//            TestMod.LOGGER.debug("AngelWingsEffect.applyEffectTick");
//            pLivingEntity.getAttributes().getInstance(CaelusApi.getInstance().getFlightAttribute()).addTransientModifier(CaelusApi.getInstance().getElytraModifier());
//        }
//    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        TestMod.LOGGER.debug("AngelWingsEffect.removeAttributeModifiers.1");
        if (pLivingEntity.getAttributes().hasAttribute(CaelusApi.getInstance().getFlightAttribute())) {
            TestMod.LOGGER.debug("AngelWingsEffect.removeAttributeModifiers.2");
            pLivingEntity.getAttributes().getInstance(CaelusApi.getInstance().getFlightAttribute()).setBaseValue(0);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        livingEntity = pLivingEntity;
        TestMod.LOGGER.debug("AngelWingsEffect.addAttributeModifiers.1");

        if (pLivingEntity.getAttributes().hasAttribute(CaelusApi.getInstance().getFlightAttribute())) {
            TestMod.LOGGER.debug("AngelWingsEffect.addAttributeModifiers.2");
            pLivingEntity.getAttributes().getInstance(CaelusApi.getInstance().getFlightAttribute()).setBaseValue(1);
        }

    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        if (livingEntity != null) {
            TestMod.LOGGER.debug("AngelWingsEffect.isDurationEffectTick base:{}, val:{}",
                    livingEntity.getAttributes().getInstance(CaelusApi.getInstance().getFlightAttribute()).getBaseValue(),
                    livingEntity.getAttributes().getInstance(CaelusApi.getInstance().getFlightAttribute()).getValue());

        }
        return false;
    }
}
