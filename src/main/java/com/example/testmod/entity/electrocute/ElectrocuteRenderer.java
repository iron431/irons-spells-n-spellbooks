package com.example.testmod.entity.electrocute;

import com.example.testmod.TestMod;
import com.example.testmod.entity.blood_slash.BloodSlashProjectile;
import com.example.testmod.entity.cone_of_cold.ConeOfColdProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ElectrocuteRenderer extends EntityRenderer<ElectrocuteProjectile> {
    private static ResourceLocation TEXTURE = TestMod.id("textures/entity/blood_slash/blood_slash_large.png");

    public ElectrocuteRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(ElectrocuteProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-entity.getOwner().getYRot()));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getOwner().getXRot()));
        //drawCube(pose,poseMatrix,consumer,light, entity.position(),entity.position().add(entity.getLookAngle().scale(10).add(1,1,1)));
        drawSegment(pose, consumer, entity, bufferSource, light, entity.position(), entity.position().add(entity.getOwner().getLookAngle().normalize().scale(-10)), 1);
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private void drawSegment(PoseStack.Pose pose, VertexConsumer consumer, ElectrocuteProjectile entity, MultiBufferSource bufferSource, int light, Vec3 to, Vec3 from, float width) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

//        to.subtract(entity.position());
//        from.subtract(entity.position());
        to = Vec3.ZERO;
        to = entity.getOwner().getEyePosition().subtract(entity.position());
        TestMod.LOGGER.debug("{} - {} = {}",entity.getOwner().getEyePosition(),entity.position(),to);
        from = new Vec3(1,0,10);
        float halfWidth = width * .5f;
        double radians = entity.getYRot() * Mth.DEG_TO_RAD;
        float rotX = halfWidth;// * (float) Math.cos(radians);
        float rotZ = halfWidth;//-halfWidth * (float) Math.sin(radians);

        consumer.vertex(poseMatrix, -rotX + (float) from.x, (float) from.y, -rotZ + (float) from.z).color(90, 0, 10, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, rotX + (float) from.x, (float) from.y, -rotZ + (float) from.z).color(90, 0, 10, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, rotX + (float) to.x, (float) to.y, rotZ + (float) to.z).color(90, 0, 10, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, -rotX + (float) to.x, (float) to.y, rotZ + (float) to.z).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
    }

    private void drawCube(PoseStack.Pose pose, Matrix4f poseMatrix, VertexConsumer consumer, int light, Vec3 from, Vec3 to) {
        Matrix3f normalMatrix = pose.normal();

        float minX = (float) Math.min(from.x, to.x);
        float minY = (float) Math.min(from.y, to.y);
        float minZ = (float) Math.min(from.z, to.z);
        float maxX = (float) Math.max(from.x, to.x);
        float maxY = (float) Math.max(from.y, to.y);
        float maxZ = (float) Math.max(from.z, to.z);
        //"closer" square face
        //bottom left
        consumer.vertex(poseMatrix, minX, minY, minZ).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        //bottom right
        consumer.vertex(poseMatrix, maxX, minY, minZ).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        //top left
        consumer.vertex(poseMatrix, minX, maxY, minZ).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        //top right
        consumer.vertex(poseMatrix, maxX, maxY, minZ).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();

        //"farther" square face
        //bottom left
        consumer.vertex(poseMatrix, minX, minY, maxZ).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        //bottom right
        consumer.vertex(poseMatrix, maxX, minY, maxZ).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        //top left
        consumer.vertex(poseMatrix, minX, maxY, maxZ).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        //top right
        consumer.vertex(poseMatrix, maxX, maxY, maxZ).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ElectrocuteProjectile p_115264_) {
        return TEXTURE;
    }
}