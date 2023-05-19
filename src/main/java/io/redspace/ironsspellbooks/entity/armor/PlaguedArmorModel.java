package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.PlaguedArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PlaguedArmorModel extends AnimatedGeoModel<PlaguedArmorItem> {

    public PlaguedArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelLocation(PlaguedArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/plagued_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(PlaguedArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/plagued.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(PlaguedArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}