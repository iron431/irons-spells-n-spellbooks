package io.redspace.ironsspellbooks.entity.spells.wall_of_fire;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class WallOfFireRenderer extends EntityRenderer<WallOfFireEntity> {

    private static ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/fire_0.png");

    //private static ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("textures/entity/creeper/creeper_armor.png");
    public WallOfFireRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(WallOfFireEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float height = 3;
        Vec3 origin = entity.position();

        for (int i = 0; i < entity.subEntities.length - 1; i++) {
            Vec3 start = entity.subEntities[i].position().subtract(origin);
            Vec3 end = entity.subEntities[i + 1].position().subtract(origin);
            int frameCount = 32;
            int frame = (entity.tickCount + i * 87) % frameCount;
            float uvPerFrame = (1 / (float) frameCount);
            float uvY = frame * uvPerFrame;
            poseStack.pushPose();
            consumer.vertex(poseMatrix, (float) start.x, (float) start.y, (float) start.z).color(255, 255, 255, 255).uv(0f, uvY + uvPerFrame).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, (float) start.x, (float) start.y + height, (float) start.z).color(255, 255, 255, 255).uv(0f, uvY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, (float) end.x, (float) end.y + height, (float) end.z).color(255, 255, 255, 255).uv(1f, uvY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, (float) end.x, (float) end.y, (float) end.z).color(255, 255, 255, 255).uv(1f, uvY + uvPerFrame).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            consumer.vertex(poseMatrix, (float) start.x, (float) start.y, (float) start.z).color(255, 255, 255, 255).uv(0f, uvY + uvPerFrame).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, (float) start.x, (float) start.y + height, (float) start.z).color(255, 255, 255, 255).uv(0f, uvY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, (float) end.x, (float) end.y + height, (float) end.z).color(255, 255, 255, 255).uv(1f, uvY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, (float) end.x, (float) end.y, (float) end.z).color(255, 255, 255, 255).uv(1f, uvY + uvPerFrame).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();

            poseStack.popPose();
        }
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }


    @Override
    public ResourceLocation getTextureLocation(WallOfFireEntity entity) {
        return TEXTURE;
    }
}