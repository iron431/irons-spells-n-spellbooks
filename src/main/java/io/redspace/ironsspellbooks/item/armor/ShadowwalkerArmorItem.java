package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.ShadowwalkerArmorModel;
import net.minecraft.world.item.ArmorItem;


import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class ShadowwalkerArmorItem extends ImbuableChestplateArmorItem {
    public ShadowwalkerArmorItem(ArmorItem.Type slot, Properties settings) {
        super(ExtendedArmorMaterials.SHADOWWALKER, slot, settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new ShadowwalkerArmorModel());
    }
}
