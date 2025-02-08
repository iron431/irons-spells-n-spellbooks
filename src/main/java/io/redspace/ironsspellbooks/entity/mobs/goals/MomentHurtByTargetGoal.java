package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

public class MomentHurtByTargetGoal extends HurtByTargetGoal {
    int forcedAggroTime;
    float intensity;

    public MomentHurtByTargetGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
        super(pMob, pToIgnoreDamage);
    }

    @Override
    public void stop() {
        return;
    }

    @Override
    public void tick() {
        super.tick();
        // if we continue to take damage while we are processing this goal, decide whether we should stop processing this goal, or double down
        if (this.timestamp != this.mob.getLastHurtByMobTimestamp()) {
            this.timestamp = this.mob.getLastHurtByMobTimestamp();
            if (mob.getLastHurtByMob() != targetMob) {
                // if we are being attacked by new mobs, hasten our re-decision time
                forcedAggroTime -= 20;
            } else {
                // if we are being attacked by the same mob, continue to fight it.
                // however, begin waning intensity we care about this specific mob
                forcedAggroTime += (int) (20 * intensity);
                intensity *= .8f;
            }
        }
    }

    @Override
    public void start() {
        super.start();
        this.forcedAggroTime = 40 + this.mob.getRandom().nextInt(80) + this.mob.getRandom().nextInt(80);
        intensity = 1f;
    }

    @Override
    public boolean canContinueToUse() {
        return --forcedAggroTime > 0 && super.canContinueToUse();
    }
}
