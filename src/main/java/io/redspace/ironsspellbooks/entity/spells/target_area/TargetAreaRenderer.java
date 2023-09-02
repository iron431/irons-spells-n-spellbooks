package io.redspace.ironsspellbooks.entity.spells.target_area;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import io.redspace.ironsspellbooks.render.SpellTargetingLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TargetAreaRenderer extends EntityRenderer<TargetedAreaEntity> {
    public TargetAreaRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(TargetedAreaEntity pEntity) {
        return null;
    }

    @Override
    public void render(TargetedAreaEntity entity, float pEntityYaw, float pPartialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(SpellRenderingHelper.SOLID, 0, 0));
        var color = entity.getColor();
        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float radius = entity.getRadius();
        float circumference = 2 * radius * Mth.PI;
        int segments = (int) (5 * radius + 9);
        float angle = 2 * Mth.PI / segments;
        float segmentWidth = (circumference / segments);

        RenderSystem.disableTexture();
        for (int i = 0; i < segments; i++) {
            float theta = angle * i;
            float theta2 = angle * (i + 1);
            float x1 = radius * Mth.cos(theta);
            float x2 = radius * Mth.cos(theta2);
            float z1 = radius * Mth.sin(theta);
            float z2 = radius * Mth.sin(theta2);
            //drawPlane(consumer, color, poseMatrix, normalMatrix, light, segmentWidth, radius);
            //poseStack.mulPose(Vector3f.YP.rotationDegrees(angle));
            consumer.vertex(poseMatrix, x2, 0, z2).color(color.x(), color.y(), color.z(), 1).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light * 4).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, x2, 0.6f, z2).color(0, 0, 0, 1).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light * 4).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, x1, 0.6f, z1).color(0, 0, 0, 1).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light * 4).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, x1, 0, z1).color(color.x(), color.y(), color.z(), 1).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light * 4).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        }
        RenderSystem.enableTexture();
        poseStack.popPose();
    }
}
