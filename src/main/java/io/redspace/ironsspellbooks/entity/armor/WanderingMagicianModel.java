package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.WanderingMagicianArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WanderingMagicianModel extends GeoModel<WanderingMagicianArmorItem> {

    public WanderingMagicianModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(WanderingMagicianArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/wandering_magician_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WanderingMagicianArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/wandering_magician.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WanderingMagicianArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}