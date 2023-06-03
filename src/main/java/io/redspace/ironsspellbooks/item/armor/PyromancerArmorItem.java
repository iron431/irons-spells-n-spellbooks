package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.PyromancerArmorModel;
import io.redspace.ironsspellbooks.entity.armor.pumpkin.PumpkinArmorModel;
import io.redspace.ironsspellbooks.entity.armor.pumpkin.PumpkinArmorRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PyromancerArmorItem extends ExtendedArmorItem{
    public PyromancerArmorItem(Type slot, Properties settings) {
        super(ExtendedArmorMaterials.PYROMANCER, slot, settings);
    }

    @Override
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new PyromancerArmorModel());
    }
}
