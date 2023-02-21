package com.example.testmod.entity.armor.culist.archevoker;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.CultistArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class CultistArmorModel extends AnimatedGeoModel<CultistArmorItem> {

    public CultistArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(CultistArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/cultist_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CultistArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/cultist.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CultistArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}