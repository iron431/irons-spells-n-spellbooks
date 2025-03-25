package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class FireBossMoveControl extends MoveControl {
    int customMovementTimer;
    int customMovementDuration;
    Function<Float, Vec3> currentCustomMovementControl;

    public FireBossMoveControl(Mob pMob) {
        super(pMob);
    }

    @Override
    public void tick() {
        if (customMovementTimer > 0) {
            customMovementTimer--;
            var target = mob.getTarget();
            if (target != null) {
                float f = Math.clamp(customMovementTimer / (float) customMovementDuration, 0, 1f);
                Vec3 movement = currentCustomMovementControl.apply(f).scale(mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                float angle = -Utils.getAngle(mob.getX(), mob.getZ(), target.getX(), target.getZ()) - Mth.HALF_PI;
                mob.setDeltaMovement(mob.getDeltaMovement().add(movement.yRot(angle).scale(f * f)));
                float slowdownRange = (float) mob.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) * mob.getScale() * .9f;
                if (mob.distanceToSqr(target) < slowdownRange * slowdownRange) {
                    //rapid deceleration
                    customMovementTimer -= 2;
                }
            } else {
                // stop
                customMovementTimer = 0;
            }
        } else {
            super.tick();
        }
    }
    @Override
    protected float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
        //This fixes a bug where a mob tries to path into the block it's already standing, and spins around trying to look "forward"
        //We nullify our rotation calculation if we are close to block we are trying to get to
        double d0 = this.wantedX - this.mob.getX();
        double d1 = this.wantedZ - this.mob.getZ();
        if (d0 * d0 + d1 * d1 < .5f) {
            return pSourceAngle;
        } else {
            return super.rotlerp(pSourceAngle, pTargetAngle, pMaximumChange * .25f);
        }
    }

    public void triggerCustomMovement(int duration, Function<Float, Vec3> progressToMovement) {
        this.currentCustomMovementControl = progressToMovement;
        customMovementTimer = duration;
        customMovementDuration = duration;
    }
}
