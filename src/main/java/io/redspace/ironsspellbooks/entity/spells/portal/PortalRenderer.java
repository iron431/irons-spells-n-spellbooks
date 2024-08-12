package io.redspace.ironsspellbooks.entity.spells.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class PortalRenderer extends EntityRenderer<PortalEntity> {
    private static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/portal/portal_round.png");

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

        VertexConsumer consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.darkGlow(getTextureLocation()));
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
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    static int frameCount = 10;
    static int ticksPerFrame = 2;

    public static void vertex(Matrix4f pMatrix, Matrix3f pNormals, VertexConsumer pVertexBuilder, float pOffsetX, float pOffsetY, float pOffsetZ, float pTextureX, float pTextureY) {
        pVertexBuilder.addVertex(pMatrix, pOffsetX, pOffsetY, pOffsetZ).setColor(255, 255, 255, 100).setUv(pTextureX, pTextureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal((float) 0, (float) 0, (float) 1);
    }

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return getTextureLocation();
    }

    public static ResourceLocation getTextureLocation() {
        return TEXTURE;
    }


}