package com.example.testmod.entity.armor;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.ShadowwalkerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ShadowwalkerArmorModel extends AnimatedGeoModel<ShadowwalkerArmorItem> {

    public ShadowwalkerArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(ShadowwalkerArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/shadowwalker_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShadowwalkerArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/shadowwalker.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShadowwalkerArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
}