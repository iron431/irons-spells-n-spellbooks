package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.api.entity.NoKnockbackProjectile;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.MagicSummon;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;

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

    public static boolean applyDamage(Entity target, float baseAmount, DamageSource damageSource) {
        if (target instanceof LivingEntity livingTarget && damageSource instanceof SpellDamageSource spellDamageSource) {
            var e = new SpellDamageEvent(livingTarget, baseAmount, spellDamageSource);
            if (MinecraftForge.EVENT_BUS.post(e)) {
                return false;
            }
            baseAmount = e.getAmount();
            float adjustedDamage = baseAmount * getResist(livingTarget, spellDamageSource.spell.getSchoolType());
            if (damageSource.getDirectEntity() instanceof NoKnockbackProjectile) {
                ignoreNextKnockback(livingTarget);
            }
            if (damageSource.getEntity() instanceof LivingEntity livingAttacker) {
                if (isFriendlyFireBetween(livingAttacker, livingTarget)) {
                    return false;
                }
                livingAttacker.setLastHurtMob(target);
            }
            return livingTarget.hurt(damageSource, adjustedDamage);
        } else {
            return target.hurt(damageSource, baseAmount);
        }
    }

    //I can't tell if this is genius or incredibly stupid
    private static final HashMap<LivingEntity, Integer> knockbackImmunes = new HashMap<>();

    public static void ignoreNextKnockback(LivingEntity livingEntity) {
        if (!livingEntity.level.isClientSide) {
            knockbackImmunes.put(livingEntity, livingEntity.tickCount);
        }
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

    @SubscribeEvent
    public static void preHitEffects(LivingHurtEvent event) {
        var damageSource = event.getSource();
        MagicSummon fromSummon = damageSource.getDirectEntity() instanceof MagicSummon summon ? summon : damageSource.getEntity() instanceof MagicSummon summon ? summon : null;
        if (fromSummon != null) {
            if (fromSummon.getSummoner() != null) {
                event.setAmount(event.getAmount() * (float) fromSummon.getSummoner().getAttributeValue(AttributeRegistry.SUMMON_DAMAGE.get()));
            }
            if (fromSummon instanceof LivingEntity livingSummon) {
                event.getEntity().setLastHurtByMob(livingSummon);
            }
        }
    }

    @SubscribeEvent
    public static void postHitEffects(LivingDamageEvent event) {
        if (event.getSource() instanceof SpellDamageSource spellDamageSource && spellDamageSource.hasPostHitEffects()) {
            float actualDamage = event.getAmount();
            var target = event.getEntity();
            var attacker = event.getSource().getEntity();
            if (attacker instanceof LivingEntity livingAttacker) {
                if (spellDamageSource.getLifestealPercent() > 0) {
                    livingAttacker.heal(spellDamageSource.getLifestealPercent() * actualDamage);
                }
            }
            if (spellDamageSource.getFreezeTicks() > 0 && target.canFreeze()) {
                //Freeze ticks count down by 2, so we * 2 so the spell damages source can be dumb
                target.setTicksFrozen(target.getTicksFrozen() + spellDamageSource.getFreezeTicks() * 2);
            }
            if (spellDamageSource.getFireTime() > 0) {
                target.setSecondsOnFire(spellDamageSource.getFireTime());
            }
            if (spellDamageSource.getIFrames() >= 0) {
                target.invulnerableTime = spellDamageSource.getIFrames();
            }
        }
    }

    public static boolean isFriendlyFireBetween(Entity attacker, Entity target) {
        if (attacker == null || target == null)
            return false;
        if (attacker.isPassengerOfSameVehicle(target)) {
            return true;
        }
        if (attacker instanceof Player playerAttacker && target instanceof Player playertarget
                && !playerAttacker.canHarmPlayer(playertarget)) {
            return true;
        }
        var team = attacker.getTeam();
        if (team != null) {
            return team.isAlliedTo(target.getTeam()) && !team.isAllowFriendlyFire();
        }
        //We already manually checked for teams, so this will only return true for any overrides (such as summons)
        return attacker.isAlliedTo(target);
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
        var baseResist = entity.getAttributeValue(AttributeRegistry.SPELL_RESIST.get());
        if (damageSchool == null)
            return 2 - (float) Utils.softCapFormula(baseResist);
        else
            return 2 - (float) Utils.softCapFormula(damageSchool.getResistanceFor(entity) * baseResist);
    }
}
