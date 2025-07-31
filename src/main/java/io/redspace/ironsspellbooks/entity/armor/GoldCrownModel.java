package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.GoldCrownArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GoldCrownModel extends GeoModel<GoldCrownArmorItem> {

    public GoldCrownModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(GoldCrownArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/tarnished_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GoldCrownArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/gold_crown.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GoldCrownArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}