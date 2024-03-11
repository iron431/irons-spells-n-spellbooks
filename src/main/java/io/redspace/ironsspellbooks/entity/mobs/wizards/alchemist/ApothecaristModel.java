package io.redspace.ironsspellbooks.entity.mobs.wizards.alchemist;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class ApothecaristModel extends AbstractSpellCastingMobModel {
    public static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/apothecarist.png");
    public static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/piglin_casting_mob.geo.json");

    public ApothecaristModel() {
        super();
        float tilt = 10 * Mth.DEG_TO_RAD;
        Vector3f forward = new Vector3f(0, 0, Mth.sin(tilt) * -12);
        offsets.positionOffset().put(PartNames.HEAD, forward);
        offsets.positionOffset().put(PartNames.RIGHT_ARM, forward);
        offsets.positionOffset().put(PartNames.LEFT_ARM, forward);
        offsets.positionOffset().put("torso", forward);
        offsets.rotationOffset().put("torso", new Vector3f(-tilt, 0, 0));
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return TEXTURE;
    }


    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        IBone leftEar = this.getAnimationProcessor().getBone("left_ear");
        IBone rightEar = this.getAnimationProcessor().getBone("right_ear");
        IBone rightArm = this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM);

        if (leftEar == null || rightEar == null)
            return;
        float partialTick = animationEvent.getPartialTick();
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
        float r = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f;
        r *= .3f;
        r += Mth.PI * .08f;
        leftEar.setRotationZ(-r);
        rightEar.setRotationZ(r);

        if (entity.swingTime > 0) {
            float rot = Mth.lerp((entity.swingTime - partialTick) / 10f, 0, Mth.PI);
            updateRotation(rightArm, rot, rightArm.getRotationY(), rightArm.getRotationZ());
        }
    }
}