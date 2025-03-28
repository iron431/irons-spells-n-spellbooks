package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.WalkAnimationState;
import org.joml.Vector2f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.Objects;

public class IceSpiderModel extends DefaultedEntityGeoModel<IceSpiderEntity> {
    static final String[] SIDES = {"right"/*, "left"*/};
    static final String[] LEGS = {"Fore", "ForeMiddle", "BackMiddle", "Back"};
    static final String SHOULDER = "Shoulder";
    static final String LEG = "Leg";

    static final float OFFSET_PER_LEG = 35 * Mth.DEG_TO_RAD;

    public IceSpiderModel() {
        super(IronsSpellbooks.id("spellcastingmob"));
    }

    protected TransformStack transformStack = new TransformStack();

    public static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/ice_spider/ice_spider.png");
    public static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/ice_spider.geo.json");

    @Override
    public ResourceLocation getModelResource(IceSpiderEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(IceSpiderEntity object) {
        return TEXTURE;
    }


    @Override
    public ResourceLocation getAnimationResource(IceSpiderEntity animatable) {
        return AbstractSpellCastingMob.animationInstantCast;
    }

    @Override
    public void handleAnimations(IceSpiderEntity entity, long instanceId, AnimationState<IceSpiderEntity> animationState, float partialTick) {
        if (!Minecraft.getInstance().isPaused()) {
            transformStack.resetDirty();
        }
        super.handleAnimations(entity, instanceId, animationState, partialTick);
    }

    @Override
    public void setCustomAnimations(IceSpiderEntity entity, long instanceId, AnimationState<IceSpiderEntity> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
        var partialTick = animationState.getPartialTick();
        getAnimationProcessor().getBone("torso").updatePosition((float) IceSpiderEntity.TORSO_OFFSET.x, (float) IceSpiderEntity.TORSO_OFFSET.y, (float) IceSpiderEntity.TORSO_OFFSET.z);
        transformStack.pushRotation(getAnimationProcessor().getBone("head"),
                Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD,
                Mth.lerp(partialTick,
                        Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                        Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD
                ),
                0);
        Vector2f limbSwingVec = getLimbSwing(entity, entity.walkAnimation, partialTick);
        float limbSwing = limbSwingVec.y;
        float limbSwingAmount = limbSwingVec.x;

        float f = 0.5f;
        float yRange = 20 * Mth.DEG_TO_RAD * f;
        float zRange = 15 * Mth.DEG_TO_RAD * f;
        float speed = 2 * .05f / f;

        float primaryY = legY(limbSwing, speed, 0) * yRange * limbSwingAmount;
        float secondaryY = legY(limbSwing, speed, Mth.PI) * yRange * limbSwingAmount;
        float primaryZ = legZ(limbSwing, speed, 0) * zRange * limbSwingAmount;
        float secondaryZ = legZ(limbSwing, speed, Mth.PI) * zRange * limbSwingAmount;
        for (int i = 0; i < SIDES.length; i++) {
            for (int j = 0; j < LEGS.length; j++) {
                int offset = j + i;
                float baseY = (j - 1.5f) * OFFSET_PER_LEG * (i - 1);
                float baseZ = Mth.PI / 4f * (i - 1);
                String shoulderBone = String.format("%s%s%s", SIDES[i], LEGS[j], SHOULDER);
                String legBone = String.format("%s%s%s", SIDES[i], LEGS[j], LEG);
                boolean primary = offset % 2 == 0;
                try {
                    transformStack.pushRotation(Objects.requireNonNull(getAnimationProcessor().getBone(shoulderBone)), 0, (primary ? primaryY : secondaryY) + baseY, 0);
                    transformStack.pushRotation(Objects.requireNonNull(getAnimationProcessor().getBone(legBone)), 0, 0, (primary ? primaryZ : secondaryZ) + baseZ);
                } catch (Exception e) {
                    IronsSpellbooks.LOGGER.error("beep");
                }
            }
        }
//            float f = (float) (Math.PI / 4);
//
//            float hindY = -(Mth.cos(limbSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbSwingAmount;
//            float middleHindY = -(Mth.cos(limbSwing * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbSwingAmount;
//            float middleFrontY = -(Mth.cos(limbSwing * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * limbSwingAmount;
//            float frontLegY = -(Mth.cos(limbSwing * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * limbSwingAmount;
//            float hindLegZ = Math.abs(Mth.sin(limbSwing * 0.6662F + 0.0F) * 0.4F) * limbSwingAmount;
//            float middleHindZ = Math.abs(Mth.sin(limbSwing * 0.6662F + (float) Math.PI) * 0.4F) * limbSwingAmount;
//            float middleFrontZ = Math.abs(Mth.sin(limbSwing * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * limbSwingAmount;
//            float frontZ = Math.abs(Mth.sin(limbSwing * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * limbSwingAmount;
//            transformStack.pushRotation(rightLeg1, 0, frontLegY, frontZ);
//            transformStack.pushRotation(rightLeg2, 0, middleFrontY, middleFrontZ);
//            transformStack.pushRotation(rightLeg3, 0, middleHindY, middleHindZ);
//            transformStack.pushRotation(rightLeg4, 0, hindY, hindLegZ);
        transformStack.popStack();
    }

    private float legY(float limbSwing, float speedFactor, float offset) {
        float f = offset - Mth.HALF_PI;

        return Mth.sin((limbSwing * Mth.TWO_PI) * speedFactor + f +
                (Mth.sin(limbSwing * Mth.TWO_PI * speedFactor + f)) * 0.5f);
    }

    private float legZ(float limbSwing, float speedFactor, float offset) {
        float f = Mth.sin((limbSwing * Mth.TWO_PI) * speedFactor + offset);
        f = f * f * f;
        return Math.max(f, 0);
    }

    /**
     * @param walkAnimationState
     * @return x: amount, y: speed
     */
    protected Vector2f getLimbSwing(AbstractSpellCastingMob entity, WalkAnimationState walkAnimationState, float partialTick) {
        float limbSwingAmount = 0;
        float limbSwingSpeed = 0;
        if (entity.isAlive()) {
            limbSwingAmount = walkAnimationState.speed(partialTick);
            limbSwingSpeed = walkAnimationState.position(partialTick);
            if (entity.isBaby()) {
                limbSwingSpeed *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }
        return new Vector2f(limbSwingAmount, limbSwingSpeed);
    }
}