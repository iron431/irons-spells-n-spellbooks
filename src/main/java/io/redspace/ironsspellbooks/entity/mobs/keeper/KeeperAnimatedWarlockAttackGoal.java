package io.redspace.ironsspellbooks.entity.mobs.keeper;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.goals.AttackAnimationData;
import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import io.redspace.ironsspellbooks.network.ClientboundSyncAnimation;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Random;

public class KeeperAnimatedWarlockAttackGoal extends WarlockAttackGoal {
    final KeeperEntity keeper;

    public KeeperAnimatedWarlockAttackGoal(KeeperEntity abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval, float meleeRange) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval, meleeRange);
        keeper = abstractSpellCastingMob;
        nextAttack = randomizeNextAttack(0);
        this.meleeBias = 1f;
        this.wantsToMelee = true;
    }

    int meleeAnimTimer = -1;
    public KeeperEntity.AttackType currentAttack;
    public KeeperEntity.AttackType nextAttack;
    public KeeperEntity.AttackType queueCombo;
    private boolean hasLunged;
    private boolean hasHitLunge;

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        //Handling Animation hit frames
        float distance = Mth.sqrt((float) distanceSquared);
        //mob.lookAt(target, 300, 300);

        if (meleeAnimTimer > 0) {
            //We are currently attacking and are in a melee animation
            forceFaceTarget();
            meleeAnimTimer--;
            if (currentAttack.data.isHitFrame(meleeAnimTimer)) {
                //IronsSpellbooks.LOGGER.debug("KeeperAnimatedAttack: hit frame | currentAttack: {}", currentAttack);
                if (currentAttack == KeeperEntity.AttackType.Lunge) {
                    if (!hasLunged) {
                        Vec3 lunge = target.position().subtract(mob.position()).normalize().multiply(2.4, .5, 2.4).add(0, 0.15, 0);
                        mob.push(lunge.x, lunge.y, lunge.z);
                        hasLunged = true;
                    }
                    if (!hasHitLunge && distance <= meleeRange * .6f) {
                        this.mob.doHurtTarget(target);
                        target.knockback(1, Mth.sin(mob.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(mob.getYRot() * ((float) Math.PI / 180F)));

                        hasHitLunge = true;
                    }
                } else {
                    //mob.lookAt(target, 300, 300);
                    Vec3 lunge = target.position().subtract(mob.position()).normalize().scale(.55f)/*.add(0, 0.2, 0)*/;
                    mob.push(lunge.x, lunge.y, lunge.z);
                    if (distance <= meleeRange) {
                        boolean flag = this.mob.doHurtTarget(target);
                        target.invulnerableTime = 0;
                        if (currentAttack.data.isSingleHit() && ((flag && mob.getRandom().nextFloat() < .75f) || target.isBlocking())) {
                            //Attack again! combos!
                            queueCombo = randomizeNextAttack(0);
                        }
                    }
                }

            }
        } else if (queueCombo != null) {
            nextAttack = queueCombo;
            queueCombo = null;
            doMeleeAction();
        } else if (meleeAnimTimer == 0) {
            //Reset animations/attack
            nextAttack = randomizeNextAttack(distance);
            resetAttackTimer(distanceSquared);
            meleeAnimTimer = -1;
        } else {
            //Handling attack delay
            mob.lookAt(target, 15, 15);
            if (distance < meleeRange * (nextAttack == KeeperEntity.AttackType.Lunge ? 3 : 1)) {
                if (--this.attackTime == 0) {
                    doMeleeAction();
                } else if (this.attackTime < 0) {
                    resetAttackTimer(distanceSquared);
                }
            } else if (--this.attackTime < 0) {
                //Always keep the clock running, eventually he'll lunge to close distance. Otherwise he can be kited incredibly easily
                resetAttackTimer(distanceSquared);
                nextAttack = randomizeNextAttack(distance);
            }
        }
    }

    private KeeperEntity.AttackType randomizeNextAttack(float distance) {
        //Lunge is the last enum. If we are close, no need to lunge.
        int i;
        if (distance < meleeRange * 1.5f) {
            i = KeeperEntity.AttackType.values().length - 1;
        } else {
            i = KeeperEntity.AttackType.values().length;
        }
        return KeeperEntity.AttackType.values()[mob.getRandom().nextInt(i)];
    }


    private void forceFaceTarget() {
        if (hasLunged)
            return;
        double d0 = target.getX() - mob.getX();
        double d1 = target.getZ() - mob.getZ();
        float yRot = (float) (Mth.atan2(d1, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
        mob.setYBodyRot(yRot);
        mob.setYHeadRot(yRot);
        mob.setYRot(yRot);
    }

    @Override
    protected void doMeleeAction() {
        //anim duration
        currentAttack = nextAttack;
        if (currentAttack != null) {
            this.mob.swing(InteractionHand.MAIN_HAND);
            meleeAnimTimer = currentAttack.data.lengthInTicks;
            hasLunged = false;
            hasHitLunge = false;
            Messages.sendToPlayersTrackingEntity(new ClientboundSyncAnimation<>(currentAttack.ordinal(), keeper), keeper);
        }
    }

    @Override
    protected void doMovement(double distanceSquared) {
        if (distanceSquared > meleeRange * meleeRange) {
            if (isFlying)
                this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), this.speedModifier * 1.3f);
            else
                this.mob.getNavigation().moveTo(this.target, this.speedModifier * 1.3f);
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.meleeAnimTimer = -1;
    }
}
