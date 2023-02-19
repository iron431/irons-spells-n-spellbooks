package com.example.testmod.entity.armor;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.ExtendedArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GenericGeoArmorModel extends AnimatedGeoModel<ExtendedArmorItem> {

    public GenericGeoArmorModel() {
        super();
    }

    @Override
    public ResourceLocation getModelResource(ExtendedArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/" + object.getMaterial().getName() + "_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ExtendedArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/" + object.getMaterial().getName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(ExtendedArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }

}