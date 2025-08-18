package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.Objects;

public class IceSpiderModel extends DefaultedEntityGeoModel<IceSpiderEntity> {
    static final String[] SIDES = {"right", "left"};
    static final String[] LEGS = {"Fore", "ForeMiddle", "BackMiddle", "Back"};
    static final String SHOULDER = "Shoulder";
    static final String LEG = "Leg";

    static final float OFFSET_PER_LEG = 35 * Mth.DEG_TO_RAD;

    public IceSpiderModel() {
        super(IronsSpellbooks.id("spellcastingmob"));
    }

    protected TransformStack transformStack = new TransformStack();

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/ice_spider/ice_spider.png");
    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/ice_spider.geo.json");
    public static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/ice_spider.animation.json");
    public static final DataTicket<TransformStack> STACK_TICKET = new DataTicket<>("irons_spellbooks:transform_stack", TransformStack.class);

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
        return ANIMATION;
    }

    private long lastRenderedInstance = -1;

    @Override
    public void handleAnimations(IceSpiderEntity entity, long instanceId, AnimationState<IceSpiderEntity> animationState, float partialTick) {
        var manager = entity.getAnimatableInstanceCache().getManagerForId(instanceId);
        Double currentTick = animationState.getData(DataTickets.TICK);
        double currentFrameTime = entity instanceof Entity || entity instanceof GeoReplacedEntity ? currentTick + partialTick : currentTick - manager.getFirstTickTime();
        boolean isReRender = !manager.isFirstTick() && currentFrameTime == manager.getLastUpdateTime();
        if (isReRender && instanceId == this.lastRenderedInstance)
            return;
        this.lastRenderedInstance = instanceId;

//        TransformStack transformStack = manager.getData(STACK_TICKET);
//        if (transformStack == null) {
//            transformStack = new TransformStack();
//            manager.setData(STACK_TICKET, transformStack);
//        }
//        animationState.setData(STACK_TICKET, transformStack);
        transformStack.resetDirty();
        super.handleAnimations(entity, instanceId, animationState, partialTick);
        transformStack.popStack();
    }

    @Override
    public void setCustomAnimations(IceSpiderEntity entity, long instanceId, AnimationState<IceSpiderEntity> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
//        TransformStack transformStack = animationState.getData(STACK_TICKET);
//        assert transformStack != null;
        var partialTick = animationState.getPartialTick();
        transformStack.pushPosition(getAnimationProcessor().getBone("torso"),
                (float) IceSpiderEntity.TORSO_OFFSET.x,
                (float) IceSpiderEntity.TORSO_OFFSET.y * entity.getCrouchHeightMultiplier(partialTick),
                (float) IceSpiderEntity.TORSO_OFFSET.z);

        Vec3 normal = Utils.lerp(partialTick, entity.lastNormal, entity.normal);
        Quaternionf normalRotation = Utils.rotationBetweenVectors(normal.toVector3f(), new Vector3f(0, 1, 0));
        Vector3f headRotation = new Vector3f(
                Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) * Mth.DEG_TO_RAD,
                Mth.lerp(partialTick,
                        Mth.wrapDegrees(entity.yHeadRotO - entity.yBodyRotO) * Mth.DEG_TO_RAD,
                        Mth.wrapDegrees(entity.yHeadRot - entity.yBodyRot) * Mth.DEG_TO_RAD
                ), 0);
        normalRotation.invert().transform(headRotation); // undo body rotation and apply to head rotation to normalize
        var head = getAnimationProcessor().getBone("head");
        transformStack.pushRotation(head, -headRotation.x, -headRotation.y, -headRotation.z);

        Vector2f limbSwingVec = getLimbSwing(entity, entity.walkAnimation, partialTick);
        float limbSwing = limbSwingVec.y;
        float limbSwingAmount = limbSwingVec.x;

        float f = 0.5f;
        float yRange = 20 * Mth.DEG_TO_RAD * f;
        float zRange = 12 * Mth.DEG_TO_RAD * f;
        float speed = 2 * .05f / f;


        float primaryY = legY(limbSwing, speed, 0) * yRange * limbSwingAmount;
        float secondaryY = legY(limbSwing, speed, Mth.PI) * yRange * limbSwingAmount;
        float primaryZ = legZ(limbSwing, speed, Mth.PI) * zRange * limbSwingAmount;
        float secondaryZ = legZ(limbSwing, speed, 0) * zRange * limbSwingAmount;

        for (int i = 0; i < SIDES.length; i++) {
            for (int j = 0; j < LEGS.length; j++) {
                int sideSign = Mth.sign(i - 0.5);
                float baseY = 0;//(j - 1.5f) * OFFSET_PER_LEG * sideSign;
                float baseZ = Mth.lerp(entity.crouchTweenPercent(partialTick), 10, 0) * Mth.DEG_TO_RAD;
                String shoulderBone = String.format("%s%s%s", SIDES[i], LEGS[j], SHOULDER);
                String legBone = String.format("%s%s%s", SIDES[i], LEGS[j], LEG);
                boolean primary = j % 2 == 0;
                try {
                    transformStack.pushRotation(Objects.requireNonNull(getAnimationProcessor().getBone(shoulderBone)), 0, ((primary ? primaryY : secondaryY) + baseY) * sideSign, 0);
                    transformStack.pushRotation(Objects.requireNonNull(getAnimationProcessor().getBone(legBone)), 0, 0, ((primary ? primaryZ : secondaryZ) + baseZ) * -sideSign);
                } catch (Exception e) {
                    IronsSpellbooks.LOGGER.error("beep");
                }
            }
        }
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