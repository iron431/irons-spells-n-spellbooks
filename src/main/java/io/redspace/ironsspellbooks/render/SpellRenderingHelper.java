package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpellRenderingHelper {
    private static ResourceLocation SOLID = IronsSpellbooks.id("textures/entity/ray/solid.png");
    private static ResourceLocation BEACON = IronsSpellbooks.id("textures/entity/ray/beacon_beam.png");
    private static ResourceLocation GLOW = IronsSpellbooks.id("textures/entity/ray/ribbon_glow.png");

    public static void renderRay(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int r, int g, int b, int a, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getEyeHeight() * .8f, 0);
        float f = Mth.rotlerp(entity.yRotO, entity.getYRot(), partialTicks);
        float f1 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        //IronsSpellbooks.LOGGER.debug("Visual POS: yrot: {} yrotold: {} parital ticks: {} inbetween: {}", entity.getYRot(), entity.yRotO, partialTicks, f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(f1));
        var pose = poseStack.last();
        Vec3 start = Vec3.ZERO;//caster.getEyePosition(partialTicks);
        Vec3 end = new Vec3(0, 0, 10);//Utils.raycastForEntity(entity.level, entity, 32, true).getLocation().subtract(entity.position());
        //IronsSpellbooks.LOGGER.debug("RenderRay: {} to {}", start, end);
        VertexConsumer inner = bufferSource.getBuffer(RenderType.entityCutout(BEACON));
        VertexConsumer outer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(BEACON));
        for (int j = 1; j <= 10 / 1; j++) {
            end = new Vec3(0, 0, j);
            drawHull(start, end, .125f, .125f, pose, inner, r, g, b, a);
            drawHull(start, end, .25f, .25f, pose, outer, r / 2, g / 2, b / 2, a / 2);
            start = end;

        }
        i++;
        poseStack.popPose();
    }

    private static void drawHull(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
        //Bottom
        drawQuad(from.subtract(0, height * .5f, 0), to.subtract(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a);
        //Top
        drawQuad(from.add(0, height * .5f, 0), to.add(0, height * .5f, 0), width, 0, pose, consumer, r, g, b, a);
        //Left
        drawQuad(from.subtract(width * .5f, 0, 0), to.subtract(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a);
        //Right
        drawQuad(from.add(width * .5f, 0, 0), to.add(width * .5f, 0, 0), 0, height, pose, consumer, r, g, b, a);
    }

    static int i;
    private static void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        float f1 = -(i / 40f) % 40f;
        float max = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        float min = -1.0F + max;
        float halfWidth = width * .5f;
        float halfHeight = height * .5f;

        consumer.vertex(poseMatrix, (float) from.x - halfWidth, (float) from.y - halfHeight, (float) from.z).color(r, g, b, a).uv(0f, min).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) from.x + halfWidth, (float) from.y + halfHeight, (float) from.z).color(r, g, b, a).uv(1f, min).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x + halfWidth, (float) to.y + halfHeight, (float) to.z).color(r, g, b, a).uv(1f, max).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, (float) to.x - halfWidth, (float) to.y - halfHeight, (float) to.z).color(r, g, b, a).uv(0f, max).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(normalMatrix, 0f, 1f, 0f).endVertex();

    }
}