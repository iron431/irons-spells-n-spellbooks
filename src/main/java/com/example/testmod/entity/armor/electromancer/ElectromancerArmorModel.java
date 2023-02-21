package com.example.testmod.entity.armor.electromancer;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.ElectromancerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ElectromancerArmorModel extends AnimatedGeoModel<ElectromancerArmorItem> {

    public ElectromancerArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(ElectromancerArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/electromancer_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ElectromancerArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/electromancer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ElectromancerArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}