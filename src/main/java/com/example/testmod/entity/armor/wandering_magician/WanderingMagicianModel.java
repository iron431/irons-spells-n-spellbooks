package com.example.testmod.entity.armor.wandering_magician;

import com.example.testmod.TestMod;
import com.example.testmod.item.armor.WanderMagicianArmorItem;
import com.example.testmod.item.armor.WizardArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WanderingMagicianModel extends AnimatedGeoModel<WanderMagicianArmorItem> {

    public WanderingMagicianModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(WanderMagicianArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "geo/wandering_magician_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WanderMagicianArmorItem object) {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/wandering_magician.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WanderMagicianArmorItem animatable) {
        return new ResourceLocation(TestMod.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}