package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.CryomancerArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class CryomancerArmorModel extends DefaultedItemGeoModel<CryomancerArmorItem> {

    public CryomancerArmorModel() {
        super(new ResourceLocation(IronsSpellbooks.MODID, ""));
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
}