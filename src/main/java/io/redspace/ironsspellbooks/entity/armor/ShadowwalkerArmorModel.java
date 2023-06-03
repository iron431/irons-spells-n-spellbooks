package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.ShadowwalkerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ShadowwalkerArmorModel extends GeoModel<ShadowwalkerArmorItem> {

    public ShadowwalkerArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(ShadowwalkerArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/shadowwalker_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShadowwalkerArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/shadowwalker.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShadowwalkerArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}