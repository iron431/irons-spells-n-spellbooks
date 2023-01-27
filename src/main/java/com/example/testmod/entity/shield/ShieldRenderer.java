package com.example.testmod.entity.shield;

import com.example.testmod.TestMod;
import com.example.testmod.entity.blood_slash.BloodSlashProjectile;
import com.example.testmod.entity.mobs.simple_wizard.SimpleWizardModel;
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

public class ShieldRenderer extends EntityRenderer<ShieldEntity> {
    //    private static final ResourceLocation[] TEXTURES = {
//            TestMod.id("textures/entity/blood_slash/blood_slash_1.png"),
//            TestMod.id("textures/entity/blood_slash/blood_slash_2.png"),
//            TestMod.id("textures/entity/blood_slash/blood_slash_3.png"),
//            TestMod.id("textures/entity/blood_slash/blood_slash_4.png"),
//            TestMod.id("textures/entity/blood_slash/blood_slash_5.png"),
//            TestMod.id("textures/entity/blood_slash/blood_slash_6.png")
//    };
    private static ResourceLocation TEXTURE = TestMod.id("textures/entity/shield.png");
    //private static ResourceLocation TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final ShieldModel model;
    public ShieldRenderer(Context context) {
        super(context);
        this.model = new ShieldModel(context.bakeLayer(ShieldModel.LAYER_LOCATION));
    }

    @Override
    public void render(ShieldEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-entity.getYRot()));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot()));

        //VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        float f = (entity.tickCount + partialTicks) * .02f;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(getTextureLocation(entity), shittyNoise(1.2f * f), shittyNoise(f + 456)));
        float width = entity.width * .5f;
        poseStack.scale(width,width,width);
        model.renderToBuffer(poseStack,consumer,LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.0F, 0.2F, 1.0F, 0.5F);
//        float pixelScale = .25f;
//        float halfWidth = width * .65f;
//        //old color: 125, 0, 10
//        consumer.vertex(poseMatrix, -halfWidth, -halfWidth, 0).color(0, 156, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, halfWidth, -halfWidth, 0).color(0, 156, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, halfWidth, halfWidth, 0).color(0, 156, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, -halfWidth, halfWidth, 0).color(0, 156, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private float shittyNoise(float f) {
        return (float) (Math.sin(f / 4) + 2 * Math.sin(f / 3) + 3 * Math.sin(f / 2) + 4 * Math.sin(f)) * .25f;
    }

    @Override
    public ResourceLocation getTextureLocation(ShieldEntity entity) {
        return TEXTURE;
    }

}