package io.redspace.ironsspellbooks.entity.spells.void_tentacle;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class VoidTentacleModel extends AnimatedGeoModel<VoidTentacle> {
    public static final ResourceLocation modelResource = new ResourceLocation(IronsSpellbooks.MODID, "geo/void_tentacle.geo.json");
    public static final ResourceLocation textureResource = IronsSpellbooks.id("textures/entity/void_tentacle.png");
    public static final ResourceLocation animationResource = new ResourceLocation(IronsSpellbooks.MODID, "animations/void_tentacle_animations.json");

    @Override
    public ResourceLocation getModelResource(VoidTentacle object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(VoidTentacle mob) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(VoidTentacle animatable) {
        return animationResource;
    }

}