package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.ShadowwalkerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ShadowwalkerArmorModel extends AnimatedGeoModel<ShadowwalkerArmorItem> {

    public ShadowwalkerArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelLocation(ShadowwalkerArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/shadowwalker_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowwalkerArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/shadowwalker.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ShadowwalkerArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}