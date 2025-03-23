package io.redspace.ironsspellbooks.entity.dragon.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

public class DragonBodyRotationControl extends BodyRotationControl {
    float wantedYRotation;
    private final Mob mob;
    private static final int HEAD_STABLE_ANGLE = 15;
    private static final int DELAY_UNTIL_STARTING_TO_FACE_FORWARD = 10;
    private static final int HOW_LONG_IT_TAKES_TO_FACE_FORWARD = 50;
    private int headStableTime;
    private float lastStableYHeadRot;

    public DragonBodyRotationControl(Mob pMob) {
        super(pMob);
        this.mob = pMob;

    }

    @Override
    public void clientTick() {
        if (this.isMoving()) {
            wantedYRotation = mob.getYRot();
            mob.yBodyRot = Mth.approachDegrees(mob.yBodyRot, wantedYRotation, 4f / Mth.clamp(mob.getScale(), .25f, 4f));
            this.rotateHeadIfNecessary();
            this.lastStableYHeadRot = this.mob.yHeadRot;
            this.headStableTime = 0;
        } else {
            if (this.notCarryingMobPassengers()) {
                if (Math.abs(this.mob.yHeadRot - this.lastStableYHeadRot) > HEAD_STABLE_ANGLE) {
                    this.headStableTime = 0;
                    this.lastStableYHeadRot = this.mob.yHeadRot;
                    this.rotateBodyIfNecessary();
                } else {
                    this.headStableTime++;
                    if (this.headStableTime > DELAY_UNTIL_STARTING_TO_FACE_FORWARD) {
                        this.rotateHeadTowardsFront();
                    }
                }
            }
        }
    }

    private void rotateBodyIfNecessary() {
        this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, (float) this.mob.getMaxHeadYRot());
    }

    private void rotateHeadIfNecessary() {
        this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float) this.mob.getMaxHeadYRot());
    }

    private void rotateHeadTowardsFront() {
        int i = this.headStableTime - DELAY_UNTIL_STARTING_TO_FACE_FORWARD;
        float f = Mth.clamp((float) i / HOW_LONG_IT_TAKES_TO_FACE_FORWARD, 0.0F, 1.0F);
        float f1 = (float) this.mob.getMaxHeadYRot() * (1.0F - f);
        float newRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, f1);
        if (Math.abs(newRot - this.mob.yBodyRot) > 0.1) {
            this.mob.yBodyRot = newRot;
            this.mob.walkAnimation.update(0.25f, .1f);
        } else {
            this.headStableTime = 0;
        }
    }

    private boolean notCarryingMobPassengers() {
        return !(this.mob.getFirstPassenger() instanceof Mob);
    }

    private boolean isMoving() {
        double d0 = this.mob.getX() - this.mob.xo;
        double d1 = this.mob.getZ() - this.mob.zo;
        return d0 * d0 + d1 * d1 > 2.5000003E-7F;
    }
}
