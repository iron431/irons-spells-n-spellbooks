package io.redspace.ironsspellbooks.entity.spells.scapegoat;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.TransformStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import org.joml.Vector2f;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ScapegoatModel extends DefaultedEntityGeoModel<ScapegoatEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/scapegoat.png");
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/scapegoat.geo.json");
    public static final ResourceLocation ANIMS = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/scapegoat_animations.animation.json");

    public ScapegoatModel() {
        super(IronsSpellbooks.id("spellcastingmob"));
    }


    @Override
    public ResourceLocation getTextureResource(ScapegoatEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(ScapegoatEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(ScapegoatEntity animatable) {
        return ANIMS;
    }

    private long lastRenderedInstance = -1;
    protected TransformStack transformStack = new TransformStack();

    @Override
    public void handleAnimations(ScapegoatEntity entity, long instanceId, AnimationState<ScapegoatEntity> animationState, float partialTick) {
        var manager = entity.getAnimatableInstanceCache().getManagerForId(instanceId);
        Double currentTick = animationState.getData(DataTickets.TICK);
        double currentFrameTime = entity instanceof Entity || entity instanceof GeoReplacedEntity ? currentTick + partialTick : currentTick - manager.getFirstTickTime();
        boolean isReRender = !manager.isFirstTick() && currentFrameTime == manager.getLastUpdateTime();
        if (isReRender && instanceId == this.lastRenderedInstance)
            return;
        this.lastRenderedInstance = instanceId;
        transformStack.resetDirty();
        super.handleAnimations(entity, instanceId, animationState, partialTick);
        transformStack.popStack();
    }

    @Override
    public void setCustomAnimations(ScapegoatEntity entity, long instanceId, AnimationState<ScapegoatEntity> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
        var partialTick = animationState.getPartialTick();

//        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
//        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
//        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
//        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        Vector2f limbSwingVec = getLimbSwing(entity, entity.walkAnimation, partialTick);
        float limbSwing = limbSwingVec.y;
        float limbSwingAmount = limbSwingVec.x;
        transformStack.pushRotation(getAnimationProcessor().getBone("rightHindLeg"), Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount, 0, 0);
        transformStack.pushRotation(getAnimationProcessor().getBone("leftHindLeg"), Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount, 0, 0);
        transformStack.pushRotation(getAnimationProcessor().getBone("rightFrontLeg"), Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount, 0, 0);
        transformStack.pushRotation(getAnimationProcessor().getBone("leftFrontLeg"), Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount, 0, 0);
    }


    /**
     * @return x: amount, y: speed
     */
    protected Vector2f getLimbSwing(LivingEntity entity, WalkAnimationState walkAnimationState, float partialTick) {
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