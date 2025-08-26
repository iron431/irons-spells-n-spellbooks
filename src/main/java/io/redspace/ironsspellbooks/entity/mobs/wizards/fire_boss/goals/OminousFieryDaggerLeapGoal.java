package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.goals;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.entity.spells.fiery_dagger.FieryDaggerEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class OminousFieryDaggerLeapGoal extends AnimatedActionGoal<FireBossEntity> {
    public OminousFieryDaggerLeapGoal(FireBossEntity mob) {
        super(mob);
    }

    @Override
    protected boolean canStartAction() {
        return mob.isOminous() && mob.onGround() && mob.getTarget() != null && mob.distanceToSqr(mob.getTarget()) > 6 * 6;
    }

    @Override
    protected int getActionTimestamp() {
        return 38;
    }

    @Override
    protected int getActionDuration() {
        return 70;
    }

    @Override
    protected int getCooldown() {
        return 0;//Utils.random.nextIntBetweenInclusive(8 * 20, 12 * 20);
    }

    @Override
    protected String getAnimationId() {
        return "fire_boss_acrobatic_dagger_throw";
    }

    private static int JUMP_TIMESTAMP = 16;
    @Override
    public void tick() {
        //todo: make sure look control is working
//        if (mob.getTarget() != null) {
//            mob.attackGoal.setTarget(mob.getTarget());
//            mob.attackGoal.doMovement(mob.distanceToSqr(mob.getTarget()));
//        }
        super.tick();
        if (this.abilityTimer == JUMP_TIMESTAMP) {
            mob.setDeltaMovement(0, 0.75, 0); // leap into air
            //todo: effects (shockwave, sound)
        }
//        if(this.abilityTimer == JUMP_TIMESTAMP + 20){
//            mob.addEffect(new MobEffectInstance(MobEffectRegistry.ANTIGRAVITY,))
//        }
        if (abilityTimer == getActionTimestamp() - 15) {
            mob.procSpectralDagger();
        }
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    protected void doAction() {
        var primaryTarget = mob.getTarget();
        if (primaryTarget != null) {
            var type = primaryTarget.getClass();
            var targets = mob.level.getEntitiesOfClass(type, mob.getBoundingBox().inflate(32));
            Vec3 start = mob.getEyePosition();

            for (Entity target : targets) {
                mob.playSound(SoundRegistry.FIERY_DAGGER_THROW.get(), 2f, Utils.random.nextIntBetweenInclusive(80, 110) * .01f);

                Vec3 targetPos = target.position();
                Vec3 deltaAim = targetPos.subtract(start);
                Vec3 aim = start.add(deltaAim);
                int delay = 20;

                FieryDaggerEntity dagger = new FieryDaggerEntity(mob.level);
                dagger.setOwner(mob);
                dagger.setPos(start);
                dagger.delay = delay;
                dagger.setDamage((float) (mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * .75));
                dagger.setExplosionRadius(4 + Utils.random.nextFloat() * 2);
                dagger.setNoGravity(false);

                Vec3 horizontal = aim.subtract(start).multiply(1, 0, 1);
                double horizontalSpeed = 1 * Mth.cos(Mth.PI * .25f) + 0.5; // + 0.5 for extra oomph
                double distance = horizontal.length();
                double ticks = distance / horizontalSpeed;

                // y(t) = -1/2(g)(t^2) + v0*t
                // => v0 = [y1 + 1/2(g)(t1^2)]/t1
                double y1 = aim.y - start.y;
                double g = dagger.getGravity();
                double verticalSpeed = (y1 + 0.5 * g * ticks * ticks) / ticks;
                Vec3 trajectory = horizontal.normalize().scale(horizontalSpeed).add(0, verticalSpeed, 0);
                dagger.setDeltaMovement(trajectory);
                mob.level.addFreshEntity(dagger);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        mob.attackGoal.setTarget(null);
    }
}
