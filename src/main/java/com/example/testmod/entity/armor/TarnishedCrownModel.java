package com.example.testmod.entity.armor;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.TarnishedCrownArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TarnishedCrownModel extends AnimatedGeoModel<TarnishedCrownArmorItem> {

    public TarnishedCrownModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(TarnishedCrownArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/tarnished_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TarnishedCrownArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/tarnished_crown.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TarnishedCrownArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}