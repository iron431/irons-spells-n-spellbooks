package com.example.testmod.entity.mobs;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizard;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.MagicMissileSpell;
import com.example.testmod.spells.ender.TeleportSpell;
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

    private int seeTime = 0;
    private int attackTime = -1;

    private int attackCount = 0;
    private int movementCount = 0;

    private TeleportSpell teleportSpell = (TeleportSpell) AbstractSpell.getSpell(SpellType.TELEPORT_SPELL, 10);
    private MagicMissileSpell magicMissileSpell = (MagicMissileSpell) AbstractSpell.getSpell(SpellType.MAGIC_MISSILE_SPELL, 1);

    public WizardAttackGoal(SimpleWizard simpleWizard, double pSpeedModifier, int pAttackInterval) {
        this(simpleWizard, pSpeedModifier, pAttackInterval, pAttackInterval);
    }

    public WizardAttackGoal(SimpleWizard simpleWizard, double pSpeedModifier, int pAttackIntervalMin, int pAttackIntervalMax) {
        this.simpleWizard = simpleWizard;
        this.speedModifier = pSpeedModifier;
        this.attackIntervalMin = pAttackIntervalMin;
        this.attackIntervalMax = pAttackIntervalMax;
        this.attackRadius = 20;
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
            //TestMod.LOGGER.debug("WizardAttackGoal.canuse: target:{}", target.getName().getString());
            return true;
        } else {
            attackCount = 0;
            movementCount = 0;
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
        double distanceSquared = this.simpleWizard.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean hasLineOfSight = this.simpleWizard.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (!(distanceSquared > (double) attackRadiusSqr) && seeTime >= 5) {
            //TestMod.LOGGER.debug("WizardAttackGoal.tick.1: distanceSquared: {},attackRadiusSqr: {}, seeTime: {}, attackTime: {}", distanceSquared, attackRadiusSqr, seeTime, attackTime);
            this.simpleWizard.getNavigation().stop();
        } else {
            this.simpleWizard.getNavigation().moveTo(this.target, this.speedModifier);
        }

        this.simpleWizard.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime == 0) {
            if (!hasLineOfSight) {
                return;
            }

            float f = (float) Math.sqrt(distanceSquared) / this.attackRadius;
            float f1 = Mth.clamp(f, 0.1F, 1.0F);

            doAction();

            this.attackTime = Mth.floor(f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
            //TestMod.LOGGER.debug("WizardAttackGoal.tick.2: attackTime.1: {}", attackTime);
        } else if (this.attackTime < 0) {
            this.attackTime = Mth.floor(Mth.lerp(Math.sqrt(distanceSquared) / (double) this.attackRadius, (double) this.attackIntervalMin, (double) this.attackIntervalMax));
            //TestMod.LOGGER.debug("WizardAttackGoal.tick.3: attackTime.2: {}", attackTime);
        }
    }

    private void doAction() {
        attackCount++;

        if (attackCount % 3 == 0) {
            doMovement();
        } else {
            doAttack();
        }
    }

    private void doAttack() {
        simpleWizard.lookAt(target, 360, 360);
        //simpleWizard.getLookControl().setLookAt(target, 30, 30);
        TestMod.LOGGER.debug("WizardAttackGoal.doAttack: {}, {}, {}", simpleWizard.getLookAngle(), target.getName().getString(), simpleWizard.getLookControl().isLookingAtTarget());
        magicMissileSpell.onCast(this.simpleWizard.level, simpleWizard, null);
    }

    private void doMovement() {
        TestMod.LOGGER.debug("WizardAttackGoal.doMovement");

        var rotation = target.getLookAngle().normalize().scale(-15);
        var pos = target.position();
        var dest = rotation.add(pos);

        teleportSpell.setTeleportLocation(simpleWizard, dest);
        teleportSpell.onCast(simpleWizard.level, simpleWizard, null);
    }
}