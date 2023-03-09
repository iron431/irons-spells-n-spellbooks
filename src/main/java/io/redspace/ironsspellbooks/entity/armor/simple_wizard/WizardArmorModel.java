package io.redspace.ironsspellbooks.entity.armor.simple_wizard;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.WizardArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WizardArmorModel extends AnimatedGeoModel<WizardArmorItem> {

    public WizardArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(WizardArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/wizard_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WizardArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/wizard_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WizardArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
//    public static String listOfBonesToString(List<IBone> list){
//        String s = "";
//        for (IBone o:list)
//            s += o.getName()+", ";
//        return s;
//    }
}