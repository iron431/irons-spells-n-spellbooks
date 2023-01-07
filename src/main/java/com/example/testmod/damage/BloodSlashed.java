package com.example.testmod.damage;

import com.example.testmod.TestMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BloodSlashed extends MobEffect {
    public BloodSlashed(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amount) {
        TestMod.LOGGER.debug("BloodSlashed.applyEffectTick: {}", amount);
        entity.hurt(DamageSources.BLOOD_SLASH, 1);
    }
}
