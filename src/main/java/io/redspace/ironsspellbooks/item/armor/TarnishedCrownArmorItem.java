package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.PlaguedArmorModel;
import io.redspace.ironsspellbooks.entity.armor.TarnishedCrownModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class TarnishedCrownArmorItem extends ExtendedArmorItem {
    public TarnishedCrownArmorItem(ArmorItem.Type slot, Properties settings) {
        super(ExtendedArmorMaterials.TARNISHED, slot, settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new TarnishedCrownModel() );
    }
}
