package io.redspace.ironsspellbooks.entity.armor.priest;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.ExtendedArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class PriestArmorModel extends DefaultedItemGeoModel<ExtendedArmorItem> {

    public PriestArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "armor/priest"));
    }

    @Override
    public ResourceLocation getModelResource(ExtendedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/priest_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ExtendedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/priest.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ExtendedArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}