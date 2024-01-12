package io.redspace.ironsspellbooks.entity.armor.netherite;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.armor.ArchevokerArmorItem;
import io.redspace.ironsspellbooks.item.armor.NetheriteMageArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class NetheriteMageArmorModel extends AnimatedGeoModel<NetheriteMageArmorItem> {

    public NetheriteMageArmorModel(){
        super();

    }
    @Override
    public ResourceLocation getModelResource(NetheriteMageArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "geo/netherite_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NetheriteMageArmorItem object) {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/netherite.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NetheriteMageArmorItem animatable) {
        return new ResourceLocation(IronsSpellbooks.MODID, "animations/wizard_armor_animation.json");
    }
}