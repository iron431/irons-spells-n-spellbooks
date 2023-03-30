package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class GlowingEyesLayer {
    public static final ResourceLocation EYE_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/purple_eyes.png");
    public static final RenderType EYES = RenderType.eyes(EYE_TEXTURE);

    public static class GlowingEyesVanilla<T extends LivingEntity, M extends HumanoidModel<T>> extends EyesLayer<T, M> {

        public GlowingEyesVanilla(RenderLayerParent pRenderer) {
            super(pRenderer);
        }

        @Override
        public RenderType renderType() {
            return EYES;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int pPackedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            var eye = getEyeType(livingEntity);
            if (eye != EyeType.None) {
                VertexConsumer vertexconsumer = multiBufferSource.getBuffer(this.renderType());

                //pMatrixStack.translate(0, -eye.yOffset, -eye.forwardOffset);
                float scale = getEyeScale(livingEntity);
                poseStack.scale(scale, scale, scale);
                this.getParentModel().renderToBuffer(poseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, eye.r, eye.g, eye.b, 1.0F);
            }
        }
    }

    public static class GlowingEyesGeo extends GeoLayerRenderer<AbstractSpellCastingMob> {
        public GlowingEyesGeo(IGeoRenderer entityRendererIn) {
            super(entityRendererIn);
        }

        @Override
        public RenderType getRenderType(ResourceLocation textureLocation) {
            return EYES;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLightIn, AbstractSpellCastingMob abstractSpellCastingMob, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            var eye = getEyeType(abstractSpellCastingMob);
            if (eye != EyeType.None) {
                var model = entityRenderer.getGeoModelProvider().getModel(entityRenderer.getGeoModelProvider().getModelResource(abstractSpellCastingMob));
                model.getBone("head").ifPresent((headBone) -> {
                    var scale = getEyeScale(abstractSpellCastingMob);
                    headBone.setScale(scale, scale, scale);
                    this.renderModel(this.getEntityModel(), EYE_TEXTURE, poseStack, multiBufferSource, packedLightIn, abstractSpellCastingMob, partialTicks, eye.r, eye.g, eye.b);
                });
            }
        }
    }

    public static EyeType getEyeType(LivingEntity entity) {
        //Sorted by most prioritized color
        if (ClientMagicData.getSyncedSpellData(entity).hasEffect(SyncedSpellData.ABYSSAL_SHROUD))
            return EyeType.Abyssal;
        else if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.SHADOWWALKER_HELMET.get()))
            return EyeType.Ender_Armor;
        else return EyeType.None;
    }

    public static float getEyeScale(LivingEntity entity) {
        //Sorted by most prioritized scale (highest to lowest)
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.SHADOWWALKER_HELMET.get()))
            return EyeType.Ender_Armor.scale;
        if (ClientMagicData.getSyncedSpellData(entity).hasEffect(SyncedSpellData.ABYSSAL_SHROUD))
            return EyeType.Abyssal.scale;
        else return EyeType.None.scale;
    }

    public enum EyeType {
        None(0, 0, 0, 0),
        Abyssal(1f, 1f, 1f, 1f),
        Ender_Armor(.816f, 0f, 1f, 1.15f);

        public final float r, g, b, scale;

        EyeType(float r, float g, float b, float scale) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.scale = scale;
        }
    }
}

