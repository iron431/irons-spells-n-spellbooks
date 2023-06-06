package io.redspace.ironsspellbooks.entity.spells.blood_needle;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BloodNeedleRenderer extends EntityRenderer<BloodNeedle> {
//    private static final ResourceLocation[] TEXTURES = {
//            IronsSpellbooks.id("textures/entity/blood_needle/needle_0.png"),
//            IronsSpellbooks.id("textures/entity/blood_needle/needle_1.png"),
//            IronsSpellbooks.id("textures/entity/blood_needle/needle_2.png"),
//            IronsSpellbooks.id("textures/entity/blood_needle/needle_3.png"),
//            IronsSpellbooks.id("textures/entity/blood_needle/needle_4.png"),
//            IronsSpellbooks.id("textures/entity/blood_needle/needle_5.png"),
//            IronsSpellbooks.id("textures/entity/blood_needle/needle_6.png"),
//            IronsSpellbooks.id("textures/entity/blood_needle/needle_7.png")
//    };
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/blood_needle/needle_5.png");

    //private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/magic_missile_projectile.png");
    public BloodNeedleRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(BloodNeedle entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        Pose pose = poseStack.last();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getZRot() + (entity.tickCount + partialTicks) * 40));

        //VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));

        //float halfWidth = width * .5f;
        //old color: 125, 0, 10
        float width = 2.5f;
        //drawSlash(pose, bufferSource, light, width, 2);
        poseStack.mulPose(Axis.XP.rotationDegrees(45));
        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);
        drawSlash(pose, entity, bufferSource, light, width);

        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private void drawSlash(Pose pose, BloodNeedle entity, MultiBufferSource bufferSource, int light, float width) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        float halfWidth = width * .5f;
        //old color: 125, 0, 10
        consumer.vertex(poseMatrix, 0, -halfWidth, -halfWidth).color(90, 0, 10, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, halfWidth, -halfWidth).color(90, 0, 10, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, halfWidth, halfWidth).color(90, 0, 10, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, -halfWidth, halfWidth).color(90, 0, 10, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMatrix, 0f, 1f, 0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BloodNeedle entity) {
//        int frame = (entity.tickCount / 4) % TEXTURES.length;
//        return TEXTURES[frame];
        return TEXTURE;
    }

}