package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.entity.mobs.wizards.GenericAnimatedWarlockAttackGoal;

public class IceSpiderAttackGoal extends GenericAnimatedWarlockAttackGoal<IceSpiderEntity> {
    public IceSpiderAttackGoal(IceSpiderEntity abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval);
    }

    @Override
    public void handleAttackLogic(double distanceSquared) {
        if (mob.getGrappleTargetUUID() != null) {
            //pause attacking while we are biting our enemy
            return;
        }
        super.handleAttackLogic(distanceSquared);
    }

    @Override
    public void playSwingSound() {
        //todo: spider sounds
    }

//    @Override
//    protected AttackAnimationData getNextAttack(float distanceSquared) {
//        if (this.moveList.isEmpty()) {
//            return null;
//        }
//        //fixme: this immediately breaks when adding new attacks, but the super grapple attack is the final one so...
//        int i;
//        if (distanceSquared > 5 * 5 || mob.getRandom().nextFloat() < 0.25) {
//            i = mob.getRandom().nextInt(moveList.size()); // allow all attacks
//        } else {
//            i = mob.getRandom().nextInt(moveList.size() - 1); // prevent jump attack
//        }
//        return moveList.get(i);
//    }

//    @Override
//    protected boolean handleDamaging(LivingEntity target, AttackKeyframe attackData) {
//        boolean flag = super.handleDamaging(target, attackData);
//        if (flag && attackData instanceof GrappleKeyframe) {
//            this.mob.startGrapple(target);
//        }
//        return flag;
//    }
//
//    @Override
//    protected void onHitFrame(AttackKeyframe attackKeyframe, float meleeRange) {
//        if (attackKeyframe instanceof JumpKeyframe) {
//            //todo: sound effect
//            float f = 0;
//            if (mob.getTarget() != null) {
//                f = Mth.clampedLerp(.25f, 2, mob.distanceTo(mob.getTarget()) / 16f);
//            }
//            Vec3 lunge = attackKeyframe.lungeVector().scale(f).yRot(-Utils.getAngle(mob.getX(), mob.getZ(), target.getX(), target.getZ()) - Mth.HALF_PI);
//            doLunge(lunge, meleeRange);
//        } else {
//            super.onHitFrame(attackKeyframe, meleeRange);
//        }
//    }
}
