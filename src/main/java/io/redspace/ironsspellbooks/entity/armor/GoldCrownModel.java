package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.GoldCrownArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GoldCrownModel extends AnimatedGeoModel<GoldCrownArmorItem> {

    public GoldCrownModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(GoldCrownArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/tarnished_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GoldCrownArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/gold_crown.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GoldCrownArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}