package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.ElectromancerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ElectromancerArmorModel extends AnimatedGeoModel<ElectromancerArmorItem> {

    public ElectromancerArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelLocation(ElectromancerArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/electromancer_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ElectromancerArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/electromancer.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ElectromancerArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}