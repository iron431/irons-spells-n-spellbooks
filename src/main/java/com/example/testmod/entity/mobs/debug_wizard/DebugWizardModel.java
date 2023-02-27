package com.example.testmod.entity.mobs.debug_wizard;

import com.example.testmod.TestMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class DebugWizardModel extends AnimatedGeoModel<DebugWizard> {
    public static final ResourceLocation modelResource = new ResourceLocation(TestMod.MODID, "geo/abstract_casting_mob.geo.json");
    public static final ResourceLocation textureResource = new ResourceLocation(TestMod.MODID, "textures/entity/abstract_casting_mob/abstract_casting_mob.png");
    public static final ResourceLocation animationResource = new ResourceLocation(TestMod.MODID, "animations/instant_cast.json");

    @Override
    public ResourceLocation getModelResource(DebugWizard object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(DebugWizard object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(DebugWizard animatable) {
        return animationResource;
    }
}