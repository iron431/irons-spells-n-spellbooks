package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public abstract class AbstractSpellCastingMobModel extends DefaultedEntityGeoModel<AbstractSpellCastingMob> {

    public AbstractSpellCastingMobModel(/*ResourceLocation assetSubpath*/) {
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
            head.updateRotation(0, 0, 0);
            head.setRotY(Mth.lerp(partialTick,
                    Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                    Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD));
            head.setRotX(Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD);
        }
        /*
            Crazy Vanilla Magic Calculations (LivingEntityRenderer:116 & HumanoidModel#setupAnim
         */
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
            rightLeg.setRotX(Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount * strength);
            leftLeg.setRotX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount * strength);
            if (entity.bobBodyWhileWalking()) {
                updatePosition(body, 0, Mth.abs(Mth.cos((pLimbSwing * 1.2662F - Mth.PI * .5f) * .5f)) * 2 * strength * pLimbSwingAmount, 0);
            }
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
        //leftArm.updateRotation(leftArm.getRotX(),leftArm.getRotY(),leftArm.getRotZ());
        //rightArm.updateRotation(rightArm.getRotX(),rightArm.getRotY(),rightArm.getRotZ());

//        rightArm.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f);
//        leftArm.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f);

        //rightLeg.yRot = 0.0F;
        //leftLeg.yRot = 0.0F;
        //rightLeg.zRot = 0.0F;
        //leftLeg.zRot = 0.0F;

    }

    protected void bobBone(CoreGeoBone bone, int offset, boolean inverted) {
        //Copied from AnimationUtils#bobLimb
        float z = (Mth.cos(offset * 0.09F) * 0.05F + 0.05F);
        float x = Mth.sin(offset * 0.067F) * 0.05F;
        //return new Vector2f(x, z);
        addRotationX(bone, (inverted ? -1 : 1) * x);
        addRotationZ(bone, (inverted ? -1 : 1) * z);
    }

    protected void addRotationX(CoreGeoBone bone, float rotation) {
        bone.setRotX(wrapRadians(bone.getRotX() + rotation));
    }

    protected void addRotationZ(CoreGeoBone bone, float rotation) {
        bone.setRotZ(wrapRadians(bone.getRotZ() + rotation));
    }

    protected void addRotationY(CoreGeoBone bone, float rotation) {
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

    private static void updatePosition(CoreGeoBone bone, float x, float y, float z) {
        bone.setPosX(x);
        bone.setPosY(y);
        bone.setPosZ(z);
    }

    private static void updateRotation(CoreGeoBone bone, float x, float y, float z) {
        bone.setRotX(x);
        bone.setRotY(y);
        bone.setRotZ(z);
    }
}