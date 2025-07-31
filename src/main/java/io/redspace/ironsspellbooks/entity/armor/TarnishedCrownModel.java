package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.TarnishedCrownArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TarnishedCrownModel extends GeoModel<TarnishedCrownArmorItem> {

    public TarnishedCrownModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(TarnishedCrownArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/tarnished_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TarnishedCrownArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/tarnished.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TarnishedCrownArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}