package com.example.testmod.entity.armor;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.ArchevokerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ArchevokerArmorModel extends AnimatedGeoModel<ArchevokerArmorItem> {

    public ArchevokerArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(ArchevokerArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/archevoker_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ArchevokerArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/archevoker.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ArchevokerArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}