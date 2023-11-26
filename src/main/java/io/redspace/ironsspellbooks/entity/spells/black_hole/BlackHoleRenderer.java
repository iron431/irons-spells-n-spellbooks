package io.redspace.ironsspellbooks.entity.spells.black_hole;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class BlackHoleRenderer extends EntityRenderer<BlackHole> {
    public BlackHoleRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    private static final ResourceLocation CENTER_TEXTURE = IronsSpellbooks.id("textures/entity/black_hole/black_hole.png");
    private static final ResourceLocation BEAM_TEXTURE = IronsSpellbooks.id("textures/entity/black_hole/beam.png");

    @Override
    public void render(BlackHole entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getBoundingBox().getYsize() / 2, 0);

        float entityScale = entity.getBbWidth() * .025f;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        poseStack.scale(.5f * entityScale, .5f * entityScale, .5f * entityScale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.translate(5, 0, 0);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(CENTER_TEXTURE));

        consumer.vertex(poseMatrix, 0, -8, -8).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, 8, -8).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, 8, 8).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, -8, 8).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        poseStack.popPose();
        poseStack.pushPose();

        poseStack.translate(0, entity.getBoundingBox().getYsize() / 2, 0);
        float animationProgress = (entity.tickCount + partialTicks) / 200.0F;
        //float fadeProgress = Math.min(animationProgress > 0.8F ? (animationProgress - 0.8F) / 0.2F : 0.0F, 1.0F);
        float fadeProgress = .5f;
        RandomSource randomSource = RandomSource.create(432L);
//        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.energySwirl(BEAM_TEXTURE, 0, 0));
        //poseStack.translate(0.0D, -1.0D, -2.0D);

        float segments = Math.min(animationProgress, .8f);
        for (int i = 0; (float) i < (segments + segments * segments) / 2.0F * 60.0F; ++i) {
            poseStack.mulPose(Axis.XP.rotationDegrees(randomSource.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(randomSource.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(randomSource.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(randomSource.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(randomSource.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(randomSource.nextFloat() * 360.0F + animationProgress * 90.0F));
            float size1 = (randomSource.nextFloat() * 10.0F + 5.0F + fadeProgress * 5.0F) * entityScale * .4f;
            //float size2 = randomSource.nextFloat() * 2.0F + 1.0F + fadeProgress * 2.0F;
            Matrix4f matrix = poseStack.last().pose();
            Matrix3f normalMatrix2 = poseStack.last().normal();

            int alpha = (int) (255.0F * (1.0F - fadeProgress));
//            vertex01(vertexConsumer, matrix, alpha);
//            vertex2(vertexConsumer, matrix, size1, size2);
//            vertex3(vertexConsumer, matrix, size1, size2);
//            vertex01(vertexConsumer, matrix, alpha);
            drawTriangle(vertexConsumer, matrix, normalMatrix2, size1);
//            vertex3(vertexConsumer, matrix, size1, size2);
//            vertex4(vertexConsumer, matrix, size1, size2);
//            vertex01(vertexConsumer, matrix, alpha);
//            vertex4(vertexConsumer, matrix, size1, size2);
//            vertex2(vertexConsumer, matrix, size1, size2);
        }

        poseStack.popPose();

        super.render(entity, pEntityYaw, partialTicks, poseStack, bufferSource, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHole pEntity) {
        return IcicleRenderer.TEXTURE;
    }

    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);

    private static void vertex01(VertexConsumer p_114220_, Matrix4f p_114221_, int p_114222_) {
        p_114220_.vertex(p_114221_, 0.0F, 0.0F, 0.0F).color(255, 255, 255, p_114222_).endVertex();
    }

    private static void vertex2(VertexConsumer p_114215_, Matrix4f p_114216_, float p_114217_, float p_114218_) {
        p_114215_.vertex(p_114216_, -HALF_SQRT_3 * p_114218_, p_114217_, -0.5F * p_114218_).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex3(VertexConsumer p_114224_, Matrix4f p_114225_, float p_114226_, float p_114227_) {
        p_114224_.vertex(p_114225_, HALF_SQRT_3 * p_114227_, p_114226_, -0.5F * p_114227_).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex4(VertexConsumer p_114229_, Matrix4f p_114230_, float p_114231_, float p_114232_) {
        p_114229_.vertex(p_114230_, 0.0F, p_114231_, 1.0F * p_114232_).color(255, 0, 255, 0).endVertex();
    }

    private static void drawTriangle(VertexConsumer consumer, Matrix4f poseMatrix, Matrix3f normalMatrix, float size) {
        consumer.vertex(poseMatrix, 0, 0, 0).color(255, 0, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, 3 * size, -1 * size).color(0, 0, 0, 0).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, 3 * size, 1 * size).color(0, 0, 0, 0).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, 0, 0).color(255, 0, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
    }

}
