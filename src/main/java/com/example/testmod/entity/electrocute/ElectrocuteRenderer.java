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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ElectrocuteRenderer extends EntityRenderer<ElectrocuteProjectile> {
    private static ResourceLocation TEXTURES[] = {
            TestMod.id("textures/entity/electric_beams/beam_1.png"),
            TestMod.id("textures/entity/electric_beams/beam_2.png"),
            TestMod.id("textures/entity/electric_beams/beam_3.png"),
            TestMod.id("textures/entity/electric_beams/beam_4.png")
    };

    public ElectrocuteRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(ElectrocuteProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (entity.getOwner() == null)
            return;
        poseStack.pushPose();

        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-entity.getOwner().getYRot()));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getOwner().getXRot()));

        if (entity.getAge() % 2 == 0)
            entity.generateLightningBeams();
        List<Vec3> segments = entity.getBeamCache();
        //TestMod.LOGGER.debug("ElectrocuteRenderer.segments.length: {}",segments.size());


        //TODO: use y value as width scaling
        for (int i = 0; i < segments.size() - 1; i += 2) {
            drawSegment(segments.get(i), segments.get(i + 1), 0.25f, pose, consumer, entity, bufferSource, light);
        }


//        drawSegment(new Vec3(0, 0, 0), new Vec3(-1, 1, 3), 1, pose, consumer, entity, bufferSource, light);
//        drawSegment(new Vec3(-1, 1, 3), new Vec3(1, 1, 5), 1, pose, consumer, entity, bufferSource, light);
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }


    private void drawSegment(Vec3 from, Vec3 to, float width, PoseStack.Pose pose, VertexConsumer consumer, ElectrocuteProjectile entity, MultiBufferSource bufferSource, int light) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        //to = new Vec3(1, 0, 10);
        float halfWidth = width * .5f;
        float height = (float) (Math.random() * .25f) + .25f;
        consumer.vertex(poseMatrix, (float) from.x - halfWidth, height, (float) from.z).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) from.x + halfWidth, height, (float) from.z).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x + halfWidth, height, (float) to.z).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x - halfWidth, height, (float) to.z).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();

    }

    @Override
    public ResourceLocation getTextureLocation(ElectrocuteProjectile p_115264_) {
        return TEXTURES[(int) (Math.random() * 4)];
    }
}