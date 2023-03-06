package com.example.testmod.entity.mobs.abstract_spell_casting_mob;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
        if (Minecraft.getInstance().isPaused())
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

        //Make the head look forward, whatever forward is (influenced externally, such as a lootAt target)
        head.setRotationY(Mth.lerp(partialTick,
                Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD));
        head.setRotationX(Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD);
        //body.setRotationY(0);

        //If we are riding something, pose ourselves sitting
        if (entity.isPassenger()) {
            rightLeg.setRotationX(1.4137167F);
            rightLeg.setRotationY(-(float) Math.PI / 10F);
            rightLeg.setRotationZ(-0.07853982F);
            leftLeg.setRotationX(1.4137167F);
            leftLeg.setRotationY((float) Math.PI / 10F);
            leftLeg.setRotationZ(0.07853982F);
        }

        if (entity.isAnimating())
            return;
        /*
        Copied from LivingEntityRenderer:116 (Swing the limbs)
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
        if (!entity.isPassenger()) {
            rightLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount / f);
            leftLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount / f);
        }


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