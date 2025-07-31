package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.PriestArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class PriestArmorModel extends DefaultedItemGeoModel<PriestArmorItem> {

    public PriestArmorModel(){
        super(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "armor/priest"));
    }

    @Override
    public ResourceLocation getModelResource(PriestArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/priest_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PriestArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/priest.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PriestArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}