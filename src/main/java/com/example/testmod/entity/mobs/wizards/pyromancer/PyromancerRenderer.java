package com.example.testmod.entity.mobs.wizards.pyromancer;


import com.example.testmod.TestMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class PyromancerRenderer extends HumanoidMobRenderer<PyromancerWizard, HumanoidModel<PyromancerWizard>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/pyromancer.png");

    public static ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(new ResourceLocation(TestMod.MODID, "pyromancer"), "body");

    public PyromancerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<PyromancerWizard>(context.bakeLayer(MODEL_LAYER)), 0.5f);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(PyromancerWizard entity) {
        return TEXTURE;
    }
}
