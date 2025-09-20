package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.entity.armor.CultistArmorModel;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class CultistArmorItem extends ImbuableChestplateArmorItem {
    public CultistArmorItem(ArmorItem.Type slot, Properties settings) {
        super(ExtendedArmorMaterials.CULTIST, slot, settings/*, schoolAttributes(AttributeRegistry.BLOOD_SPELL_POWER)*/);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new CultistArmorModel());
    }
}
