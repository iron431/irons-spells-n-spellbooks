package com.example.testmod.entity.armor.simple_wizard;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.WizardArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import java.util.List;

public class WizardArmorModel extends AnimatedGeoModel<WizardArmorItem> {

    public WizardArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(WizardArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/wizard_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WizardArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/wizard_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WizardArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}