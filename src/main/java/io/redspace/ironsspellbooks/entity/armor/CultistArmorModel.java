package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.CultistArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class CultistArmorModel extends AnimatedGeoModel<CultistArmorItem> {

    public CultistArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(CultistArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/cultist_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CultistArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/cultist.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CultistArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}