package io.redspace.ironsspellbooks.entity;

import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.goals.GenericFollowOwnerGoal;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;

public class DragonEntity extends PathfinderMob implements Enemy {
    public DragonEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16));
        this.goalSelector.addGoal(1, new GenericFollowOwnerGoal(this, () -> level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 25, null), 1f, 12, 5, false, 100));
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return super.createBodyControl();
    }

    public static AttributeSupplier.Builder dragonAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 2)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.23F)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    public final float hipHeight = 2f;
    public final float hipWidth = 1.5f;
    public float legSpeed = .25f;
    public float stepHeight = 1f;
    public boolean nextStepRight = true;
    public int stepStartTick, stepEndTick;

    Vec3 rightRestPosition = Vec3.ZERO, rightCurrentPosition = Vec3.ZERO, rightTargetPosition = Vec3.ZERO;
    Vec3 rightOldPosition = Vec3.ZERO;
    boolean stepping;
    int stepTick;

    @Override
    public void tick() {
        super.tick();
        rightOldPosition = rightCurrentPosition;
        float bodyRot = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = Vec3.directionFromRotation(0, this.yBodyRot);
        Vec3 hipOffset = new Vec3(-forward.z, 0, forward.x).scale(hipWidth);
        Vec3 centerOfMass = this.getBoundingBox().getCenter();
        rightRestPosition = ground(centerOfMass.add(hipOffset).add(forward.scale(.5f)).add(getDeltaMovement().scale(8)));
        float m = maxFootDistance();
        float d = (float) rightCurrentPosition.distanceToSqr(rightRestPosition);
        if (stepping) {
            stepTick++;
            Vec3 delta = rightTargetPosition.subtract(rightCurrentPosition).normalize().scale(legSpeed);
            //float stepAssist = 2 * Mth.sin(Mth.PI * tickCount) * Mth.PI * Mth.cos(Mth.PI * stepTick) /** 0.05f*/; //f'(x), f(x) = sin(PI * x) ^ 2
            rightCurrentPosition = rightCurrentPosition.add(delta)/*.add(0, stepAssist, 0)*/;
            if (rightCurrentPosition.subtract(rightTargetPosition).lengthSqr() < .2f) {
                stepping = false;
            }
        } else if (d > m * m /*|| (getDeltaMovement().lengthSqr() < .1f && d > 1)*/) {
            if (d > 25 * 25) {
                rightCurrentPosition = rightRestPosition;
            } else {
                rightTargetPosition = rightRestPosition;
                stepping = true;
                stepTick = 0;
            }
        }
    }

    public float maxFootDistance() {
        return (float) (0.5f + getDeltaMovement().horizontalDistanceSqr() > .1f ? 1 : 0);
    }

    public Vec3 ground(Vec3 vec3) {
        return Utils.moveToRelativeGroundLevel(level, vec3, 2, 5);
    }

    public Vec3 getRightFootEffector(float partialTick) {
//        if (isStepping()) {
//
//            float f = (tickCount + partialTick - stepStartTick) / (stepEndTick - stepStartTick);
//            float height = Mth.sin(f * Mth.PI);
//            height *= height * stepHeight;
//            Vec3 effector = getTarget(rightFootTarget, rightFootTargetGoal, f).add(0, height, 0);
//            return effector;
//        } else {
//            return rightFootTarget == null ? Vec3.ZERO : rightFootTarget;
//        }
        return lerpVec(rightOldPosition, rightCurrentPosition, partialTick);
    }

    //    public boolean isSteppingRight() {
//        return rightFootTarget != null && !rightFootTarget.equals(rightFootTargetGoal);
//    }
//
//    public boolean isSteppingLeft() {
//        return leftFootTarget != null && !leftFootTarget.equals(leftFootTargetGoal);
//    }
//
//    public boolean isStepping() {
//        return isSteppingLeft() || isSteppingRight();
//    }
//
    public Vec3 lerpVec(Vec3 a, Vec3 b, float f) {
        return new Vec3(Mth.lerp(f, a.x, b.x), Mth.lerp(f, a.y, b.y), Mth.lerp(f, a.z, b.z));
    }
}
