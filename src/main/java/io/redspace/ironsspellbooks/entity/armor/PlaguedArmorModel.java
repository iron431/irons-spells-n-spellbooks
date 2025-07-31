package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.PlaguedArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlaguedArmorModel extends GeoModel<PlaguedArmorItem> {

    public PlaguedArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(PlaguedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/plagued_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlaguedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/plagued.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlaguedArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}