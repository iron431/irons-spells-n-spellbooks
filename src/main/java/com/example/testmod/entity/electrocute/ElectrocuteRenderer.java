package com.example.testmod.entity.electrocute;

import com.example.testmod.TestMod;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ElectrocuteRenderer extends EntityRenderer<ElectrocuteProjectile> {
    private static ResourceLocation TEXTURES[] = {
            TestMod.id("textures/entity/electric_beams/beam_1.png"),
            TestMod.id("textures/entity/electric_beams/beam_2.png"),
            TestMod.id("textures/entity/electric_beams/beam_3.png"),
            TestMod.id("textures/entity/electric_beams/beam_4.png")
    };
    private static ResourceLocation SOLID = TestMod.id("textures/entity/electric_beams/solid.png");

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
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(getTextureLocation(entity)));
        //VertexConsumer consumer = bufferSource.getBuffer(RenderType.endPortal());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-entity.getOwner().getYRot()));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getOwner().getXRot()));

        if (entity.getAge() % 2 == 0)
            entity.generateLightningBeams();
        List<Vec3> segments = entity.getBeamCache();
        //TestMod.LOGGER.debug("ElectrocuteRenderer.segments.length: {}",segments.size());


        float width = .25f;
        float height = width;
        for (int i = 0; i < segments.size() - 1; i += 2) {
            var from = segments.get(i).add(0, entity.getOwner().getEyeHeight() / 4, 0);
            var to = segments.get(i + 1).add(0, entity.getOwner().getEyeHeight() / 4, 0);
            drawHull(from, to, width, height, pose, consumer, 0, 156, 255, 35);
            drawHull(from, to, width * .75f, height * .75f, pose, consumer, 0, 226, 255, 35);
            drawHull(from, to, width * .3f, height * .3f, pose, consumer, 255, 255, 255, 255);
        }


//        drawSegment(new Vec3(0, 0, 0), new Vec3(-1, 1, 3), 1, pose, consumer, entity, bufferSource, light);
//        drawSegment(new Vec3(-1, 1, 3), new Vec3(1, 1, 5), 1, pose, consumer, entity, bufferSource, light);
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private void drawHull(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
        //Bottom
        drawQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a);
        //Top
        drawQuad(from.add(0, height * .5f, 0), to.add(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a);
        //Left
        drawQuad(from.subtract(width * .5f, 0, 0), to.subtract(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a);
        //Right
        drawQuad(from.add(width * .5f, 0, 0), to.add(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a);
    }

    private void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        //to = new Vec3(1, 0, 10);
        float halfWidth = width * .5f;
        float halfHeight = height * .5f;
        //float height = (float) (Math.random() * .25f) + .25f;
        consumer.vertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).color(r, g, b, a).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).color(r, g, b, a).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).color(r, g, b, a).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).color(r, g, b, a).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();

    }

    @Override
    public ResourceLocation getTextureLocation(ElectrocuteProjectile p_115264_) {
        //return TEXTURES[(int) (Math.random() * 4)];
        return SOLID;
    }
}