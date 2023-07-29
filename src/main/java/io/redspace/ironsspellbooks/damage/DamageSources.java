package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.spells.SchoolType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;

import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

//https://github.com/cleannrooster/Spellblade-1.19.2/search?q=MobEffect
//https://github.com/LittleEzra/Augment-1.19.2/blob/334dc95462a3e6b25e6f73d3d909d012d63be109/src/main/java/com/littleezra/augment/item/enchantment/RecoilCurseEnchantment.java
//DamageSource
//StatusEffect
//MobEffect: https://forge.gemwire.uk/wiki/Mob_Effects/1.18

@Mod.EventBusSubscriber
public class DamageSources {
    public static DamageSource get(Level level, ResourceKey<DamageType> damageType) {
        return level.damageSources().source(damageType);
    }

    public static Holder<DamageType> getHolderFromResource(Entity entity, ResourceKey<DamageType> damageTypeResourceKey) {
        var option = entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(damageTypeResourceKey);
        if (option.isPresent()) {
            return option.get();
        } else {
            return entity.level().damageSources().genericKill().typeHolder();
        }
    }

    public static boolean applyDamage(Entity target, float baseAmount, DamageSource damageSource, @Nullable SchoolType damageSchool) {
        if (target instanceof LivingEntity livingTarget) {
            //Todo: should this be handled in damage event? (would by where enchantments and stuff also get put)
            float adjustedDamage = baseAmount * getResist(livingTarget, damageSchool);
            boolean fromSummon = false;
            if (damageSource.getDirectEntity() instanceof MagicSummon summon) {
                fromSummon = true;
                if (summon.getSummoner() != null)
                    adjustedDamage *= summon.getSummoner().getAttributeValue(AttributeRegistry.SUMMON_DAMAGE.get());
            } else if (damageSource.getDirectEntity() instanceof AoeEntity || damageSource.getDirectEntity() instanceof AbstractConeProjectile) {
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

    @Deprecated(since = "MC_1.20", forRemoval = true)
    public static DamageSource directDamageSource(DamageSource source, Entity attacker) {
        return new DamageSource(source.typeHolder(), attacker);
        //return new EntityDamageSource(source.getMsgId(), attacker);
    }

    @Deprecated(since = "MC_1.20", forRemoval = true)
    public static DamageSource indirectDamageSource(DamageSource source, Entity projectile, @Nullable Entity attacker) {
        return new DamageSource(source.typeHolder(), attacker, projectile);
    }

    /**
     * Returns the resistance multiplier of the entity. (If they are resistant, the value is < 1)
     */
    public static float getResist(LivingEntity entity, SchoolType damageSchool) {
//        if (damageSchool == null)
//            return 1;
//        else
        return 2 - (float) Utils.softCapFormula(entity.getAttributeValue(AttributeRegistry.SPELL_RESIST.get()));
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
