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
    private static final ResourceLocation ELDRITCH_ROUND_PORTAL = IronsSpellbooks.id("textures/entity/portal/pocket_dimension_portal_round.png");
    private static final ResourceLocation ELDRITCH_SQUARE_PORTAL = IronsSpellbooks.id("textures/entity/portal/pocket_dimension_portal_square.png");
    private static final ResourceLocation SQUARE_PORTAL = IronsSpellbooks.id("textures/entity/portal/portal_square.png");
    private static final ResourceLocation SQUARE_COLOR_PORTAL = IronsSpellbooks.id("textures/entity/portal/portal_square_color.png");

    public PortalRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(PortalEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));

        renderPortal(poseStack, bufferSource, entity.tickCount, partialTicks, true, -1);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    public static void renderPortal(PoseStack poseStack, MultiBufferSource buffer, int animationTick, float partialTicks, boolean round, int color) {
        renderPortal(poseStack, buffer, animationTick, partialTicks, round, false, color);
    }

    public static void renderPortal(PoseStack poseStack, MultiBufferSource buffer, int animationTick, float partialTicks, boolean round, boolean eldritch, int color) {
        poseStack.pushPose();
        poseStack.scale(0.0625f, 0.0625f, 0.0625f);

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        ResourceLocation texture = round ?
                (eldritch ? ELDRITCH_ROUND_PORTAL : ROUND_PORTAL) :
                (eldritch ? ELDRITCH_SQUARE_PORTAL : color == -1 ? SQUARE_PORTAL : SQUARE_COLOR_PORTAL);
        VertexConsumer consumer = buffer.getBuffer(RenderHelper.CustomerRenderType.darkGlow(texture));
        int anim = (animationTick / ticksPerFrame) % frameCount;
        float uvMin = anim / (float) frameCount;
        float uvMax = (anim + 1) / (float) frameCount;
        vertex(poseMatrix, normalMatrix, consumer, -8, 0, 0, uvMin, 0, color);
        vertex(poseMatrix, normalMatrix, consumer, 8, 0, 0, uvMax, 0, color);
        vertex(poseMatrix, normalMatrix, consumer, 8, 32, 0, uvMax, 1f, color);
        vertex(poseMatrix, normalMatrix, consumer, -8, 32, 0, uvMin, 1f, color);

        poseStack.popPose();
    }

    public static void renderPortal(PoseStack poseStack, MultiBufferSource buffer, int animationTick, float partialTicks, boolean round) {
        renderPortal(poseStack, buffer, animationTick, partialTicks, round, -1);
    }

    static int frameCount = 10;
    static int ticksPerFrame = 2;

    public static void vertex(Matrix4f pMatrix, Matrix3f pNormals, VertexConsumer pVertexBuilder, float pOffsetX, float pOffsetY, float pOffsetZ, float pTextureX, float pTextureY, int color) {
        int r = 255;
        int g = 255;
        int b = 255;
        if (color != -1) {
            r = (color & 0xFF0000) >> 16;
            g = (color & 0x00FF00) >> 8;
            b = color & 0x0000FF;
        }
        pVertexBuilder.addVertex(pMatrix, pOffsetX, pOffsetY, pOffsetZ).setColor(r, g, b, 100).setUv(pTextureX, pTextureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal((float) 0, (float) 0, (float) 1);
    }

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return ROUND_PORTAL;
    }


}