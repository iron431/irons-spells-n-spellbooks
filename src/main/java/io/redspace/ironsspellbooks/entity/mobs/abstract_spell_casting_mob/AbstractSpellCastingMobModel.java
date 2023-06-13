package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.WalkAnimationState;
import org.joml.Vector2f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public abstract class AbstractSpellCastingMobModel extends DefaultedEntityGeoModel<AbstractSpellCastingMob> {

    public AbstractSpellCastingMobModel(/*ResourceLocation assetSubpath*/) {
        //TODO: (1.19.4 port) what is this resourcelocation supposed to point to?
        // i think it may be auto-boilerplating, but we already did it for 1.19.2 so hopefully it sill works
        super(IronsSpellbooks.id("spellcastingmob"));
    }

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
    public void setCustomAnimations(AbstractSpellCastingMob entity, long instanceId, AnimationState<AbstractSpellCastingMob> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (Minecraft.getInstance().isPaused() || !entity.shouldBeExtraAnimated())
            return;

        float partialTick = animationState.getPartialTick();
        /*
                This overrides all other animation
         */
        CoreGeoBone head = this.getAnimationProcessor().getBone(PartNames.HEAD);
        CoreGeoBone body = this.getAnimationProcessor().getBone(PartNames.BODY);
        CoreGeoBone rightArm = this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM);
        CoreGeoBone leftArm = this.getAnimationProcessor().getBone(PartNames.LEFT_ARM);
        CoreGeoBone rightLeg = this.getAnimationProcessor().getBone(PartNames.RIGHT_LEG);
        CoreGeoBone leftLeg = this.getAnimationProcessor().getBone(PartNames.LEFT_LEG);

        /*
            Head Controls
         */
        //Make the head look forward, whatever forward is (influenced externally, such as a lootAt target)
        if (!entity.isAnimating() || entity.shouldAlwaysAnimateHead()) {
            head.setRotY(Mth.lerp(partialTick,
                    Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                    Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD));
            head.setRotX(Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD);
        }
        /*
            Crazy Vanilla Magic Calculations (LivingEntityRenderer:116 & HumanoidModel#setupAnim
         */
        //Parchment not finished yet
        WalkAnimationState walkAnimationState = entity.walkAnimation;
        float pLimbSwingAmount = 0.0F;
        float pLimbSwing = 0.0F;
        if (entity.isAlive()) {
            pLimbSwingAmount = walkAnimationState.speed(partialTick);
            pLimbSwing = walkAnimationState.position(partialTick);
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
            rightLeg.setRotX(1.4137167F);
            rightLeg.setRotY(-(float) Math.PI / 10F);
            rightLeg.setRotZ(-0.07853982F);
            leftLeg.setRotX(1.4137167F);
            leftLeg.setRotY((float) Math.PI / 10F);
            leftLeg.setRotZ(0.07853982F);
        } else if (!entity.isAnimating() || entity.shouldAlwaysAnimateLegs()) {
            //rightLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount / f);
            //leftLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount / f);
            //addRotationX(rightLeg, (Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount / f));
            //addRotationX(leftLeg, Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount / f);
            rightLeg.setRotX(Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount / f);
            leftLeg.setRotX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount / f);

        }
        /*
            Arm Controls
         */
        if (!entity.isAnimating()) {
            rightArm.updateRotation(0f, 0f, 0f);
            leftArm.updateRotation(0f, 0f, 0f);

            rightArm.setRotX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f);
            leftArm.setRotX(Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f);
            bobBone(rightArm, entity.tickCount, false);
            bobBone(leftArm, entity.tickCount, true);
            if (entity.isDrinkingPotion()) {
                addRotationX(entity.isLeftHanded() ? leftArm : rightArm, 35 * Mth.DEG_TO_RAD);
                addRotationZ(entity.isLeftHanded() ? leftArm : rightArm, (entity.isLeftHanded() ? 15 : -15) * Mth.DEG_TO_RAD);
                addRotationY(entity.isLeftHanded() ? leftArm : rightArm, (entity.isLeftHanded() ? -25 : 25) * Mth.DEG_TO_RAD);
            }

        } else if (entity.shouldPointArmsWhileCasting()) {
            addRotationX(rightArm, -entity.getXRot() * Mth.DEG_TO_RAD);
            addRotationX(leftArm, -entity.getXRot() * Mth.DEG_TO_RAD);
        }
        //TODO: (1.20 port) geckolib is doing some very different and weird stuff. bones seem to remember their prevous rotation and additive modifiers compound tick after tick. we'll have to deal with this later
        //leftArm.updateRotation(leftArm.getRotX(),leftArm.getRotY(),leftArm.getRotZ());
        //rightArm.updateRotation(rightArm.getRotX(),rightArm.getRotY(),rightArm.getRotZ());

//        rightArm.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f);
//        leftArm.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f);

        //rightLeg.yRot = 0.0F;
        //leftLeg.yRot = 0.0F;
        //rightLeg.zRot = 0.0F;
        //leftLeg.zRot = 0.0F;

    }

    private void bobBone(CoreGeoBone bone, int offset, boolean inverted) {
        //Copied from AnimationUtils#bobLimb
        float z = (Mth.cos(offset * 0.09F) * 0.05F + 0.05F);
        float x = Mth.sin(offset * 0.067F) * 0.05F;
        //return new Vector2f(x, z);
        addRotationX(bone, (inverted ? -1 : 1) * x);
        addRotationZ(bone, (inverted ? -1 : 1) * z);
    }

    private void addRotationX(CoreGeoBone bone, float rotation) {
        bone.setRotX(wrapRadians(bone.getRotX() + rotation));
    }

    private void addRotationZ(CoreGeoBone bone, float rotation) {
        bone.setRotZ(wrapRadians(bone.getRotZ() + rotation));
    }

    private void addRotationY(CoreGeoBone bone, float rotation) {
        bone.setRotY(wrapRadians(bone.getRotY() + rotation));
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
}