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
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GlowingEyesLayer {
    public static final ResourceLocation EYE_TEXTURE = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/entity/purple_eyes.png");
    public static final RenderType EYES = RenderType.eyes(EYE_TEXTURE);

    public static class Vanilla<T extends LivingEntity, M extends HumanoidModel<T>> extends EyesLayer<T, M> {

        public Vanilla(RenderLayerParent pRenderer) {
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

    public static class Geo extends GeoRenderLayer<AbstractSpellCastingMob> {
        public Geo(GeoEntityRenderer<AbstractSpellCastingMob> entityRendererIn) {
            super(entityRendererIn);
        }

        @Override
        public void render(PoseStack poseStack, AbstractSpellCastingMob animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            var eye = getEyeType(animatable);
            if (eye != EyeType.None) {
                bakedModel.getBone("head").ifPresent((headBone) -> {
                    var scale = getEyeScale(animatable);
                    headBone.updateScale(scale, scale, scale);

                    this.getRenderer().renderChildBones(poseStack, animatable, headBone, EYES, bufferSource, buffer, true, partialTick, packedLight, packedOverlay, eye.r, eye.g, eye.b, 1f);
                });
            }
        }
    }

    public static EyeType getEyeType(LivingEntity entity) {
        //Sorted by most prioritized color
        if (ClientMagicData.getSyncedSpellData(entity).hasEffect(SyncedSpellData.ABYSSAL_SHROUD))
            return EyeType.Abyssal;
        if (ClientMagicData.getSyncedSpellData(entity).hasEffect(SyncedSpellData.PLANAR_SIGHT))
            return EyeType.Planar_Sight;
//        else if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.SHADOWWALKER_HELMET.get()))
//            return EyeType.Ender_Armor;
        else return EyeType.None;
    }

    public static float getEyeScale(LivingEntity entity) {
        //Sorted by most prioritized scale (highest to lowest)
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ItemRegistry.SHADOWWALKER_HELMET.get()))
            return EyeType.Ender_Armor.scale;
        if (ClientMagicData.getSyncedSpellData(entity).hasEffect(SyncedSpellData.ABYSSAL_SHROUD))
            return EyeType.Abyssal.scale;
        if (ClientMagicData.getSyncedSpellData(entity).hasEffect(SyncedSpellData.PLANAR_SIGHT))
            return EyeType.Planar_Sight.scale;
        else return EyeType.None.scale;
    }

    public enum EyeType {
        None(0, 0, 0, 0),
        Abyssal(1f, 1f, 1f, 1f),
        Planar_Sight(.42f, .258f, .96f, 1f),
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

