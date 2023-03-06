package com.example.testmod.entity.mobs.dead_king_boss;

import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.goals.WarlockAttackGoal;

public class DeadKingAnimatedWarlockAttackGoal extends WarlockAttackGoal {
    public DeadKingAnimatedWarlockAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval, float meleeRange) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval, meleeRange);
    }

    int meleeAnimTimer = -1;
    //measured from animation
    int animationDuration = 40;
    int firstSwingTimestamp = animationDuration - 14;
    int secondSwingTimestamp = animationDuration - 20;

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        if (meleeAnimTimer > 0) {

            meleeAnimTimer--;
            if (meleeAnimTimer == firstSwingTimestamp || meleeAnimTimer == secondSwingTimestamp) {
                if (distanceSquared <= meleeRange * meleeRange) {
                    this.mob.doHurtTarget(target);
                    target.invulnerableTime = 0;
                }
            }
            //to prevent exiting melee mode while animating
            meleeTimeDelay++;
        } else if (meleeAnimTimer == 0) {
            resetAttackTimer(distanceSquared);
            meleeAnimTimer = -1;
        }
        super.handleAttackLogic(distanceSquared);
    }

    @Override
    protected void doMeleeAction() {
        //anim duration
        meleeAnimTimer = animationDuration;
    }
}
