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
        //TODO: (1.19.4 port) i think this is not how you're supposed to do it. see WolfArmorItem
        return new GenericCustomArmorRenderer<>(new TarnishedCrownModel() );
    }
}
