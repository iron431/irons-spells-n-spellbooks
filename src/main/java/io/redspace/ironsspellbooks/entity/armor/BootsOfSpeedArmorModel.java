package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.BootsOfSpeedArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class BootsOfSpeedArmorModel extends DefaultedItemGeoModel<BootsOfSpeedArmorItem> {

    public BootsOfSpeedArmorModel() {
        super(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(BootsOfSpeedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/boots_of_speed.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BootsOfSpeedArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "textures/models/armor/boots_of_speed.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BootsOfSpeedArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}