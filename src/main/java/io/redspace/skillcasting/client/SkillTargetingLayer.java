package io.redspace.skillcasting.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.cast.EntityCasterRef;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Optional;

public final class SkillTargetingLayer {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "irons_spellbooks", "textures/entity/target/heal.png");

    public static Optional<Vector3f> shouldRender(@NotNull Entity target) {
        if (Minecraft.getInstance().player == null || target == Minecraft.getInstance().player) {
            return Optional.empty();
        }
        EntityCasterRef entityCasterRef = CasterRef.entity(Minecraft.getInstance().player);
        SkillcastingData data = entityCasterRef.skillcastingData();
        ActiveCast activeCast = entityCasterRef.skillcastingData().getActiveCast();
        if (activeCast != null) {
            if (activeCast.context().find(SkillcastingComponentTypes.TARGETED_ENTITIES).map(
                    targetedEntities -> targetedEntities.isTargeted(target)
            ).orElse(false)) {
                return Optional.of(activeCast.context().skill().value().getAccentColor());
            }
        }
        for (RecastInstance recast : data.recasts().getActiveRecasts()) {
            if (recast.components().find(SkillcastingComponentTypes.TARGETED_ENTITIES.get()).map(
                    targetedEntities -> targetedEntities.isTargeted(target)
            ).orElse(false)) {
                return Optional.of(recast.skill().value().getAccentColor());
            }
        }
        return Optional.empty();
    }

    public static void renderTargetLayer(PoseStack poseStack, MultiBufferSource bufferSource, Entity entity, Vector3f color) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(TEXTURE, 0, 0));
        AABB aabb = entity.getBoundingBox();

        float width = (float) aabb.getXsize();
        float height = (float) aabb.getYsize();
        float halfWidth = width * .55f;
        Vector3f renderColor = new Vector3f(color).mul(.4f);
        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        for (int i = 0; i < 4; i++) {
            consumer.addVertex(poseMatrix, halfWidth, 0, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, halfWidth, height, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, -halfWidth, height, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, -halfWidth, 0, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }

        poseStack.popPose();
    }

    public static void handleRenderEvent(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Optional<Vector3f> shouldRender = shouldRender(entity);
        if (shouldRender.isPresent()) {
            Vector3f color = shouldRender.get();
            poseStack.translate(x, y, z);
            renderTargetLayer(poseStack, buffer, entity, color);
        }
    }

}
