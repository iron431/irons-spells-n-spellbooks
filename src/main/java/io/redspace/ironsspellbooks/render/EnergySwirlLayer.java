package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

@OnlyIn(Dist.CLIENT)
public class EnergySwirlLayer {
    public static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/evasion.png");
    public static final ResourceLocation CHARGE_TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/charged.png");

    public static class Vanilla extends RenderLayer<Player, HumanoidModel<Player>> {
        public static ModelLayerLocation ENERGY_LAYER = new ModelLayerLocation(new ResourceLocation(IronsSpellbooks.MODID, "energy_layer"), "main");
        private final HumanoidModel<Player> model;
        private final ResourceLocation TEXTURE;
        private final Long shouldRenderFlag;

        public Vanilla(RenderLayerParent pRenderer, ResourceLocation texture, Long shouldRenderFlag) {
            super(pRenderer);
            this.model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ENERGY_LAYER));
            this.TEXTURE = texture;
            this.shouldRenderFlag = shouldRenderFlag;
        }

        public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Player pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            if (EnergySwirlLayer.shouldRender(pLivingEntity, shouldRenderFlag)) {
                float f = (float) pLivingEntity.tickCount + pPartialTicks;
                HumanoidModel<Player> entitymodel = this.model();
                entitymodel.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
                this.getParentModel().copyPropertiesTo(entitymodel);
                VertexConsumer vertexconsumer = pBuffer.getBuffer(EnergySwirlLayer.getRenderType(TEXTURE, f));
                entitymodel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
                entitymodel.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0.8F, 0.8F, 0.8F, 1.0F);
            }
        }

        protected HumanoidModel<Player> model() {
            return model;
        }

        protected boolean shouldRender(Player entity) {
            return true;
        }
    }

    public static class Geo extends GeoLayerRenderer<AbstractSpellCastingMob> {
        private final ResourceLocation TEXTURE/* = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/evasion.png")*/;
        private final Long shouldRenderFlag;

        public Geo(IGeoRenderer entityRendererIn, ResourceLocation texture, Long shouldRenderFlag) {
            super(entityRendererIn);
            this.TEXTURE = texture;
            this.shouldRenderFlag = shouldRenderFlag;

        }

        @Override
        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractSpellCastingMob entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (EnergySwirlLayer.shouldRender(entityLivingBaseIn, shouldRenderFlag)) {
                float f = (float) entityLivingBaseIn.tickCount + partialTicks;
                var renderType = EnergySwirlLayer.getRenderType(TEXTURE, f);
                VertexConsumer vertexconsumer = bufferIn.getBuffer(renderType);
                matrixStackIn.pushPose();
//            this.getRenderer().setCurrentRTB(bufferIn);
                var model = ((AnimatedGeoModel) this.getEntityModel()).getModel(AbstractSpellCastingMob.modelResource);
                model.getBone("body").ifPresent((rootBone) -> {
                    rootBone.childBones.forEach(bone -> {
                        bone.setScale(1.1f, 1.1f, 1.1f);
                    });
                });
                this.getRenderer().render(model, entityLivingBaseIn, partialTicks, renderType, matrixStackIn, bufferIn,
                        vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, .5f, .5f, .5f, 1f);
                model.getBone("body").ifPresent((rootBone) -> {
                    rootBone.childBones.forEach(bone -> {
                        bone.setScale(1f, 1f, 1f);
                    });
                });
                matrixStackIn.popPose();
            }
        }
    }

    private static RenderType getRenderType(ResourceLocation texture, float f) {
        return RenderType.energySwirl(texture, f * 0.02F % 1.0F, f * 0.01F % 1.0F);
    }

    private static boolean shouldRender(LivingEntity entity, Long shouldRenderFlag) {
        return ClientMagicData.getSyncedSpellData(entity).hasEffect(shouldRenderFlag);
    }
}
