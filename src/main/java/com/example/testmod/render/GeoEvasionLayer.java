package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.entity.mobs.AbstractSpellCastingMob;
import com.example.testmod.player.ClientMagicData;
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
public class GeoEvasionLayer extends GeoLayerRenderer<AbstractSpellCastingMob> {
    private static final ResourceLocation EVASION_TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/evasion.png");

    public GeoEvasionLayer(IGeoRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractSpellCastingMob entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (ClientMagicData.getSyncedSpellData(entityLivingBaseIn).hasEffect(SyncedSpellData.EVASION)) {
            float f = (float) entityLivingBaseIn.tickCount + partialTicks;
            VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderType.energySwirl(EVASION_TEXTURE, f * 0.02F % 1.0F, f * 0.01F % 1.0F));
            matrixStackIn.pushPose();
            RenderType renderType = RenderType.armorCutoutNoCull(EVASION_TEXTURE);
            this.getRenderer().setCurrentRTB(bufferIn);
            this.getRenderer().render(this.getEntityModel().getModel(AbstractSpellCastingMob.modelResource), entityLivingBaseIn, partialTicks, renderType, matrixStackIn, null,
                    vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, .5f, .5f, .5f, 1f);
            matrixStackIn.popPose();
        }
    }
}