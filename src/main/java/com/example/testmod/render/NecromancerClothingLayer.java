package com.example.testmod.render;

import com.example.testmod.TestMod;
import com.example.testmod.entity.mobs.necromancer.NecromancerEntity;
import com.example.testmod.entity.mobs.necromancer.NecromancerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class NecromancerClothingLayer extends RenderLayer<NecromancerEntity, NecromancerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/necromancer/necromancer_overlay.png");
    private final NecromancerModel layerModel;

    public NecromancerClothingLayer(RenderLayerParent<NecromancerEntity, NecromancerModel> pRenderer, EntityModelSet p_174545_) {
        super(pRenderer);
        this.layerModel = new NecromancerModel(p_174545_.bakeLayer(ModelLayers.STRAY_OUTER_LAYER));
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, NecromancerEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, TEXTURE, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, 1.0F, 1.0F, 1.0F);
    }
}