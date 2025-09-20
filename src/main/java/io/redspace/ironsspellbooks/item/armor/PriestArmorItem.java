package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.entity.armor.priest.PriestArmorModel;
import io.redspace.ironsspellbooks.entity.armor.priest.PriestArmorRenderer;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PriestArmorItem extends ImbuableChestplateArmorItem {
    public PriestArmorItem(ArmorItem.Type slot, Properties settings) {
        super(ExtendedArmorMaterials.PRIEST, slot, settings/*, schoolAttributes(AttributeRegistry.HOLY_SPELL_POWER)*/);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new PriestArmorRenderer(new PriestArmorModel());
    }
}
