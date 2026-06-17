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
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * Render layer that automatically renders a target outline indicator for entities targeted by the Local Players from synced component {@link SkillcastingComponentTypes#MULTI_TARGET_ENTITIES}
 */
// FIXME: currently no geckolib support
public final class SkillTargetingLayer {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "irons_spellbooks", "textures/entity/target/heal.png");

    public static Optional<Vector3f> shouldRender(@NotNull Entity target) {
        EntityCasterRef entityCasterRef = Minecraft.getInstance().player == null ? null : CasterRef.entity(Minecraft.getInstance().player);
        if (entityCasterRef == null) {
            return Optional.empty();
        }
        SkillcastingData data = entityCasterRef.skillcastingData();
        if (!data.isLive()) {
            return Optional.empty();
        }
        ActiveCast activeCast = entityCasterRef.skillcastingData().getActiveCast();
        if (activeCast != null) {
            if (activeCast.context().find(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES).map(
                    targetedEntities -> targetedEntities.isTargeted(target)
            ).orElse(false)) {
                return Optional.of(activeCast.context().skill().value().getAccentColor());
            }
        }
        for (RecastInstance recast : data.recasts().getActiveRecasts()) {
            if (recast.components().find(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES.get()).map(
                    targetedEntities -> targetedEntities.isTargeted(target)
            ).orElse(false)) {
                return Optional.of(recast.skill().value().getAccentColor());
            }
        }
        return Optional.empty();
    }

    public static void renderTargetLayer(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity entity, Vector3f color) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(TEXTURE, 0, 0));
        AABB aabb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());

        float width = (float) aabb.getXsize();
        float height = (float) aabb.getYsize();
        float halfWidth = width * .55f;
        float magicYOffset = (float) (1.5 - height);
        Vector3f renderColor = new Vector3f(color).mul(.4f);
        poseStack.pushPose();
        poseStack.translate(0, magicYOffset, 0);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();

        for (int i = 0; i < 4; i++) {
            consumer.addVertex(poseMatrix, halfWidth, height, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, halfWidth, 0, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, -halfWidth, 0, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, -halfWidth, height, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }

        poseStack.popPose();
    }

    public static void register(EntityRenderersEvent.AddLayers event) {
        addLayerToPlayerSkin(event, PlayerSkin.Model.SLIM);
        addLayerToPlayerSkin(event, PlayerSkin.Model.WIDE);
        for (var entityType : event.getEntityTypes()) {
            var renderer = event.getRenderer(entityType);
            if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                addVanillaLayer(livingRenderer);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skinName) {
        var renderer = event.getSkin(skinName);
        if (renderer instanceof PlayerRenderer playerRenderer) {
            addVanillaLayer(playerRenderer);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addVanillaLayer(LivingEntityRenderer livingRenderer) {
        livingRenderer.addLayer(new Vanilla(livingRenderer));
    }

    public static class Vanilla<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        public Vanilla(RenderLayerParent<T, M> renderer) {
            super(renderer);
        }

        @Override
        public void render(
                @NotNull PoseStack poseStack,
                @NotNull MultiBufferSource bufferSource,
                int packedLight,
                @NotNull T entity,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch) {
            Optional<Vector3f> shouldRender = shouldRender(entity);
            if (shouldRender.isPresent()) {
                Vector3f color = shouldRender.get();
                renderTargetLayer(poseStack, bufferSource, entity, color);
            }
        }
    }
}
