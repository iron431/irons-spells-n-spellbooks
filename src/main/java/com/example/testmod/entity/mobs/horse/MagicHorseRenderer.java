package com.example.testmod.entity.mobs.horse;

import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MagicHorseRenderer extends AbstractHorseRenderer<SpectralSteed, HorseModel<SpectralSteed>> {
    public MagicHorseRenderer(EntityRendererProvider.Context p_174167_) {
        super(p_174167_, new HorseModel<>(p_174167_.bakeLayer(ModelLayers.HORSE)), 1.1F);
        //.addLayer(new HorseMarkingLayer(this));
        //this.addLayer(new HorseArmorLayer(this, p_174167_.getModelSet()));
    }
//    public MagicHorseRenderer(EntityRendererProvider.Context pContext, HorseModel pModel, float pScale) {
//        super(pContext, pModel, pScale);
//    }

    @Override
    public ResourceLocation getTextureLocation(SpectralSteed pEntity) {
        return new ResourceLocation("textures/entity/horse/horse_skeleton.png");
    }
}
