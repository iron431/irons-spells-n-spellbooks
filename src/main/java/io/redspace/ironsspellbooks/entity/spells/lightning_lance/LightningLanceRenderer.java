package io.redspace.ironsspellbooks.entity.spells.lightning_lance;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LightningLanceRenderer extends EntityRenderer<LightningLanceProjectile> {
    //private static final ResourceLocation TEXTURE = irons_spellbooks.id("textures/entity/icicle_projectile.png");

    public static final ResourceLocation[] TEXTURES = {
            IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance_1.png"),
            IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance_2.png"),
            IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance_3.png"),
            IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance_4.png"),
            IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance_5.png"),
            IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance_6.png"),
            IronsSpellbooks.id("textures/entity/lightning_lance/lightning_lance_7.png")
    };

    public LightningLanceRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(LightningLanceProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();


        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        renderModel(poseStack, bufferSource, entity.getAge());
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    public static void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int animOffset) {
        //poseStack.scale(0.0625f, 0.0625f, 0.0625f);

        //poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        //poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(getTextureLocation(animOffset), 0, 0));

        float halfWidth = 2;
        float halfHeight = 1;
        float angleCorrection = 55;
        //Vertical plane
        poseStack.mulPose(Axis.XP.rotationDegrees(angleCorrection));
        consumer.vertex(poseMatrix, 0, -halfWidth, -halfHeight).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, halfWidth, -halfHeight).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, halfWidth, halfHeight).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, -halfWidth, halfHeight).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        poseStack.mulPose(Axis.XP.rotationDegrees(-angleCorrection));
//        //Vertical Backface (because of the render type)
//        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
//        poseStack.mulPose(Axis.XP.rotationDegrees(angleCorrection));
//        consumer.vertex(poseMatrix, 0, -halfWidth, -halfHeight).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, 0, halfWidth, -halfHeight).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, 0, halfWidth, halfHeight).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, 0, -halfWidth, halfHeight).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        poseStack.mulPose(Axis.XP.rotationDegrees(-angleCorrection));

        //Horizontal plane
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleCorrection));
        consumer.vertex(poseMatrix, -halfWidth, 0, -halfHeight).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, halfWidth, 0, -halfHeight).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, halfWidth, 0, halfHeight).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, -halfWidth, 0, halfHeight).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
        poseStack.mulPose(Axis.YP.rotationDegrees(angleCorrection));
//        //Horizontal Backface (because of the render type)
//        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
//        poseStack.mulPose(Axis.YP.rotationDegrees(-angleCorrection));
//        consumer.vertex(poseMatrix, -halfWidth, 0, -halfHeight).color(255, 255, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, halfWidth, 0, -halfHeight).color(255, 255, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, halfWidth, 0, halfHeight).color(255, 255, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        consumer.vertex(poseMatrix, -halfWidth, 0, halfHeight).color(255, 255, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
//        poseStack.mulPose(Axis.YP.rotationDegrees(angleCorrection));
    }

    @Override
    public ResourceLocation getTextureLocation(LightningLanceProjectile entity) {
        return getTextureLocation(entity.getAge());
    }

    public static ResourceLocation getTextureLocation(int offset) {
        float ticksPerFrame = 1f;
        return TEXTURES[(int) (offset / ticksPerFrame) % TEXTURES.length];
    }
//
//    public class EnergyRenderType extends RenderType{
//
//        public EnergyRenderType(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
//            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
//        }
//
//        public static final Function<ResourceLocation, RenderType> ENERGY = Util.memoize((p_173253_) -> {
//            return RenderType.create("energy", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().
//                    setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
//                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
//                    .setCullState(NO_CULL)
//                    .setLightmapState(LIGHTMAP)
//                    .setOverlayState(OVERLAY)
//                    .setWriteMaskState(COLOR_WRITE)
//                    .createCompositeState(false));
//
//        });
//
//        public static RenderType energy(ResourceLocation pLocation) {
//            return ENERGY.apply(pLocation);
//        }
//    }
}