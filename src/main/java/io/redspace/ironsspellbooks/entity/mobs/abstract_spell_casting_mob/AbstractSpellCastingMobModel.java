package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public abstract class AbstractSpellCastingMobModel extends AnimatedGeoModel<AbstractSpellCastingMob> {

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return AbstractSpellCastingMob.modelResource;
    }

    @Override
    public abstract ResourceLocation getTextureResource(AbstractSpellCastingMob mob);

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return AbstractSpellCastingMob.animationInstantCast;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        if (Minecraft.getInstance().isPaused() || !entity.shouldBeExtraAnimated())
            return;

        float partialTick = animationEvent.getPartialTick();
        /*
                This overrides all other animation
         */
        IBone head = this.getAnimationProcessor().getBone(PartNames.HEAD);
        IBone body = this.getAnimationProcessor().getBone(PartNames.BODY);
        IBone rightArm = this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM);
        IBone leftArm = this.getAnimationProcessor().getBone(PartNames.LEFT_ARM);
        IBone rightLeg = this.getAnimationProcessor().getBone(PartNames.RIGHT_LEG);
        IBone leftLeg = this.getAnimationProcessor().getBone(PartNames.LEFT_LEG);

        /*
            Head Controls
         */
        //Make the head look forward, whatever forward is (influenced externally, such as a lootAt target)
        if (!entity.isAnimating() || entity.shouldAlwaysAnimateHead()) {
            head.setRotationY(Mth.lerp(partialTick,
                    Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                    Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD));
            head.setRotationX(Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD);
        }
        /*
            Crazy Vanilla Magic Calculations (LivingEntityRenderer:116 & HumanoidModel#setupAnim
         */
        float pLimbSwingAmount = 0.0F;
        float pLimbSwing = 0.0F;
        if (entity.isAlive()) {
            pLimbSwingAmount = Mth.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
            pLimbSwing = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
            if (entity.isBaby()) {
                pLimbSwing *= 3.0F;
            }

            if (pLimbSwingAmount > 1.0F) {
                pLimbSwingAmount = 1.0F;
            }
        }
        float f = 1.0F;
        if (entity.getFallFlyingTicks() > 4) {
            f = (float) entity.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f *= f * f;
        }

        if (f < 1.0F) {
            f = 1.0F;
        }
        /*
            Leg Controls
         */
        if (entity.isPassenger() && entity.getVehicle().shouldRiderSit()) {
            //If we are riding something, pose ourselves sitting
            rightLeg.setRotationX(1.4137167F);
            rightLeg.setRotationY(-(float) Math.PI / 10F);
            rightLeg.setRotationZ(-0.07853982F);
            leftLeg.setRotationX(1.4137167F);
            leftLeg.setRotationY((float) Math.PI / 10F);
            leftLeg.setRotationZ(0.07853982F);
        } else if (!entity.isAnimating() || entity.shouldAlwaysAnimateLegs()) {
            float strength = .75f;
            Vec3 facing = entity.getForward().multiply(1, 0, 1).normalize();
            Vec3 momentum = entity.getDeltaMovement().multiply(1, 0, 1).normalize();
            Vec3 facingOrth = new Vec3(-facing.z, 0, facing.x);
            float directionf = (float) facing.dot(momentum);
            float directions = (float) facingOrth.dot(momentum) * .35f; //scale side to side movement so they dont rip off thier own legs
            float rightLat = -Mth.sin(pLimbSwing * 0.6662F) * 4 * pLimbSwingAmount;
            float leftLat = -Mth.sin(pLimbSwing * 0.6662F - Mth.PI) * 4 * pLimbSwingAmount;
            updatePosition(rightLeg, rightLat * directions, Mth.cos(pLimbSwing * 0.6662F) * 4 * strength * pLimbSwingAmount, rightLat * directionf);
            updatePosition(leftLeg, leftLat * directions, Mth.cos(pLimbSwing * 0.6662F - Mth.PI) * 4 * strength * pLimbSwingAmount, leftLat * directionf);
            rightLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount * strength);
            leftLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount * strength);
            if (entity.bobBodyWhileWalking()) {
                updatePosition(body, 0, Mth.abs(Mth.cos((pLimbSwing * 1.2662F - Mth.PI * .5f) * .5f)) * 2 * strength * pLimbSwingAmount, 0);
            }
        }
        /*
            Arm Controls
         */
        if (!entity.isAnimating()) {
            addRotationX(rightArm, Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f);
            addRotationX(leftArm, Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f);
            bobBone(rightArm, entity.tickCount, 1);
            bobBone(leftArm, entity.tickCount, -1);
            if (entity.isDrinkingPotion()) {
                addRotationX(entity.isLeftHanded() ? leftArm : rightArm, 35 * Mth.DEG_TO_RAD);
                addRotationZ(entity.isLeftHanded() ? leftArm : rightArm, (entity.isLeftHanded() ? 15 : -15) * Mth.DEG_TO_RAD);
                addRotationY(entity.isLeftHanded() ? leftArm : rightArm, (entity.isLeftHanded() ? -25 : 25) * Mth.DEG_TO_RAD);
            }
        } else if (entity.shouldPointArmsWhileCasting()) {
            addRotationX(rightArm, -entity.getXRot() * Mth.DEG_TO_RAD);
            addRotationX(leftArm, -entity.getXRot() * Mth.DEG_TO_RAD);
        }

//        rightArm.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f);
//        leftArm.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f);

        //rightLeg.yRot = 0.0F;
        //leftLeg.yRot = 0.0F;
        //rightLeg.zRot = 0.0F;
        //leftLeg.zRot = 0.0F;

    }

    protected void bobBone(IBone bone, int offset, float multiplier) {
        //Copied from AnimationUtils#bobLimb
        float z = multiplier * (Mth.cos(offset * 0.09F) * 0.05F + 0.05F);
        float x = multiplier * Mth.sin(offset * 0.067F) * 0.05F;
        addRotationZ(bone, z);
        addRotationX(bone, x);
        //bone.setRotationX(bone.getRotationX() + x);
        //bone.setRotationZ(bone.getRotationZ() + z);

    }

    protected void addRotationX(IBone bone, float rotation) {
        bone.setRotationX(wrapRadians(bone.getRotationX() + rotation));
    }

    protected void addRotationZ(IBone bone, float rotation) {
        bone.setRotationZ(wrapRadians(bone.getRotationZ() + rotation));
    }

    protected void addRotationY(IBone bone, float rotation) {
        bone.setRotationY(wrapRadians(bone.getRotationY() + rotation));
    }

    public static float wrapRadians(float pValue) {
        float twoPi = 6.2831f;
        float pi = 3.14155f;
        float f = pValue % twoPi;
        if (f >= pi) {
            f -= twoPi;
        }

        if (f < -pi) {
            f += twoPi;
        }

        return f;
    }

    private static void updatePosition(IBone bone, float x, float y, float z) {
        bone.setPositionX(x);
        bone.setPositionY(y);
        bone.setPositionZ(z);
    }

    private static void updateRotation(IBone bone, float x, float y, float z) {
        bone.setRotationX(x);
        bone.setRotationY(y);
        bone.setRotationZ(z);
    }
}