package com.example.testmod.entity.armor;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.PyromancerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PyromancerArmorModel extends AnimatedGeoModel<PyromancerArmorItem> {

    public PyromancerArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(PyromancerArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/pyromancer_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PyromancerArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/pyromancer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PyromancerArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}