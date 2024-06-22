package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.PyromancerArmorModel;


import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PyromancerArmorItem extends ImbuableChestplateArmorItem {
    public PyromancerArmorItem(Type slot, Properties settings) {
        super(ExtendedArmorMaterials.PYROMANCER, slot, settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new PyromancerArmorModel());
    }
}
