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
    private static final ResourceLocation ROUND_PORTAL = IronsSpellbooks.id("textures/entity/portal/portal_round.png");
    private static final ResourceLocation SQUARE_PORTAL = IronsSpellbooks.id("textures/entity/portal/portal_square.png");

    public PortalRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(PortalEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));

        renderPortal(poseStack, bufferSource, entity.tickCount, partialTicks, true);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    public static void renderPortal(PoseStack poseStack, MultiBufferSource buffer, int animationTick, float partialTicks, boolean round) {
        poseStack.pushPose();
        poseStack.scale(0.0625f, 0.0625f, 0.0625f);

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        VertexConsumer consumer = buffer.getBuffer(RenderHelper.CustomerRenderType.darkGlow(round ? ROUND_PORTAL : SQUARE_PORTAL));
        int anim = (animationTick / ticksPerFrame) % frameCount;
        float uvMin = anim / (float) frameCount;
        float uvMax = (anim + 1) / (float) frameCount;
        vertex(poseMatrix, normalMatrix, consumer, -8, 0, 0, uvMin, 0);
        vertex(poseMatrix, normalMatrix, consumer, 8, 0, 0, uvMax, 0);
        vertex(poseMatrix, normalMatrix, consumer, 8, 32, 0, uvMax, 1f);
        vertex(poseMatrix, normalMatrix, consumer, -8, 32, 0, uvMin, 1f);

        poseStack.popPose();
    }

    static int frameCount = 10;
    static int ticksPerFrame = 2;

    public static void vertex(Matrix4f pMatrix, Matrix3f pNormals, VertexConsumer pVertexBuilder, float pOffsetX, float pOffsetY, float pOffsetZ, float pTextureX, float pTextureY) {
        pVertexBuilder.addVertex(pMatrix, pOffsetX, pOffsetY, pOffsetZ).setColor(255, 255, 255, 100).setUv(pTextureX, pTextureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal((float) 0, (float) 0, (float) 1);
    }

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return ROUND_PORTAL;
    }


}