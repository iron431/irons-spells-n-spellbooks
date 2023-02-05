package com.example.testmod.entity.armor.wandering_magician;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.WanderingMagicianArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WanderingMagicianModel extends AnimatedGeoModel<WanderingMagicianArmorItem> {

    public WanderingMagicianModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(WanderingMagicianArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/pyromancer_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WanderingMagicianArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/pyromancer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WanderingMagicianArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}