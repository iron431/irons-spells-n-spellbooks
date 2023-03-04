package com.example.testmod.entity.mobs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AbstractSpellCastingMobModel extends AnimatedGeoModel<AbstractSpellCastingMob> {

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return AbstractSpellCastingMob.modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return AbstractSpellCastingMob.textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return AbstractSpellCastingMob.animationInstantCast;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        float partialTick = animationEvent.getPartialTick();
        IBone head = this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.HEAD_BONE_IDENT);
        //head.setRotationY(entity.tickCount * Mth.DEG_TO_RAD);
        IBone body = this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.BODY_BONE_IDENT);
        IBone rightArm = this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.RIGHT_ARM_BONE_IDENT);
        IBone leftArm = this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.LEFT_ARM_BONE_IDENT);
        IBone rightLeg = this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.RIGHT_LEG_BONE_IDENT);
        IBone leftLeg = this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.LEFT_LEG_BONE_IDENT);



        head.setRotationY(Mth.lerp(partialTick,
                Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD));
        head.setRotationX(Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD);
        body.setRotationY(0);

        if (entity.syncedIsCasting())
            return;
        /*
        Copied from LivingEntityRenderer:116
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
        /*
        Copied from HumanoidModel#setupAnim
         */

        float f = 1.0F;
        if (entity.getFallFlyingTicks() > 4) {
            f = (float) entity.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f *= f * f;
        }

        if (f < 1.0F) {
            f = 1.0F;
        }
        rightArm.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f);
        leftArm.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f);
        //rightArm.zRot = 0.0F;
        //leftArm.zRot = 0.0F;
        rightLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount / f);
        leftLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount / f);

        bobBone(rightArm, entity.tickCount, 1);
        bobBone(leftArm, entity.tickCount, -1);
        //rightLeg.yRot = 0.0F;
        //leftLeg.yRot = 0.0F;
        //rightLeg.zRot = 0.0F;
        //leftLeg.zRot = 0.0F;

    }

    private void bobBone(IBone bone, int offset, float multiplier) {
        //Copied from AnimationUtils#bobLimb
        float z = multiplier * (Mth.cos(offset * 0.09F) * 0.05F + 0.05F);
        float x = multiplier * Mth.sin(offset * 0.067F) * 0.05F;
        bone.setRotationX(bone.getRotationX() + x);
        bone.setRotationZ(bone.getRotationZ() + z);

    }
}