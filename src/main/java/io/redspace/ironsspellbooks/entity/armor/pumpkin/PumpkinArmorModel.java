package io.redspace.ironsspellbooks.entity.armor.pumpkin;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.PumpkinArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PumpkinArmorModel extends GeoModel<PumpkinArmorItem> {

    public PumpkinArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(PumpkinArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/pumpkin_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PumpkinArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/pumpkin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PumpkinArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}