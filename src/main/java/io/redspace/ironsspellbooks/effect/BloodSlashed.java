package io.redspace.ironsspellbooks.effect;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import static io.redspace.ironsspellbooks.damage.DamageSources.BLEED_DAMAGE;

public class BloodSlashed extends MobEffect {


    public BloodSlashed(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

//    public static void applyDamage(Entity source, Entity target, float baseDamage) {
//        if (target instanceof LivingEntity targetEntity) {
//            float resist = 2 - (float) targetEntity.getAttributeValue(AttributeRegistry.BLOOD_MAGIC_RESIST.get());
//            float actualDamage = baseDamage * resist;
//            float actualHeal = baseDamage * resist * .1f;
//            DamageSource damageSource = null;
//
//            if (source instanceof Player sourcePlayer) {
//                damageSource = DamageSources.bloodSlash(sourcePlayer);
//                sourcePlayer.heal(actualHeal);
//            } else {
//                damageSource = DamageSources.BLOOD_MAGIC;
//            }
//
//            targetEntity.hurt(damageSource, actualDamage);
//            targetEntity.addEffect(new MobEffectInstance(MobEffectRegistry.BLOOD_SLASHED.get(), 40, 1));
//        }else{
//            target.hurt(DamageSources.BLOOD_MAGIC, baseDamage);
//
//        }
//    }

    @Override
    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amount) {
//        float resist = 1;
//        if (entity instanceof Player targetPlayer) {
//            resist = 2 - (float) targetPlayer.getAttributeValue(AttributeRegistry.BLOOD_MAGIC_RESIST.get());
//        }
//
//        entity.hurt(DamageSources.BLOOD_MAGIC, amount * resist);
        DamageSources.applyDamage(entity, amount, BLEED_DAMAGE, SchoolType.BLOOD);
    }
}
