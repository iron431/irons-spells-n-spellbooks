package io.redspace.ironsspellbooks.entity.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.CultistArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class CultistArmorModel extends DefaultedItemGeoModel<CultistArmorItem> {

    public CultistArmorModel() {
        super(new ResourceLocation(IronsSpellbooks.MODID, ""));
    }

    @Override
    public ResourceLocation getModelResource(CultistArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/cultist_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CultistArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/cultist.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CultistArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}