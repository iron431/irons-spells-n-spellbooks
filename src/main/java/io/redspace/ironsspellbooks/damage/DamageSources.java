package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
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

    public static DamageSource BLEED_DAMAGE = new DamageSource("bleed_effect");

    public static DamageSource FIRE_MAGIC = new DamageSource("fire_magic_damage");
    public static DamageSource ICE_MAGIC = new DamageSource("ice_magic_damage");
    public static DamageSource LIGHTNING_MAGIC = new DamageSource("lightning_magic_damage");
    public static DamageSource HOLY_MAGIC = new DamageSource("holy_magic_damage");
    public static DamageSource BLOOD_MAGIC = new DamageSource("blood_magic_damage");
    public static DamageSource ENDER_MAGIC = new DamageSource("ender_magic_damage");
    public static DamageSource EVOCATION_MAGIC = new DamageSource("evocation_magic_damage");

    public static boolean applyDamage(Entity target, float baseAmount, DamageSource damageSource, @Nullable SchoolType damageSchool) {
        if (target instanceof LivingEntity livingTarget) {
            //Todo: should this be handled in damage event? (would by where enchantments and stuff also get put)
            float adjustedDamage = baseAmount * getResist(livingTarget, damageSchool);

            if (damageSource.getDirectEntity() instanceof LivingEntity livingAttacker) {
                if (livingAttacker.isAlliedTo(livingTarget))
                    return false;
                livingAttacker.setLastHurtMob(target);
            }
            var flag = livingTarget.hurt(damageSource, adjustedDamage);
            if (flag && damageSource.getDirectEntity() instanceof MagicSummon)
                livingTarget.setLastHurtByMob((LivingEntity) damageSource.getDirectEntity());
            return flag;
        } else {
            return target.hurt(damageSource, baseAmount);
        }

    }

    public static DamageSource directDamageSource(DamageSource source, Entity attacker) {
        return new EntityDamageSource(source.getMsgId(), attacker);
    }

    public static DamageSource indirectDamageSource(DamageSource source, Entity projectile, @Nullable Entity attacker) {
        return new IndirectEntityDamageSource(source.msgId, projectile, attacker);
    }

    public static float getResist(LivingEntity entity, SchoolType damageSchool) {
        if (damageSchool == null)
            return 1;
        else
            return (float) entity.getAttributeValue(AttributeRegistry.SPELL_RESIST.get());
//        return switch (damageSchool) {
//            case FIRE -> 2 - (float) entity.getAttributeValue(AttributeRegistry.FIRE_MAGIC_RESIST.get());
//            case ICE -> 2 - (float) entity.getAttributeValue(AttributeRegistry.ICE_MAGIC_RESIST.get());
//            case LIGHTNING -> 2 - (float) entity.getAttributeValue(AttributeRegistry.LIGHTNING_MAGIC_RESIST.get());
//            case HOLY -> 2 - (float) entity.getAttributeValue(AttributeRegistry.HOLY_MAGIC_RESIST.get());
//            case ENDER -> 2 - (float) entity.getAttributeValue(AttributeRegistry.ENDER_MAGIC_RESIST.get());
//            case BLOOD -> 2 - (float) entity.getAttributeValue(AttributeRegistry.BLOOD_MAGIC_RESIST.get());
//            case EVOCATION -> 2 - (float) entity.getAttributeValue(AttributeRegistry.EVOCATION_MAGIC_RESIST.get());
//            default -> 1;
//        };
    }
}
