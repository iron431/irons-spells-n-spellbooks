package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.CryomancerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class CryomancerArmorModel extends AnimatedGeoModel<CryomancerArmorItem> {

    public CryomancerArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(CryomancerArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/cryomancer_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CryomancerArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/cryomancer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CryomancerArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}