package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DeadKingAnimatedWarlockAttackGoal extends WarlockAttackGoal {
    final DeadKingBoss boss;

    public DeadKingAnimatedWarlockAttackGoal(DeadKingBoss abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval, float meleeRange) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval, meleeRange);
        boss = abstractSpellCastingMob;
    }

    int meleeAnimTimer = -1;
    //measured from animation (both happen to be the exact same length)
    int animationDuration = 40;
    int firstSwingTimestamp = animationDuration - 14;
    int secondSwingTimestamp = animationDuration - 20;
    int swingSoundTimestamp = animationDuration - 13;
    int slamTimestamp = animationDuration - 25;
    int slamSoundTimestamp = animationDuration - 22;
    boolean slam;

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        if (meleeAnimTimer > 0) {
            mob.lookAt(target, 50, 50);
            meleeAnimTimer--;
            if (slam) {
                if (meleeAnimTimer == slamSoundTimestamp)
                    mob.playSound(SoundRegistry.DEAD_KING_SLAM.get());
                if (meleeAnimTimer == slamTimestamp) {
                    Vec3 slamPos = mob.position().add(mob.getForward().multiply(1, 0, 1).normalize());
                    Vec3 bbHalf = new Vec3(meleeRange, meleeRange, meleeRange).scale(.4);
                    mob.level().getEntitiesOfClass(target.getClass(), new AABB(slamPos.subtract(bbHalf), slamPos.add(bbHalf))).forEach((entity) -> {
                        float damage = (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f;
                        entity.hurt(mob.level().damageSources().mobAttack(mob), damage);
                        Utils.throwTarget(mob, entity, 7f, true);
                        //mob.doHurtTarget(entity);
                        //entity.push(0, 1, 0);
                        if (entity instanceof Player player && player.isBlocking())
                            player.disableShield(true);
                    });
                }
            } else {
                if (meleeAnimTimer == swingSoundTimestamp)
                    mob.playSound(SoundRegistry.DEAD_KING_SWING.get());
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
            boss.setNextSlam(mob.getRandom().nextFloat() < .33f);
            meleeAnimTimer = -1;
        }
        super.handleAttackLogic(distanceSquared);
    }

    @Override
    protected void doMeleeAction() {
        //anim duration
        meleeAnimTimer = animationDuration;
        slam = boss.isNextSlam();

    }
}
