package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.InfernalSorcererArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class InfernalSorcererArmorModel extends DefaultedItemGeoModel<InfernalSorcererArmorItem> {

    public InfernalSorcererArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(InfernalSorcererArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/infernal_sorcerer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(InfernalSorcererArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/infernal_sorcerer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(InfernalSorcererArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}