package io.redspace.ironsspellbooks.entity.spells.sunbeam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.render.RenderHelper;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SunbeamRenderer extends EntityRenderer<SunbeamEntity> {

    public SunbeamRenderer(Context context) {
        super(context);
    }


    @Override
    public boolean shouldRender(SunbeamEntity pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public void render(SunbeamEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        //poseStack.mulPose(Axis.XP.rotationDegrees(90));
        float maxRadius = 2.5f;
        float minRadius = 0.005f;
        float deltaTicks = entity.tickCount + partialTicks;
        float deltaUV = -deltaTicks % 10;
        float max = Mth.frac(deltaUV * 0.2F - (float) Mth.floor(deltaUV * 0.1F));
        float min = -1.0F + max;
        float f = deltaTicks / SunbeamEntity.WARMUP_TIME;
        f *= f;
        float radius = Mth.clampedLerp(maxRadius, minRadius, f);
        VertexConsumer inner = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magic(SpellRenderingHelper.BEACON));
        //SpellRenderingHelper.drawHull(Vec3.ZERO, new Vec3(0, 100, 0), radius, radius, poseStack.last(), inner, 255, 255, 255, (int) (255 * f), min, max);
        float halfRadius = radius * .5f;
        float quarterRadius = halfRadius * .5f;
        float yMin = entity.onGround() ? 0 : Utils.findRelativeGroundLevel(entity.level, entity.position(), 8) - (float) entity.getY();
        for (int i = 0; i < 4; i++) {
            //orange glow
            RenderHelper.quadBuilder()
                    .vertex(-halfRadius, yMin, -halfRadius).uv(0, min).normal(0, 1, 0)
                    .vertex(-halfRadius, yMin, halfRadius).uv(1, min).normal(0, 1, 0)
                    .vertex(-halfRadius, 250, halfRadius).uv(1, max).normal(0, 1, 0)
                    .vertex(-halfRadius, 250, -halfRadius).uv(0, max).normal(0, 1, 0)
                    .color(Mth.clamp(.8f * f, 0, 1), Mth.clamp(.8f * f * f, 0, 1), Mth.clamp(.5f * f * f, 0, 1))
                    .light(LightTexture.FULL_BRIGHT)
                    .overlay(OverlayTexture.NO_OVERLAY)
                    .matrix(poseStack.last().pose())
                    .build(inner);
            //yellow core
            RenderHelper.quadBuilder()
                    .vertex(-quarterRadius, yMin, -quarterRadius).uv(0, min).normal(0, 1, 0)
                    .vertex(-quarterRadius, yMin, quarterRadius).uv(1, min).normal(0, 1, 0)
                    .vertex(-quarterRadius, 250, quarterRadius).uv(1, max).normal(0, 1, 0)
                    .vertex(-quarterRadius, 250, -quarterRadius).uv(0, max).normal(0, 1, 0)
                    .color(Mth.clamp(1f * f, 0, 1), Mth.clamp(.85f * f, 0, 1), Mth.clamp(.7f * f * f, 0, 1))
                    .light(LightTexture.FULL_BRIGHT)
                    .overlay(OverlayTexture.NO_OVERLAY)
                    .matrix(poseStack.last().pose())
                    .build(inner);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SunbeamEntity entity) {
        return SpellRenderingHelper.BEACON;
    }

}