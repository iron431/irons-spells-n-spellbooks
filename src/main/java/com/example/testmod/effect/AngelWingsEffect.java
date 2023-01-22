package com.example.testmod.effect;

import com.example.testmod.TestMod;
import net.minecraft.client.Minecraft;
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

    @Override
    public boolean isInstantenous() {
        return false;
    }


    //    @Override
//    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
//        if (livingEntity != null) {
//            TestMod.LOGGER.debug("AngelWingsEffect.isDurationEffectTick base:{}, val:{}",
//                    livingEntity.getAttributes().getInstance(CaelusApi.getInstance().getFlightAttribute()).getBaseValue(),
//                    livingEntity.getAttributes().getInstance(CaelusApi.getInstance().getFlightAttribute()).getValue());
//
//        }
//        return false;
//    }
}
