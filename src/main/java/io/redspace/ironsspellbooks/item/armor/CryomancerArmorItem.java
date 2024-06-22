package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.entity.armor.CryomancerArmorModel;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import net.minecraft.world.item.ArmorItem;


import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class CryomancerArmorItem extends ImbuableChestplateArmorItem {
    public CryomancerArmorItem(ArmorItem.Type slot, Properties settings) {
        super(ExtendedArmorMaterials.CRYOMANCER, slot, settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new CryomancerArmorModel());
    }
}
