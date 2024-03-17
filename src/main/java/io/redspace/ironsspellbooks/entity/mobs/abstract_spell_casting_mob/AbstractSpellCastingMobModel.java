package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.HashMap;

public abstract class AbstractSpellCastingMobModel extends DefaultedEntityGeoModel<AbstractSpellCastingMob> {

    public AbstractSpellCastingMobModel(/*ResourceLocation assetSubpath*/) {
        super(IronsSpellbooks.id("spellcastingmob"));
    }

    protected ModelPartOffsets offsets = new ModelPartOffsets(new HashMap<>(), new HashMap<>());

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
        CoreGeoBone torso = this.getAnimationProcessor().getBone("torso");
        CoreGeoBone rightArm = this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM);
        CoreGeoBone leftArm = this.getAnimationProcessor().getBone(PartNames.LEFT_ARM);
        CoreGeoBone rightLeg = this.getAnimationProcessor().getBone(PartNames.RIGHT_LEG);
        CoreGeoBone leftLeg = this.getAnimationProcessor().getBone(PartNames.LEFT_LEG);

        /*
            Head Controls
         */
        //Make the head look forward, whatever forward is (influenced externally, such as a lootAt target)
        Vector3f rotOverride;
        Vector3f posOverride;
        posOverride = offsets.positionOffset().getOrDefault(PartNames.HEAD, new Vector3f(0, 0, 0));
        rotOverride = offsets.rotationOffset().getOrDefault(PartNames.HEAD, new Vector3f(0, 0, 0));
        updatePosition(head, posOverride.x(), posOverride.y(), posOverride.z());
        updateRotation(head, rotOverride.x(), rotOverride.y(), rotOverride.z());
        if (!entity.isAnimating() || entity.shouldAlwaysAnimateHead()) {
            rotOverride = offsets.rotationOffset().getOrDefault(PartNames.HEAD, new Vector3f(0, 0, 0));
            head.setRotY(Mth.lerp(partialTick,
                    Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                    Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD) + rotOverride.y());
            head.setRotX(Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD + rotOverride.x());
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
            rotOverride = offsets.rotationOffset().getOrDefault(PartNames.RIGHT_LEG, new Vector3f(0, 0, 0));
            updateRotation(rightLeg,
                    1.4137167F + rotOverride.x(),
                    -(float) Math.PI / 10F + rotOverride.y(),
                    -0.07853982F + rotOverride.z()
            );
            rotOverride = offsets.rotationOffset().getOrDefault(PartNames.LEFT_LEG, new Vector3f(0, 0, 0));
            updateRotation(leftLeg,
                    1.4137167F + rotOverride.x(),
                    (float) Math.PI / 10F + rotOverride.y(),
                    0.07853982F + rotOverride.z()
            );
        } else if (!entity.isAnimating() || entity.shouldAlwaysAnimateLegs()) {
            float strength = .75f;
            Vec3 facing = entity.getForward().multiply(1, 0, 1).normalize();
            Vec3 momentum = entity.getDeltaMovement().multiply(1, 0, 1).normalize();
            Vec3 facingOrth = new Vec3(-facing.z, 0, facing.x);
            float directionf = (float) facing.dot(momentum);
            float directions = (float) facingOrth.dot(momentum) * .35f; //scale side to side movement so they dont rip off thier own legs
            float rightLat = -Mth.sin(pLimbSwing * 0.6662F) * 4 * pLimbSwingAmount;
            float leftLat = -Mth.sin(pLimbSwing * 0.6662F - Mth.PI) * 4 * pLimbSwingAmount;
            posOverride = offsets.positionOffset().getOrDefault(PartNames.RIGHT_LEG, new Vector3f(0, 0, 0));
            rotOverride = offsets.rotationOffset().getOrDefault(PartNames.RIGHT_LEG, new Vector3f(0, 0, 0));
            updatePosition(rightLeg, rightLat * directions + posOverride.x(), Mth.cos(pLimbSwing * 0.6662F) * 4 * strength * pLimbSwingAmount + posOverride.y(), rightLat * directionf + posOverride.z());
            updateRotation(rightLeg, Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount * strength + rotOverride.x(), 0 + rotOverride.y(), 0 + rotOverride.z());

            posOverride = offsets.positionOffset().getOrDefault(PartNames.LEFT_LEG, new Vector3f(0, 0, 0));
            rotOverride = offsets.rotationOffset().getOrDefault(PartNames.LEFT_LEG, new Vector3f(0, 0, 0));
            updatePosition(leftLeg, leftLat * directions + posOverride.x(), Mth.cos(pLimbSwing * 0.6662F - Mth.PI) * 4 * strength * pLimbSwingAmount + posOverride.y(), leftLat * directionf + posOverride.z());
            updateRotation(leftLeg, Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount * strength + rotOverride.x(), 0 + rotOverride.y(), 0 + rotOverride.z());

            posOverride = offsets.positionOffset().getOrDefault("torso", new Vector3f(0, 0, 0));
            rotOverride = offsets.rotationOffset().getOrDefault("torso", new Vector3f(0, 0, 0));
            updatePosition(torso, posOverride.x(), posOverride.y(), posOverride.z());
            updateRotation(torso, rotOverride.x(), rotOverride.y(), rotOverride.z());

            if (entity.bobBodyWhileWalking()) {
                updatePosition(body, 0 + posOverride.x(), Mth.abs(Mth.cos((pLimbSwing * 1.2662F - Mth.PI * .5f) * .5f)) * 2 * strength * pLimbSwingAmount + posOverride.y(), 0 + posOverride.z());
            }
        }
        /*
            Arm Controls
         */
        if (!entity.isAnimating()) {
            posOverride = offsets.positionOffset().getOrDefault(PartNames.RIGHT_ARM, new Vector3f(0, 0, 0));
            rotOverride = offsets.rotationOffset().getOrDefault(PartNames.RIGHT_ARM, new Vector3f(0, 0, 0));
            updatePosition(rightArm, posOverride.x(), posOverride.y(), posOverride.z());
            updateRotation(rightArm, rotOverride.x(), rotOverride.y(), rotOverride.z());
            posOverride = offsets.positionOffset().getOrDefault(PartNames.LEFT_ARM, new Vector3f(0, 0, 0));
            rotOverride = offsets.rotationOffset().getOrDefault(PartNames.LEFT_ARM, new Vector3f(0, 0, 0));
            updatePosition(leftArm, posOverride.x(), posOverride.y(), posOverride.z());
            updateRotation(leftArm, rotOverride.x(), rotOverride.y(), rotOverride.z());
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

    protected static void updatePosition(CoreGeoBone bone, float x, float y, float z) {
        bone.setPosX(x);
        bone.setPosY(y);
        bone.setPosZ(z);
    }

    protected static void updateRotation(CoreGeoBone bone, float x, float y, float z) {
        bone.setRotX(x);
        bone.setRotY(y);
        bone.setRotZ(z);
    }

    protected record ModelPartOffsets(HashMap<String, Vector3f> positionOffset,
                                      HashMap<String, Vector3f> rotationOffset) {
    }
}