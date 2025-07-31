package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

public class MomentHurtByTargetGoal extends HurtByTargetGoal {
    int forcedAggroTime;
    float intensity;
    boolean isOutnumbered;

    public MomentHurtByTargetGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
        super(pMob, pToIgnoreDamage);
    }

    @Override
    public void tick() {
        super.tick();
        // if we continue to take damage while we are processing this goal, decide whether we should stop processing this goal, or double down
        if (this.timestamp != this.mob.getLastHurtByMobTimestamp()) {
            this.timestamp = this.mob.getLastHurtByMobTimestamp();
            if (mob.getLastHurtByMob() != null && mob.getLastHurtByMob() != targetMob) {
                // multiple mobs are attacking us, begin more intelligent state tracking
                isOutnumbered = true;
                // if we are being attacked by new mobs, hasten our re-decision time
                forcedAggroTime -= 20;
                // begin waning intensity we care about the current mob
                intensity *= .8f;
            } else if (isOutnumbered) {
                // if we are being attacked by the same mob, continue to fight it.
                forcedAggroTime += (int) (20 * intensity);
            }
        }
    }

    @Override
    public void start() {
        super.start();
        this.forcedAggroTime = 40 + this.mob.getRandom().nextInt(80) + this.mob.getRandom().nextInt(80);
        intensity = 1f;
        isOutnumbered = false;
    }

    @Override
    public boolean canContinueToUse() {
        return  (!isOutnumbered || --forcedAggroTime > 0) && super.canContinueToUse();
    }
}
