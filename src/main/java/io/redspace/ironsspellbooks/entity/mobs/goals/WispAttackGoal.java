package io.redspace.ironsspellbooks.entity.mobs.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class WispAttackGoal extends Goal {
    private LivingEntity target;
    private PathfinderMob wisp;
    private double speedModifier;

    public WispAttackGoal(PathfinderMob wisp, double speedModifier) {
        this.wisp = wisp;
        this.speedModifier = speedModifier;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        var livingentity = this.wisp.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            //irons_spellbooks.LOGGER.debug("WizardAttackGoal.canuse: target:{}", target.getName().getString());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !this.wisp.getNavigation().isDone();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.target = null;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        double distanceSquared = this.target.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean hasLineOfSight = this.wisp.getSensing().hasLineOfSight(this.target);
        var moveResult = this.wisp.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), this.speedModifier);

        //irons_spellbooks.LOGGER.debug("WispAttackGoal.tick: moveResult:{}, hasLineOfSight:{}", moveResult, hasLineOfSight);

        this.wisp.getLookControl().setLookAt(this.target, 180, 180);
    }

    private void doAction() {

    }
}