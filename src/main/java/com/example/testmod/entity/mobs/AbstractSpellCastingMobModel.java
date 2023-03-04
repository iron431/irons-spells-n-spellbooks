package com.example.testmod.entity.mobs;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AbstractSpellCastingMobModel extends AnimatedGeoModel<AbstractSpellCastingMob> {

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return AbstractSpellCastingMob.modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return AbstractSpellCastingMob.textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return AbstractSpellCastingMob.animationInstantCast;
    }
}