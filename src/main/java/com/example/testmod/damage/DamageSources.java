package com.example.testmod.damage;

import com.example.testmod.registries.AttributeRegistry;
import com.example.testmod.spells.SchoolType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

//https://github.com/cleannrooster/Spellblade-1.19.2/search?q=MobEffect
//https://github.com/LittleEzra/Augment-1.19.2/blob/334dc95462a3e6b25e6f73d3d909d012d63be109/src/main/java/com/littleezra/augment/item/enchantment/RecoilCurseEnchantment.java
//DamageSource
//StatusEffect
//MobEffect: https://forge.gemwire.uk/wiki/Mob_Effects/1.18

public class DamageSources {


//    public static EntityDamageSource bloodSlash(Player player) {
//        return new EntityDamageSource(BLOOD_MAGIC_ID, player);
//    }

    public static DamageSource CAULDRON = new DamageSource("blood_cauldron");
    public static DamageSource HEARTSTOP = new DamageSource("heartstop").bypassArmor().bypassMagic();

//    public static DamageSource BLOOD_MAGIC = new DamageSource(BLOOD_MAGIC_ID);
//    public static DamageSource FIRE_MAGIC = new DamageSource(FIRE_MAGIC_ID).setIsFire();
//    public static DamageSource ICE_MAGIC = new DamageSource(ICE_MAGIC_ID);
//    public static DamageSource HOLY_MAGIC = new DamageSource(HOLY_MAGIC_ID);
//    public static DamageSource ENDER_MAGIC = new DamageSource(ENDER_MAGIC_ID);

    //TODO: decide if we want per-school death messages or per-spell death messages
    public static float applyDamage(Entity target, float baseAmount, DamageSource damageSource, SchoolType damageSchool, @Nullable Entity attacker) {
        if (target instanceof LivingEntity livingTarget) {
            float adjustedDamage = baseAmount * getResist(livingTarget, damageSchool);
            if (attacker != null) {
                livingTarget.hurt(new EntityDamageSource(damageSource.getMsgId(), attacker), adjustedDamage);
                if (attacker instanceof LivingEntity livingAttacker)
                    livingAttacker.setLastHurtMob(target);
            } else {
                livingTarget.hurt(damageSource, adjustedDamage);
            }
            return adjustedDamage;
        } else {
            target.hurt(damageSource, baseAmount);
            return 0;
        }

    }

    public static float getResist(LivingEntity entity, SchoolType damageSchool) {
        return switch (damageSchool) {
            case FIRE -> 2 - (float) entity.getAttributeValue(AttributeRegistry.FIRE_MAGIC_RESIST.get());
            case ICE -> 2 - (float) entity.getAttributeValue(AttributeRegistry.ICE_MAGIC_RESIST.get());
            case LIGHTNING -> 2 - (float) entity.getAttributeValue(AttributeRegistry.LIGHTNING_MAGIC_RESIST.get());
            case HOLY -> 2 - (float) entity.getAttributeValue(AttributeRegistry.HOLY_MAGIC_RESIST.get());
            case ENDER -> 2 - (float) entity.getAttributeValue(AttributeRegistry.ENDER_MAGIC_RESIST.get());
            case BLOOD -> 2 - (float) entity.getAttributeValue(AttributeRegistry.BLOOD_MAGIC_RESIST.get());
            case EVOCATION -> 2 - (float) entity.getAttributeValue(AttributeRegistry.EVOCATION_MAGIC_RESIST.get());
            default -> 1;
        };
    }
}
