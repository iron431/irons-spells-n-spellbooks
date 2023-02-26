package com.example.testmod.entity.mobs.debug_wizard;


import com.example.testmod.TestMod;
import com.example.testmod.render.ChargeSpellLayer;
import com.example.testmod.render.EvasionLayer;
import com.example.testmod.render.GlowingEyesLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class DebugWizardRenderer extends HumanoidMobRenderer<DebugWizard, DebugWizardModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TestMod.MODID, "textures/entity/simple_wizard.png");

    public DebugWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new DebugWizardModel(context.bakeLayer(DebugWizardModel.SIMPLE_WIZARD_LAYER)), 1f);
        this.addLayer(new EvasionLayer<>(this));
        this.addLayer(new ChargeSpellLayer<>(this));
        this.addLayer(new GlowingEyesLayer<>(this));

    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(DebugWizard entity) {
        return TEXTURE;
    }
}
