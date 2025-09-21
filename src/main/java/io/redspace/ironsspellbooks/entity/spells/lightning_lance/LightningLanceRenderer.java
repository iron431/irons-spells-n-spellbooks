package io.redspace.ironsspellbooks.entity.spells.lightning_lance;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class LightningLanceRenderer extends EntityRenderer<LightningLanceProjectile> {
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance.png");

    public LightningLanceRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(LightningLanceProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        renderModel(poseStack, bufferSource, entity.getAge());
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    public static void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int animOffset) {
        //poseStack.scale(0.0625f, 0.0625f, 0.0625f);

        //poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        //poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(TEXTURE, 0, 0));
        int framecout = 7;
        int anim = animOffset % framecout;
        float uvMin = anim / (float) framecout;
        float uvMax = (anim + 1) / (float) framecout;

        float halfWidth = 2;
        float halfHeight = 1;
        float angleCorrection = 50;
        float texturefix = -.2f;
        //Vertical plane
        poseStack.mulPose(Axis.XP.rotationDegrees(angleCorrection));
        consumer.vertex(poseMatrix, 0, -halfWidth, -halfHeight + texturefix).color(255, 255, 255, 255).uv(uvMin, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, halfWidth, -halfHeight + texturefix).color(255, 255, 255, 255).uv(uvMin, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, halfWidth, halfHeight + texturefix).color(255, 255, 255, 255).uv(uvMax, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, -halfWidth, halfHeight + texturefix).color(255, 255, 255, 255).uv(uvMax, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        poseStack.mulPose(Axis.XP.rotationDegrees(-angleCorrection));

        //Horizontal plane
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleCorrection));
        consumer.vertex(poseMatrix, -halfWidth - texturefix, 0, -halfHeight).color(255, 255, 255, 255).uv(uvMin, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, halfWidth - texturefix, 0, -halfHeight).color(255, 255, 255, 255).uv(uvMin, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, halfWidth - texturefix, 0, halfHeight).color(255, 255, 255, 255).uv(uvMax, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, -halfWidth - texturefix, 0, halfHeight).color(255, 255, 255, 255).uv(uvMax, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        poseStack.mulPose(Axis.YP.rotationDegrees(angleCorrection));
    }

    @Override
    public ResourceLocation getTextureLocation(LightningLanceProjectile entity) {
        return TEXTURE;
    }
}