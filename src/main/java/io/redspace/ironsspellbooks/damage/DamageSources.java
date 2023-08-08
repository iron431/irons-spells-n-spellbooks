package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.api.entity.NoKnockbackProjectile;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;

//https://github.com/cleannrooster/Spellblade-1.19.2/search?q=MobEffect
//https://github.com/LittleEzra/Augment-1.19.2/blob/334dc95462a3e6b25e6f73d3d909d012d63be109/src/main/java/com/littleezra/augment/item/enchantment/RecoilCurseEnchantment.java
//DamageSource
//StatusEffect
//MobEffect: https://forge.gemwire.uk/wiki/Mob_Effects/1.18

@Mod.EventBusSubscriber
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
            boolean fromSummon = false;
            if (damageSource.getDirectEntity() instanceof MagicSummon summon) {
                fromSummon = true;
                if (summon.getSummoner() != null)
                    adjustedDamage *= summon.getSummoner().getAttributeValue(AttributeRegistry.SUMMON_DAMAGE.get());
            } else if (damageSource.getDirectEntity() instanceof NoKnockbackProjectile) {
                ignoreNextKnockback(livingTarget);
            }
            if (damageSource.getEntity() instanceof LivingEntity livingAttacker) {
                if (isFriendlyFireBetween(livingAttacker, livingTarget))
                    return false;
                livingAttacker.setLastHurtMob(target);
            }
            var flag = livingTarget.hurt(damageSource, adjustedDamage);
            if (fromSummon)
                livingTarget.setLastHurtByMob((LivingEntity) damageSource.getDirectEntity());
            return flag;
        } else {
            return target.hurt(damageSource, baseAmount);
        }

    }

    //I can't tell if this is genius or incredibly stupid
    private static final HashMap<LivingEntity, Integer> knockbackImmunes = new HashMap<>();

    public static void ignoreNextKnockback(LivingEntity livingEntity) {
        if (!livingEntity.getLevel().isClientSide)
            knockbackImmunes.put(livingEntity, livingEntity.tickCount);
    }

    @SubscribeEvent
    public static void cancelKnockback(LivingKnockBackEvent event) {
        //IronsSpellbooks.LOGGER.debug("DamageSources.cancelKnockback {}", event.getEntity().getName().getString());
        if (knockbackImmunes.containsKey(event.getEntity())) {
            var entity = event.getEntity();
            if (entity.tickCount - knockbackImmunes.get(entity) <= 1) {
                event.setCanceled(true);
            }
            knockbackImmunes.remove(entity);
        }
    }

    public static boolean isFriendlyFireBetween(Entity attacker, Entity target) {
        if (attacker == null || target == null)
            return false;
        var team = attacker.getTeam();
        if (team != null) {
            return team.isAlliedTo(target.getTeam()) && !team.isAllowFriendlyFire();
        }
        return false;
    }

    public static DamageSource directDamageSource(DamageSource source, Entity attacker) {
        return new EntityDamageSource(source.getMsgId(), attacker);
    }

    public static DamageSource indirectDamageSource(DamageSource source, Entity projectile, @Nullable Entity attacker) {
        return new IndirectEntityDamageSource(source.msgId, projectile, attacker);
    }

    /**
     * Returns the resistance multiplier of the entity. (If they are resistant, the value is < 1)
     */
    public static float getResist(LivingEntity entity, SchoolType damageSchool) {
        if (damageSchool == null)
            return 1;
        else
            return 2 - (float) Utils.softCapFormula(damageSchool.getResistanceFor(entity));
    }
}
