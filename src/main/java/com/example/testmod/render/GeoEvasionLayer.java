package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.debug_wizard.DebugWizard;
import com.example.testmod.entity.mobs.debug_wizard.DebugWizardModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

@OnlyIn(Dist.CLIENT)
public class GeoEvasionLayer extends GeoLayerRenderer<DebugWizard> {
    private static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/evasion.png");

    public GeoEvasionLayer(IGeoRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, DebugWizard entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        //if (ClientMagicData.getSyncedSpellData(entityLivingBaseIn).hasEffect(SyncedSpellData.EVASION)) {
        float f = (float) entityLivingBaseIn.tickCount + partialTicks;
        VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderType.energySwirl(EVASION_TEXTURE, this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));

        matrixStackIn.pushPose();
        //Move or scale the model as you see fit
        //matrixStackIn.scale(1.0f, 1.0f, 1.0f);
        //matrixStackIn.translate(0.0d, 0.0d, 0.0d);
        RenderType renderType = RenderType.armorCutoutNoCull(EVASION_TEXTURE);

        this.getRenderer().render(this.getEntityModel().getModel(DebugWizardModel.modelResource), entityLivingBaseIn, partialTicks, renderType, matrixStackIn, bufferIn,
                vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, .5f, .5f, .5f, 1f);
        matrixStackIn.popPose();

        //entitymodel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        //entitymodel.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
        //}
    }

    public float xOffset(float offset) {
        return offset * 0.02F;
    }

}