package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@OnlyIn(Dist.CLIENT)
public class EchoingStrikesHologramLayer {
    public static class Vanilla extends RenderLayer<Player, HumanoidModel<Player>> {

        public Vanilla(RenderLayerParent pRenderer) {
            super(pRenderer);
        }

        public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Player pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            float percent = getRenderPercent(pLivingEntity);
            if (percent > 0) {
                float f = (float) pLivingEntity.tickCount + pPartialTicks;
                float offset = Mth.sin(f * 2) * 0.75f * percent;
                pMatrixStack.translate(offset, 0, 0);
                pMatrixStack.scale(1.01f, 1.01f, 1.01f);
                int color = RenderHelper.colorf(.988f * percent, .313f * percent, .968f * percent, percent);
                this.getParentModel().renderToBuffer(pMatrixStack, pBuffer.getBuffer(RenderHelper.CustomerRenderType.magic(this.getTextureLocation(pLivingEntity))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
            }
        }
    }

    public static class Geo extends GeoRenderLayer<AbstractSpellCastingMob> {
        public Geo(GeoEntityRenderer<AbstractSpellCastingMob> entityRendererIn) {
            super(entityRendererIn);
        }

        @Override
        public void render(PoseStack pMatrixStack, AbstractSpellCastingMob pLivingEntity, BakedGeoModel bakedModel, RenderType renderType2, MultiBufferSource bufferSource, VertexConsumer buffer, float pPartialTicks, int packedLight, int packedOverlay) {
            float percent = getRenderPercent(pLivingEntity);
            if (percent > 0) {
                float f = (float) pLivingEntity.tickCount + pPartialTicks;
                float offset = Mth.sin(f * 2) * 0.75f * percent;
                pMatrixStack.translate(offset, 0, 0);
                pMatrixStack.scale(1.01f, 1.01f, 1.01f);
                int color = RenderHelper.colorf(.988f * percent, .313f * percent, .968f * percent, percent);
                var type = RenderHelper.CustomerRenderType.magic(this.getRenderer().getTextureLocation(pLivingEntity));
                this.getRenderer().actuallyRender(pMatrixStack, pLivingEntity, bakedModel, type, bufferSource, bufferSource.getBuffer(type), true, pPartialTicks, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
            }
        }
    }

    private static float getRenderPercent(LivingEntity entity) {
        return 0;/*entity.hasData(DataAttachmentRegistry.ECHOING_STRIKES_DATA) ?
                Mth.clamp((entity.getData(DataAttachmentRegistry.ECHOING_STRIKES_DATA).vfxTimestamp - entity.tickCount) / 20f, 0, 1) :
                0;*/
    }
}
