package com.example.testmod.entity.wisp;

import com.example.testmod.TestMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WispModel extends AnimatedGeoModel<WispEntity> {
    public static final ResourceLocation modelResource = new ResourceLocation(TestMod.MODID, "geo/wisp.geo.json");
    public static final ResourceLocation textureResource = new ResourceLocation(TestMod.MODID, "textures/entity/wisp/wisp.png");
    public static final ResourceLocation animationResource = new ResourceLocation(TestMod.MODID, "animations/wisp.animation.json");


    @Override
    public ResourceLocation getModelResource(WispEntity object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(WispEntity object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(WispEntity animatable) {
        return animationResource;
    }
}
