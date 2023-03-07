package com.example.testmod.entity.mobs.dead_king_boss;

import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import com.example.testmod.entity.mobs.goals.WarlockAttackGoal;
import com.example.testmod.util.Utils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DeadKingAnimatedWarlockAttackGoal extends WarlockAttackGoal {
    public DeadKingAnimatedWarlockAttackGoal(AbstractSpellCastingMob abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval, float meleeRange) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval, meleeRange);
    }

    int meleeAnimTimer = -1;
    //measured from animation (both happen to be the exact same length)
    int animationDuration = 40;
    int firstSwingTimestamp = animationDuration - 14;
    int secondSwingTimestamp = animationDuration - 20;
    int slamTimestamp = animationDuration - 25;
    boolean slam;

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        if (meleeAnimTimer > 0) {

            meleeAnimTimer--;
            if (slam) {
                if (meleeAnimTimer == slamTimestamp) {
                    Vec3 slamPos = mob.position().add(mob.getForward().multiply(1, 0, 1).normalize());
                    Vec3 bbHalf = new Vec3(1, 1, 1);
                    mob.level.getEntitiesOfClass(target.getClass(), new AABB(slamPos.subtract(bbHalf), slamPos.add(bbHalf))).forEach((entity) -> {
                        float damage = (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f;
                        entity.hurt(DamageSource.mobAttack(mob), damage);
                        Utils.throwTarget(mob, entity, 3f);
                    });
                }
            } else {
                if (meleeAnimTimer == firstSwingTimestamp || meleeAnimTimer == secondSwingTimestamp) {
                    if (distanceSquared <= meleeRange * meleeRange) {
                        this.mob.doHurtTarget(target);
                        target.invulnerableTime = 0;
                    }
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
        //slam = mob.getRandom().nextFloat() < .25f;
        slam = true;
        if (mob instanceof DeadKingBoss boss)
            boss.setSlamming(slam);
    }
}
