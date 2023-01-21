package com.example.testmod.effect;

import com.example.testmod.TestMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.caelus.api.CaelusApi;

public class AngelWingsEffect extends MobEffect {

    public AngelWingsEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
        TestMod.LOGGER.debug("AngelWingsEffect.applyInstantenousEffect");
        setFlying(pLivingEntity);
    }
    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        TestMod.LOGGER.debug("AngelWingsEffect.applyEffectTick");
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        TestMod.LOGGER.debug("AngelWingsEffect.removeAttributeModifiers");
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
    }

    @Override
    public MobEffect addAttributeModifier(Attribute pAttribute, String pUuid, double pAmount, AttributeModifier.Operation pOperation) {
        TestMod.LOGGER.debug("AngelWingsEffect.addAttributeModifier");
        return super.addAttributeModifier(pAttribute, pUuid, pAmount, pOperation);
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        TestMod.LOGGER.debug("AngelWingsEffect.addAttributeModifiers");
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
    }

    @Override
    public boolean isInstantenous() {
        TestMod.LOGGER.debug("AngelWingsEffect.isInstantenous");
        return true;
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        //TestMod.LOGGER.debug("AngelWingsEffect.isDurationEffectTick");
        return false;
    }

    private void setFlying(LivingEntity pLivingEntity) {
        TestMod.LOGGER.debug("AngelWingsEffect.setFlying");
        AttributeInstance attributeInstance = pLivingEntity.getAttribute(CaelusApi.getInstance().getFlightAttribute());
        if (attributeInstance != null && !attributeInstance.hasModifier(CaelusApi.getInstance().getElytraModifier()))
            attributeInstance.addTransientModifier(CaelusApi.getInstance().getElytraModifier());
    }

}
