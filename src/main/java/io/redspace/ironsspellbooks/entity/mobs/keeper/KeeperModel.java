package io.redspace.ironsspellbooks.entity.mobs.keeper;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class KeeperModel extends AbstractSpellCastingMobModel {
    public static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/keeper/keeper.png");
    public static final ResourceLocation modelResource = new ResourceLocation(IronsSpellbooks.MODID, "geo/citadel_keeper.geo.json");

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return modelResource;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        if (Minecraft.getInstance().isPaused())
            return;

        float partialTick = animationEvent.getPartialTick();

        IBone rightLeg = this.getAnimationProcessor().getBone(PartNames.RIGHT_LEG);
        IBone leftLeg = this.getAnimationProcessor().getBone(PartNames.LEFT_LEG);
        IBone rightArm = this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM);
        IBone leftArm = this.getAnimationProcessor().getBone(PartNames.LEFT_ARM);
        IBone body = this.getAnimationProcessor().getBone(PartNames.BODY);
        IBone head = this.getAnimationProcessor().getBone(PartNames.HEAD);

        boolean tick = lastTick != entity.tickCount;
        lastTick = entity.tickCount;

        float pLimbSwingAmount = 0.0F;
        float pLimbSwing = 0.0F;
        if (entity.isAlive()) {
            pLimbSwingAmount = Mth.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
            pLimbSwing = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
            //pLimbSwingAmount *= .75f;
            //pLimbSwing *= .75f;
            if (pLimbSwingAmount > 1.0F) {
                pLimbSwingAmount = 1.0F;
            }
            if (entity.hurtTime > 0) {
                pLimbSwingAmount *= .25f;
            }
        }
        if (!(entity.isPassenger() && entity.getVehicle().shouldRiderSit())) {
            float strength = .75f;
            updatePosition(rightLeg, 0, Mth.cos(pLimbSwing * 0.6662F) * 4 * strength * pLimbSwingAmount, -Mth.sin(pLimbSwing * 0.6662F) * 4 * pLimbSwingAmount);
            updatePosition(leftLeg, 0, Mth.cos(pLimbSwing * 0.6662F - Mth.PI) * 4 * strength * pLimbSwingAmount, -Mth.sin(pLimbSwing * 0.6662F - Mth.PI) * 4 * pLimbSwingAmount);
            updatePosition(body, 0, Mth.abs(Mth.cos((pLimbSwing * 1.2662F - Mth.PI * .5f) * .5f)) * 2 * strength * pLimbSwingAmount, 0);
            Vec3 interpDeltaMovement = entity.getDeltaMovement();
            interpDeltaMovement = new Vec3(Mth.lerp(partialTick, deltaMovementOld.x, interpDeltaMovement.x), Mth.lerp(partialTick, deltaMovementOld.y, interpDeltaMovement.y), Mth.lerp(partialTick, deltaMovementOld.z, interpDeltaMovement.z));
            float speed = (float) interpDeltaMovement.horizontalDistanceSqr();
            float bodyRot = (float) -(Mth.smoothstep(Mth.clamp(speed * 65, 0, 1)) * Mth.PI / 12);
            body.setRotationX(bodyRot);
            head.setRotationX(head.getRotationX() - bodyRot);
            IronsSpellbooks.LOGGER.debug("speed: {} | bodyrot: {}", speed, bodyRot);
            if (tick) {
                deltaMovementOld = entity.getDeltaMovement();
                if (!entity.isAnimating() || entity.shouldAlwaysAnimateLegs()) {
                    legTween = Mth.lerp(.9f, 0, 1);
                } else {
                    legTween = Mth.lerp(.9f, 1, 0);
                }
            }
            rightLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount * legTween * strength);
            leftLeg.setRotationX(Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount * legTween * strength);
        }
        if (entity.isAnimating()) {
            bobBone(rightArm, entity.tickCount, 1);
            bobBone(leftArm, entity.tickCount, -1);
        }
    }

    private int lastTick;
    private float legTween = 1f;
    private Vec3 deltaMovementOld = Vec3.ZERO;

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