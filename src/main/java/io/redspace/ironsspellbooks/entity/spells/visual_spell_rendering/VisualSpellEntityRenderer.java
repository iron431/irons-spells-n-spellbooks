package io.redspace.ironsspellbooks.entity.spells.visual_spell_rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VisualSpellEntityRenderer extends EntityRenderer<VisualSpellEntity> {
    private static ResourceLocation SOLID = IronsSpellbooks.id("textures/entity/electric_beams/solid.png");

    public VisualSpellEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(VisualSpellEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        IronsSpellbooks.LOGGER.debug("VisualSpellEntityRenderer: spellid: {}", entity.getSpellType());
        if (entity.getSpellType() == 0)
            return;


        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();

        poseStack.translate(0, 0, 0);

        float f = Mth.rotlerp(entity.yRotO, entity.getYRot(), partialTicks);
        float f1 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        IronsSpellbooks.LOGGER.debug("Visual POS: yrot: {} yrotold: {} parital ticks: {} inbetween: {}", entity.getYRot(), entity.yRotO, partialTicks, f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(f1));
//
//        switch (entity.getSpellType()) {
//            //Ray of Siphoning
//            case 29 -> renderRay(entity, pose, bufferSource, 255, 0, 0, 255, partialTicks);
//        }

        poseStack.popPose();
    }

    public static void renderRay(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int r, int g, int b, int a, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getEyeHeight() * .8f, 0);
        float f = Mth.rotlerp(entity.yRotO, entity.getYRot(), partialTicks);
        float f1 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        IronsSpellbooks.LOGGER.debug("Visual POS: yrot: {} yrotold: {} parital ticks: {} inbetween: {}", entity.getYRot(), entity.yRotO, partialTicks, f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(f1));
        var pose = poseStack.last();
        Vec3 start = Vec3.ZERO;//caster.getEyePosition(partialTicks);
        Vec3 end = new Vec3(0, 0, 10);//Utils.raycastForEntity(entity.level, entity, 32, true).getLocation().subtract(entity.position());
        //IronsSpellbooks.LOGGER.debug("RenderRay: {} to {}", start, end);
        VertexConsumer inner = bufferSource.getBuffer(RenderType.entityCutout(SOLID));
        VertexConsumer outer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(SOLID));
        drawHull(start, end, .125f, .125f, pose, inner, r, g, b, a);
        drawHull(start, end, .25f, .25f, pose, outer, r / 2, g / 2, b / 2, a / 2);
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

    private static void drawQuad(Vec3 from, Vec3 to, float width, float height, PoseStack.Pose pose, VertexConsumer consumer, int r, int g, int b, int a) {
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
    public ResourceLocation getTextureLocation(VisualSpellEntity p_115264_) {
        //return TEXTURES[(int) (Math.random() * 4)];
        return SOLID;
    }
}