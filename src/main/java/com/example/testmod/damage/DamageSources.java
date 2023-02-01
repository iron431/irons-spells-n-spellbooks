package com.example.testmod.damage;

import com.example.testmod.registries.AttributeRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

//https://github.com/cleannrooster/Spellblade-1.19.2/search?q=MobEffect
//https://github.com/LittleEzra/Augment-1.19.2/blob/334dc95462a3e6b25e6f73d3d909d012d63be109/src/main/java/com/littleezra/augment/item/enchantment/RecoilCurseEnchantment.java
//DamageSource
//StatusEffect
//MobEffect: https://forge.gemwire.uk/wiki/Mob_Effects/1.18

public class DamageSources {

    public static final String BLOOD_MAGIC_ID = "blood_magic";
    public static final String FIRE_MAGIC_ID = "fire_magic";
    public static final String ICE_MAGIC_ID = "ice_magic";
    public static final String HOLY_MAGIC_ID = "holy_magic";
    public static final String ENDER_MAGIC_ID = "ender_magic";

    public static EntityDamageSource bloodSlash(Player player) {
        return new EntityDamageSource(BLOOD_MAGIC_ID, player);
    }

    public static DamageSource BLOOD_MAGIC = new DamageSource(BLOOD_MAGIC_ID);
    public static DamageSource FIRE_MAGIC = new DamageSource(FIRE_MAGIC_ID).setIsFire();
    public static DamageSource ICE_MAGIC = new DamageSource(ICE_MAGIC_ID);
    public static DamageSource HOLY_MAGIC = new DamageSource(HOLY_MAGIC_ID);
    public static DamageSource ENDER_MAGIC = new DamageSource(ENDER_MAGIC_ID);

    public static float applyDamage(Entity target, float baseAmount, DamageSource damageSource, @Nullable Entity attacker) {
        if (target instanceof LivingEntity livingTarget) {
            float adjustedDamage = baseAmount * getResist(livingTarget, damageSource);
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

    public static float getResist(LivingEntity entity, DamageSource damageSource) {
        return switch (damageSource.getMsgId()) {
            case BLOOD_MAGIC_ID -> 2 - (float) entity.getAttributeValue(AttributeRegistry.BLOOD_MAGIC_RESIST.get());
            case ICE_MAGIC_ID -> 2 - (float) entity.getAttributeValue(AttributeRegistry.ICE_MAGIC_RESIST.get());
            default -> 1;
        };
    }
}
