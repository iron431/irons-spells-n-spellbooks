package com.example.testmod.damage;

import com.example.testmod.TestMod;
import com.example.testmod.registries.AttributeRegistry;
import com.example.testmod.registries.MobEffectRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BloodSlashed extends MobEffect {

    public BloodSlashed(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    public static void applyDamage(Entity source, Entity target, float baseDamage) {
        if (target instanceof Player targetPlayer) {
            float resist = 2 - (float) targetPlayer.getAttributeValue(AttributeRegistry.BLOOD_MAGIC_RESIST.get());
            float actualDamage = baseDamage * resist;
            float actualHeal = baseDamage * resist * .1f;
            DamageSource damageSource = null;

            if (source instanceof Player sourcePlayer) {
                damageSource = DamageSources.bloodSlash(sourcePlayer);
                sourcePlayer.heal(actualHeal);
            } else {
                damageSource = DamageSources.BLOOD_SLASH;
            }

            targetPlayer.hurt(damageSource, actualDamage);
            targetPlayer.addEffect(new MobEffectInstance(MobEffectRegistry.BLOOD_SLASHED.get(), 40, 1));
        }
    }

    @Override
    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amount) {
        float resist = 1;
        if (entity instanceof Player targetPlayer) {
            resist = 2 - (float) targetPlayer.getAttributeValue(AttributeRegistry.BLOOD_MAGIC_RESIST.get());
        }

        entity.hurt(DamageSources.BLOOD_SLASH, amount * resist);
    }
}
