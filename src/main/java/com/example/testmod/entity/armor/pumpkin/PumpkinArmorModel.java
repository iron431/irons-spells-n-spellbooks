package com.example.testmod.entity.armor.pumpkin;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.PumpkinArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PumpkinArmorModel extends AnimatedGeoModel<PumpkinArmorItem> {

    public PumpkinArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(PumpkinArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/pumpkin_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PumpkinArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/pumpkin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PumpkinArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}