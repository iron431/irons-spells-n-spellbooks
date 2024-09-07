package io.redspace.ironsspellbooks.entity.spells.target_area;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import io.redspace.ironsspellbooks.render.SpellTargetingLayer;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TargetAreaRenderer extends EntityRenderer<TargetedAreaEntity> {
    int fadeTick = -1;

    public TargetAreaRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(TargetedAreaEntity pEntity) {
        return null;
    }

    @Override
    public void render(TargetedAreaEntity entity, float pEntityYaw, float pPartialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(SpellRenderingHelper.SOLID, 0, 0));
        var color = entity.getColor();
        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float radius = entity.getRadius();
        int segments = (int) (5 * radius + 9);
        float angle = 2 * Mth.PI / segments;
        float entityY = (float) Mth.lerp(pPartialTick, entity.yOld, entity.getY());

        float[] heights = new float[6];
        for (int i = 0; i < 6; i++) {
            int degrees = i * 60;
            float x = radius * Mth.cos(degrees * Mth.DEG_TO_RAD);
            float z = radius * Mth.sin(degrees * Mth.DEG_TO_RAD);
            float y = Utils.findRelativeGroundLevel(entity.level, entity.position().add(x, entity.getBbHeight(), z), (int) (entity.getBbHeight() * 4));
            heights[i] = y - entityY;
            if (entity.level.collidesWithSuffocatingBlock(null, AABB.ofSize(new Vec3(x, y, z), .1, .1, .1))) {
                heights[i] = 0;
            }
            //entity.level.addParticle(ParticleHelper.EMBERS, x + entity.getX(), heights[i] + entityY, z + entity.getZ(), 0, 0, 0);
        }

        for (int i = 0; i < segments; i++) {
            //55-75
            float theta = angle * i;
            float theta2 = angle * (i + 1);
            float x1 = radius * Mth.cos(theta);
            float x2 = radius * Mth.cos(theta2);
            float z1 = radius * Mth.sin(theta);
            float z2 = radius * Mth.sin(theta2);
            int degrees = (int) (theta * Mth.RAD_TO_DEG);
            int degrees2 = (int) (theta2 * Mth.RAD_TO_DEG);
            int j = (degrees / 60) % 6;
            float heightMin = heights[j];
            float heightMax = heights[(j + 1) % 6];
            float f = ((theta * Mth.RAD_TO_DEG) % 60) / 60f;
            float f2 = ((theta2 * Mth.RAD_TO_DEG) % 60) / 60f;
            float y1 = Mth.lerp(f, heightMin, heightMax);//Mth.clampedLerp(heightMin, heightMax, f);
            if (f2 < f) {
                heightMin = heightMax;
                heightMax = heights[(j + 2) % 6];
            }
            float y2 = Mth.lerp(f2, heightMin, heightMax);//Mth.clampedLerp(heightMin, heightMax, f2);
            //float y2 = Utils.findRelativeGroundLevel(entity.level, entity.position().add(x2, entity.getBbHeight(), z2), (int) (entity.getBbHeight() * 2.5)) - entityY;
            float alpha = 1f;
            if (entity.isFading()) {
                if (fadeTick < 0) {
                    fadeTick = entity.tickCount;
                }
                alpha = Mth.clampedLerp(1, 0, (entity.tickCount + pPartialTick - fadeTick) / 10f);
            }
            consumer.vertex(poseMatrix, x2, y2 - 0.6f, z2).color(color.x() * alpha, color.y() * alpha, color.z() * alpha, 1).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light * 4).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, x2, y2 + 0.6f, z2).color(0, 0, 0, 1).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light * 4).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, x1, y1 + 0.6f, z1).color(0, 0, 0, 1).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light * 4).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, x1, y1 - 0.6f, z1).color(color.x() * alpha, color.y() * alpha, color.z() * alpha, 1).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light * 4).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            //entity.level.addParticle(particle(j), x1 + entity.getX(), y1 + entityY + 1, z1 + entity.getZ(), 0, 0, 0);

        }
        poseStack.popPose();
    }

    private ParticleOptions particle(int i) {
        return switch (i) {
            case 0 -> ParticleHelper.SNOWFLAKE;
            case 1 -> ParticleHelper.UNSTABLE_ENDER;
            case 2 -> ParticleHelper.ACID_BUBBLE;
            case 3 -> ParticleHelper.BLOOD;
            case 4 -> ParticleHelper.WISP;
            case 5 -> ParticleHelper.ELECTRIC_SPARKS;
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }
}
