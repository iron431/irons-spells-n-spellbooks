package com.example.testmod.entity.blood_slash;

import com.example.testmod.TestMod;
import com.example.testmod.entity.magic_missile.MagicMissileProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.util.Mth;

//public class BloodSlashRenderer extends EntityRenderer<BloodSlashProjectile> {
//    private static final ResourceLocation TEXTURE = TestMod.id("textures/entity/slash.png");
//    private static float animationTime;
//
//    public BloodSlashRenderer(Context context) {
//        super(context);
//    }
//
//    @Override
//    public void render(BloodSlashProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
//        poseStack.pushPose();
//
//        Pose pose = poseStack.last();
//        Matrix4f poseMatrix = pose.pose();
//        Matrix3f normalMatrix = pose.normal();
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
//        poseStack.mulPose(Vector3f.XP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
//        animationTime += partialTicks;
//        poseStack.mulPose(Vector3f.ZP.rotationDegrees(((entity.animationSeed % 30) - 15) * (float) Math.sin(animationTime * .035)));
//
//        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
//
//        float oldWith = (float) entity.oldDimensions.getXsize();
//        float width = entity.getBbWidth();
//        width = oldWith + (width - oldWith) * Math.min(partialTicks,1);
//        float halfWidth = width * .5f;
//        consumer.vertex(poseMatrix, -halfWidth, -.1f, -halfWidth).color(125, 0, 10, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, halfWidth, -.1f, -halfWidth).color(125, 0, 10, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, halfWidth, -.1f, halfWidth).color(125, 0, 10, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, -halfWidth, -.1f, halfWidth).color(125, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//
//        poseStack.popPose();
//
//        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
//    }
//
//    @Override
//    public ResourceLocation getTextureLocation(BloodSlashProjectile entity) {
//        //TODO: support "animated" textures
//        return TEXTURE;
//    }
//}