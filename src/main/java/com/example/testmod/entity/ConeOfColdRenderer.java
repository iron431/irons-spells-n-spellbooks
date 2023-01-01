package com.example.testmod.entity;

import com.example.testmod.TestMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ConeOfColdRenderer extends EntityRenderer<ConeOfColdProjectile> {
    private static final ResourceLocation TEXTURE = TestMod.id("textures/entity/magic_missile_projectile.png");

    public ConeOfColdRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(ConeOfColdProjectile entity, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        poseStack.scale(.5f, .5f, .5f);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180f));

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));

        consumer.vertex(poseMatrix, -.5f, 0f, 0f).color(0, 0, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, .5f, 0f, 0f).color(0, 0, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, .5f, 1f, 0f).color(0, 0, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, -.5f, 1f, 0f).color(0, 0, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();

        poseStack.popPose();

        super.render(entity, yaw, tickDelta, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ConeOfColdProjectile entity) {
        return TEXTURE;
    }
}