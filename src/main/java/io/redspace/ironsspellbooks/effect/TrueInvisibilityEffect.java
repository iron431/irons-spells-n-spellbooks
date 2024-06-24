package io.redspace.ironsspellbooks.effect;


import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class TrueInvisibilityEffect extends MagicMobEffect {
    public TrueInvisibilityEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    int lastHurtTimestamp;

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int pAmplifier) {
        super.onEffectAdded(pLivingEntity, pAmplifier);
        if (livingEntity instanceof Player || livingEntity instanceof AbstractSpellCastingMob) {
            MagicData.getPlayerMagicData(livingEntity).getSyncedData().addEffects(SyncedSpellData.TRUE_INVIS);
        }

        var targetingCondition = TargetingConditions.forCombat().ignoreLineOfSight().selector(e -> {
            //IronsSpellbooks.LOGGER.debug("InvisibilitySpell TargetingConditions:{}", e);
            return (((Mob) e).getTarget() == livingEntity);
        });

        //remove aggro from anything targeting us
        livingEntity.level.getNearbyEntities(Mob.class, targetingCondition, livingEntity, livingEntity.getBoundingBox().inflate(40D))
                .forEach(entityTargetingCaster -> {
                    //IronsSpellbooks.LOGGER.debug("InvisibilitySpell Clear Target From:{}", entityTargetingCaster);
                    entityTargetingCaster.setTarget(null);
                    entityTargetingCaster.setLastHurtMob(null);
                    entityTargetingCaster.setLastHurtByMob(null);
                    entityTargetingCaster.targetSelector.getAvailableGoals().forEach(WrappedGoal::stop);
                });
        this.lastHurtTimestamp = livingEntity.getLastHurtMobTimestamp();

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        //If we attack, we lose invis
        //TODO: can be optimized via use of event instead of checking every tick
        return pLivingEntity.level.isClientSide || lastHurtTimestamp == pLivingEntity.getLastHurtMobTimestamp();
    }

    @Override
    public void onEffectRemoved(LivingEntity livingEntity, int pAmplifier) {
        super.onEffectRemoved(livingEntity, pAmplifier);
        if (livingEntity instanceof Player || livingEntity instanceof AbstractSpellCastingMob) {
            MagicData.getPlayerMagicData(livingEntity).getSyncedData().removeEffects(SyncedSpellData.TRUE_INVIS);
        }
    }
}
