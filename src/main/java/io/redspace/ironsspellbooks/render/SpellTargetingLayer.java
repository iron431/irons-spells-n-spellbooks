package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.ClientSpellTargetingData;
import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.mojang.math.Axis;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SpellTargetingLayer {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/target/heal.png");

    public static class Vanilla<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        public Vanilla(RenderLayerParent<T, M> pRenderer) {
            super(pRenderer);
        }


        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int pPackedLight, T entity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            if (shouldRender(entity)) {
                renderTargetLayer(poseStack, bufferSource, entity);
            }
        }

    }

    public static class Geo extends GeoRenderLayer<AbstractSpellCastingMob> {
        public Geo(GeoEntityRenderer<AbstractSpellCastingMob> entityRendererIn) {
            super(entityRendererIn);
        }

        @Override
        public void render(PoseStack poseStack, AbstractSpellCastingMob animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (shouldRender(animatable)) {
//                //It's upside down???
//                poseStack.mulPose(Axis.XP.rotationDegrees(180));
//                poseStack.translate(0, -(abstractSpellCastingMob.getBbWidth() + abstractSpellCastingMob.getBbHeight()) / 2, 0);
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                poseStack.translate(0, -animatable.getBoundingBox().getYsize() / 2, 0);
                renderTargetLayer(poseStack, bufferSource, animatable);
                poseStack.popPose();
            }
        }
    }

    private static Vector3f getColor(String spellId) {
        return SpellRegistry.getSpell(spellId).getTargetingColor();
    }

    public static void renderTargetLayer(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity entity) {
        //EntityRenderDispatcher#169(renderHitbox)
//        AABB aabb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
//        double magicYOffset = 1.5 - aabb.getYsize();
//        poseStack.translate(0, magicYOffset, 0);
//        poseStack.pushPose();
//        LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), aabb, 1.0F, 1.0F, 1.0F, 1.0F);
//        poseStack.popPose();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(TEXTURE, 0, 0));
        AABB aabb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());

        float width = (float) aabb.getXsize();
        float height = (float) aabb.getYsize();
        float halfWidth = width * .55f;
        float magicYOffset = (float) (1.5 - height);
        Vector3f color = null;
        if (ClientMagicData.getRecasts().hasRecastsActive()) {
            for (RecastInstance recastInstance : ClientMagicData.getRecasts().getActiveRecasts()) {
                if (recastInstance.getCastData() instanceof MultiTargetEntityCastData targetEntityCastData) {
                    if (targetEntityCastData.isTargeted(entity)) {
                        color = getColor(recastInstance.getSpellId());
                        break;
                    }
                }
            }
        }
        if (color == null) {
            color = getColor(ClientMagicData.getTargetingData().spellId);
        }
        color.mul(.4f);
        poseStack.pushPose();
        poseStack.translate(0, magicYOffset, 0);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();


        //LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), aabb, 1.0F, 1.0F, 1.0F, 1.0F);
        for (int i = 0; i < 4; i++) {
            consumer.vertex(poseMatrix, halfWidth, height, halfWidth).color(color.x(), color.y(), color.z(), 1).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, halfWidth, 0, halfWidth).color(color.x(), color.y(), color.z(), 1).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, -halfWidth, 0, halfWidth).color(color.x(), color.y(), color.z(), 1).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            consumer.vertex(poseMatrix, -halfWidth, height, halfWidth).color(color.x(), color.y(), color.z(), 1).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normalMatrix, 0f, 1f, 0f).endVertex();
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        }

        poseStack.popPose();
    }

    public static boolean shouldRender(LivingEntity entity) {
        if (ClientMagicData.getRecasts().hasRecastsActive()) {
            for (RecastInstance recastInstance : ClientMagicData.getRecasts().getActiveRecasts()) {
                if (recastInstance.getCastData() instanceof MultiTargetEntityCastData targetEntityCastData) {
                    if (targetEntityCastData.isTargeted(entity)) {
                        return true;
                    }
                }
            }
        }
        return ClientMagicData.getTargetingData().isTargeted(entity);
    }
}
