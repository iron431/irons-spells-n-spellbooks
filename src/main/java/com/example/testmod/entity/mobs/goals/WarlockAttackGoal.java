package com.example.testmod.entity.mobs.goals;

import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.world.InteractionHand;

public class WarlockAttackGoal extends WizardAttackGoal {

    protected float meleeRange;
    protected boolean wantsToMelee;
    protected int meleeTime;
    protected int meleeTimeDelay;

    public WarlockAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval, float meleeRange) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval);
        this.meleeRange = meleeRange;
        meleeTimeDelay = abstractSpellCastingMob.getRandom().nextIntBetweenInclusive(80, 200);
    }

    @Override
    public void tick() {
        super.tick();
        if (++meleeTime > meleeTimeDelay) {
            meleeTime = 0;
            wantsToMelee = !wantsToMelee;
            meleeTimeDelay = mob.getRandom().nextIntBetweenInclusive(60, 120);
            if(wantsToMelee)
                meleeTimeDelay += 120;
        }
    }

    @Override
    protected void doMovement(double distanceSquared) {
        if (!wantsToMelee) {
            super.doMovement(distanceSquared);
            return;
        }

        float strafeBackwards = 0;

        if (distanceSquared > meleeRange * meleeRange) {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier * 1.3f);
        } else {
            strafeBackwards = (float) (-speedModifier * .25f);
        }
        //we do a little strafing
        if (++strafeTime > 25) {
            if (mob.getRandom().nextDouble() < .1) {
                strafingClockwise = !strafingClockwise;
                strafeTime = 0;
            }
        }

        float strafeDir = strafingClockwise ? 1f : -1f;
        mob.getMoveControl().strafe(strafeBackwards, (float) speedModifier * strafeDir);
        mob.lookAt(target, 30, 30);

    }

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        if (!wantsToMelee || distanceSquared > meleeRange * meleeRange) {
            super.handleAttackLogic(distanceSquared);
        } else if (--this.attackTime == 0) {
            this.mob.swing(InteractionHand.MAIN_HAND);
            doMeleeAction();
        }

    }

    protected void doMeleeAction() {
        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        this.mob.doHurtTarget(target);
        resetAttackTimer(distanceSquared);
    }
}
