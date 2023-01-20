package com.example.testmod.entity.armor;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.WizardArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WizardArmorModel extends AnimatedGeoModel<WizardArmorItem> {
    @Override
    public ResourceLocation getModelResource(WizardArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/wizard_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WizardArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/armor/wizard_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WizardArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
}