package com.example.testmod.entity.mobs;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizard;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class WizardAttackGoal extends Goal {
    private final SimpleWizard simpleWizard;
    private LivingEntity target;
    private final double speedModifier;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;

    private int seeTime;
    private int attackTime = -1;

    public WizardAttackGoal(SimpleWizard simpleWizard, double pSpeedModifier, int pAttackInterval) {
        this(simpleWizard, pSpeedModifier, pAttackInterval, pAttackInterval);
    }

    public WizardAttackGoal(SimpleWizard simpleWizard, double pSpeedModifier, int pAttackIntervalMin, int pAttackIntervalMax) {
        this.simpleWizard = simpleWizard;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackIntervalMax = pAttackIntervalMax;
        this.attackRadius = 30;
        this.attackRadiusSqr = attackRadius * attackRadius;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        LivingEntity livingentity = this.simpleWizard.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            TestMod.LOGGER.debug("WizardAttackGoal.canuse: target:{}", target.getName().getString());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !this.simpleWizard.getNavigation().isDone();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        double d0 = this.simpleWizard.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean flag = this.simpleWizard.getSensing().hasLineOfSight(this.target);
        if (flag) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (!(d0 > (double) this.attackRadiusSqr) && this.seeTime >= 5) {
            this.simpleWizard.getNavigation().stop();
        } else {
            this.simpleWizard.getNavigation().moveTo(this.target, this.speedModifier);
        }

        this.simpleWizard.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime == 0) {
            if (!flag) {
                return;
            }

            float f = (float) Math.sqrt(d0) / this.attackRadius;
            float f1 = Mth.clamp(f, 0.1F, 1.0F);

            //TODO: attack here

            //this.simpleWizard.performRangedAttack(this.target, f1);
            this.attackTime = Mth.floor(f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
        } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(d0) / (double) this.attackRadius, (double) this.attackIntervalMin, (double) this.attackIntervalMax));
        }
    }
}