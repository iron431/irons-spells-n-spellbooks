package io.redspace.ironsspellbooks.item.armor;


import io.redspace.ironsspellbooks.entity.armor.ArchevokerArmorModel;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class ArchevokerArmorItem extends ImbuableChestplateArmorItem {
    public ArchevokerArmorItem(Type type, Properties settings) {
        super(ExtendedArmorMaterials.ARCHEVOKER, type, settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
        public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new ArchevokerArmorModel());
    }
}
