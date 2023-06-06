package io.redspace.ironsspellbooks.item.armor;


import io.redspace.ironsspellbooks.entity.armor.ArchevokerArmorModel;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class ArchevokerArmorItem extends ExtendedArmorItem {
    public ArchevokerArmorItem(Type type, Properties settings) {
        super(ExtendedArmorMaterials.ARCHEVOKER, type, settings);
    }

    @Override
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new ArchevokerArmorModel());
    }
}
