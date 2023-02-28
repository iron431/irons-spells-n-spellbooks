package com.example.testmod.entity.armor;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.PriestArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PriestArmorModel extends AnimatedGeoModel<PriestArmorItem> {

    public PriestArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(PriestArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/priest_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PriestArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/priest.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PriestArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}