package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class HealTargetLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public HealTargetLayer(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    public static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/target/heal.png");

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (shouldRender(entity)) {

            //LightningLanceRenderer.renderModel(poseStack, bufferSource, 0);
            poseStack.pushPose();

            PoseStack.Pose pose = poseStack.last();
            Matrix4f poseMatrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();

            VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(TEXTURE));


            float width = entity.getBbWidth() * 1.2f;
            float height = width * 2;
            float halfWidth = width * .5f;

            float r = 0.85f;
            float g = 0f;
            float b = 0f;
            float a = 1f;

            //float halfHeight = height * .5f;
            //East plane (top down relative)
            consumer.vertex(poseMatrix, halfWidth, 0, -halfWidth).color(r, g, b, a).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, height, -halfWidth).color(r, g, b, a).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, height, halfWidth).color(r, g, b, a).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, 0, halfWidth).color(r, g, b, a).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();

            //South plane (top down relative)
            consumer.vertex(poseMatrix, -halfWidth, 0, -halfWidth).color(r, g, b, a).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, -halfWidth, height, -halfWidth).color(r, g, b, a).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, height, -halfWidth).color(r, g, b, a).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, 0, -halfWidth).color(r, g, b, a).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();

            poseStack.mulPose(Vector3f.YP.rotationDegrees(180));

            //West plane (top down relative)
            consumer.vertex(poseMatrix, halfWidth, 0, -halfWidth).color(r, g, b, a).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, height, -halfWidth).color(r, g, b, a).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, height, halfWidth).color(r, g, b, a).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, 0, halfWidth).color(r, g, b, a).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();

            //North plane (top down relative)
            consumer.vertex(poseMatrix, -halfWidth, 0, -halfWidth).color(r, g, b, a).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, -halfWidth, height, -halfWidth).color(r, g, b, a).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, height, -halfWidth).color(r, g, b, a).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, 0, -halfWidth).color(r, g, b, a).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();

            poseStack.popPose();

        }

    }

    boolean shouldRender(T entity) {
        return ClientMagicData.getTargetingData().target == entity;
    }
}
