package io.redspace.ironsspellbooks.entity.mobs.goals;

import io.redspace.ironsspellbooks.entity.mobs.SupportMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.function.Predicate;

public class FindSupportableTargetGoal<M extends Mob & SupportMob> extends NearestAttackableTargetGoal<LivingEntity> {
    SupportMob supportMob;

    public FindSupportableTargetGoal(M pMob, Class pTargetType, boolean pMustSee, Predicate<LivingEntity> pTargetPredicate) {
        super(pMob, pTargetType, 40, pMustSee, false, pTargetPredicate);
        this.supportMob = pMob;
        this.targetConditions = TargetingConditions.forNonCombat().range(this.getFollowDistance() * 2).selector(pTargetPredicate);
    }

    @Override
    public void start() {
        //IronsSpellbooks.LOGGER.debug("FindSupportableTargetGoal starting: {}", this.target.getName().getString());
        super.start();
        this.supportMob.setSupportTarget(this.target);
        this.mob.setTarget(null);
    }

    @Override
    public boolean canContinueToUse() {
        return this.supportMob.getSupportTarget() != null && (mob.tickCount % 20 != 0 || targetConditions.test(this.mob, this.supportMob.getSupportTarget()));
    }

    @Override
    public void stop() {
        this.supportMob.setSupportTarget(null);
        this.targetMob = null;
    }
}
