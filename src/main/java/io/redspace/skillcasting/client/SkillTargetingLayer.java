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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;

@EventBusSubscriber
public final class SkillTargetingLayer {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "irons_spellbooks", "textures/entity/target/heal.png");

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.AddLayers event) {
        if (event.getSkin(PlayerSkin.Model.SLIM) instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new Vanilla<>(livingRenderer));
        }
        if (event.getSkin(PlayerSkin.Model.WIDE) instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new Vanilla<>(livingRenderer));
        }
        for (EntityType type : event.getEntityTypes()) {
            var renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer livingRenderer) {
                livingRenderer.addLayer(new Vanilla<>(livingRenderer));
            }
        }
    }

    @SubscribeEvent
    public static void registerGeoRenderers(GeoRenderEvent.Entity.CompileRenderLayers event) {
        event.addLayer(new Geo(event.getRenderer()));
    }

    public static class Vanilla<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        public Vanilla(RenderLayerParent<T, M> pRenderer) {
            super(pRenderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            handleRenderEvent(entity, poseStack, bufferSource);
        }
    }

    public static class Geo<T extends Entity & GeoEntity> extends GeoRenderLayer<T> {
        public Geo(GeoEntityRenderer<T> entityRendererIn) {
            super(entityRendererIn);
        }

        @Override
        public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            poseStack.scale(1,-1,1);
            handleRenderEvent(animatable, poseStack, bufferSource);
        }
    }

    public static Optional<Vector3f> shouldRender(@NotNull Entity target) {
        if (true) {
            return Optional.of(new Vector3f(1, 1, 1));
        }
        EntityCasterRef entityCasterRef = Minecraft.getInstance().player == null ? null : CasterRef.entity(Minecraft.getInstance().player);
        if (entityCasterRef == null) {
            return Optional.empty();
        }
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
        float scale = entity instanceof LivingEntity livingEntity ? 1 / livingEntity.getScale() : 1;
        poseStack.scale(scale, -scale, scale);
        for (int i = 0; i < 4; i++) {
            consumer.addVertex(poseMatrix, halfWidth, -height, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(0f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, halfWidth, 0, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(0f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, -halfWidth, 0, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(1f, 0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            consumer.addVertex(poseMatrix, -halfWidth, -height, halfWidth).setColor(renderColor.x(), renderColor.y(), renderColor.z(), 1).setUv(1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }

        poseStack.popPose();
    }

    public static void handleRenderEvent(Entity entity, PoseStack poseStack, MultiBufferSource bufferSource) {
        Optional<Vector3f> shouldRender = shouldRender(entity);
        if (shouldRender.isPresent()) {
            Vector3f color = shouldRender.get();
            renderTargetLayer(poseStack, bufferSource, entity, color);
        }
    }
}
