package io.redspace.ironsspellbooks.entity.spells.echoing_strikes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EchoingMagicArrowRenderer extends EntityRenderer<EchoingArrowProjectile> {
    private static final float MODEL_SCALE = 0.065f;

    public EchoingMagicArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EchoingArrowProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (entity.tickCount < EchoingArrowProjectile.SPAWN_DELAY) {
            return;
        }

        poseStack.pushPose();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        MagicArrowRenderer.renderModel(poseStack, bufferSource, MODEL_SCALE);
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EchoingArrowProjectile entity) {
        return MagicArrowRenderer.getTextureLocation();
    }
}
