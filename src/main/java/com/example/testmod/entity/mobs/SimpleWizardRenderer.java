package com.example.testmod.entity.mobs;


import com.example.testmod.TestMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class SimpleWizardRenderer extends HumanoidMobRenderer<SimpleWizard, SimpleWizardModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/simple_wizard.png");

    public SimpleWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new SimpleWizardModel(context.bakeLayer(SimpleWizardModel.SIMPLE_WIZARD_LAYER)), 1f);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(SimpleWizard entity) {
        return TEXTURE;
    }
}
