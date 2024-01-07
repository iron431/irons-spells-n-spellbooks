package io.redspace.ironsspellbooks.entity.spells.portal;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static io.redspace.ironsspellbooks.entity.spells.portal.PortalRenderer.CustomerRenderType.crumbling;

public class PortalRenderer extends EntityRenderer<PortalEntity> {
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/portal.png");

    public PortalRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(PortalEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        poseStack.scale(0.0625f, 0.0625f, 0.0625f);

        //poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        //poseStack.mulPose(Vector3f.YP.rotationDegrees(180f));

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        VertexConsumer consumer = bufferSource.getBuffer(crumbling(getTextureLocation()));
        int anim = (entity.tickCount / ticksPerFrame) % 9;
        float uvMin = anim / (float) frameCount;
        float uvMax = (anim + 1) / (float) frameCount;
        vertex(poseMatrix, normalMatrix, consumer, -8, 0, 0, uvMin, 0);
        vertex(poseMatrix, normalMatrix, consumer, 8, 0, 0, uvMax, 0);
        vertex(poseMatrix, normalMatrix, consumer, 8, 32, 0, uvMax, 1f);
        vertex(poseMatrix, normalMatrix, consumer, -8, 32, 0, uvMin, 1f);

//        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
//        //poseStack.mulPose(dispatcher.cameraOrientation());
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(dispatcher.camera.getYRot() + entity.getYRot()));
//
//        Vec3 forwardVector = entity.getForward();
//        Vec3 viewerVector = entity.position().subtract(dispatcher.camera.getPosition()).normalize();
//        float dot = (float) forwardVector.dot(viewerVector);
//        float widthFactor = Utils.smoothstep(.2f, 1f, Mth.abs(dot)) * Mth.sign(dot);
//
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
//        vertex(poseMatrix, normalMatrix, consumer, widthFactor * -8, 0, 0, uvMin, 0);
//        vertex(poseMatrix, normalMatrix, consumer, widthFactor * 8, 0, 0, uvMax, 0);
//        vertex(poseMatrix, normalMatrix, consumer, widthFactor * 8, 32, 0, uvMax, 1f);
//        vertex(poseMatrix, normalMatrix, consumer, widthFactor * -8, 32, 0, uvMin, 1f);

        poseStack.popPose();
//        debugText("" + widthFactor, 2.25f, poseStack, bufferSource, light);
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

//    private static void debugText(String text, float yOffset, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
//        matrixStackIn.pushPose();
//        matrixStackIn.translate(0.0D, yOffset, 0.0D);
//
//        matrixStackIn.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
//        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
//        Matrix4f matrix4f = matrixStackIn.last().pose();
//        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
//        int j = (int) (f1 * 255.0F) << 24;
//
//        var font = Minecraft.getInstance().font;
//        float f2 = (float) (-font.width(text) / 2);
//        font.drawInBatch(text, f2, 0, 553648127, false, matrix4f, bufferIn, true, j, packedLightIn);
//        matrixStackIn.popPose();
//    }

    static int frameCount = 10;
    static int ticksPerFrame = 2;

    public static void vertex(Matrix4f pMatrix, Matrix3f pNormals, VertexConsumer pVertexBuilder, float pOffsetX, float pOffsetY, float pOffsetZ, float pTextureX, float pTextureY) {
        pVertexBuilder.vertex(pMatrix, pOffsetX, pOffsetY, pOffsetZ).color(255, 255, 255, 100).uv(pTextureX, pTextureY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(pNormals, (float) 0, (float) 0, (float) 1).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return getTextureLocation();
    }

    public static ResourceLocation getTextureLocation() {
        return TEXTURE;
    }


    public static class CustomerRenderType extends RenderType {
        public CustomerRenderType(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }

        public static @NotNull RenderType crumbling(@NotNull ResourceLocation pLocation) {
            return create("crumbling", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(pLocation, false, false)).setTransparencyState(ONE_MINUS).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false));
        }

        protected static final RenderStateShard.TransparencyStateShard ONE_MINUS = new RenderStateShard.TransparencyStateShard("one_minus", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });
    }

}