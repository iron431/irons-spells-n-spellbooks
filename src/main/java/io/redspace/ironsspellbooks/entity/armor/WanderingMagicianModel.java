package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.WanderingMagicianArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WanderingMagicianModel extends AnimatedGeoModel<WanderingMagicianArmorItem> {

    public WanderingMagicianModel(){
        super();

    }
    @Override
    public ResourceLocation getModelLocation(WanderingMagicianArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/wandering_magician_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(WanderingMagicianArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/wandering_magician.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(WanderingMagicianArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}