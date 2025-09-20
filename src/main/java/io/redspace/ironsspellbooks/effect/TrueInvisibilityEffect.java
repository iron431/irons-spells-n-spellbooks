package io.redspace.ironsspellbooks.effect;


import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class TrueInvisibilityEffect extends MagicMobEffect implements ISyncedMobEffect {
    public TrueInvisibilityEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    int lastHurtTimestamp;

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int pAmplifier) {
        super.onEffectAdded(livingEntity, pAmplifier);

        var targetingCondition = TargetingConditions.forCombat().ignoreLineOfSight().selector(e -> {
            //IronsSpellbooks.LOGGER.debug("InvisibilitySpell TargetingConditions:{}", e);
            return (((Mob) e).getTarget() == livingEntity);
        });

        //remove aggro from anything targeting us
        livingEntity.level.getNearbyEntities(Mob.class, targetingCondition, livingEntity, livingEntity.getBoundingBox().inflate(40D))
                .forEach(entityTargetingCaster -> {
                    //IronsSpellbooks.LOGGER.debug("InvisibilitySpell Clear Target From:{}", entityTargetingCaster);
                    entityTargetingCaster.setTarget(null);
                    entityTargetingCaster.targetSelector.getAvailableGoals().forEach(WrappedGoal::stop);
                    entityTargetingCaster.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                });
        this.lastHurtTimestamp = livingEntity.getLastHurtMobTimestamp();

    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        //If we attack, we lose invis
        if (!pLivingEntity.level.isClientSide && lastHurtTimestamp != pLivingEntity.getLastHurtMobTimestamp()){
            //Ironsspellbooks.logger.debug("TrueInvisibilityEffect.applyEffectTick: entity attacked, removing effect");
            pLivingEntity.removeEffect(this);
        }
    }

}
