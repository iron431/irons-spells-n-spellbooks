package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class ElectromancerArmorItem extends ImbuableChestplateArmorItem implements ArmorCapeProvider {
    public ElectromancerArmorItem(EquipmentSlot slot, Properties settings) {
        super(ExtendedArmorMaterials.ELECTROMANCER, slot, settings);
    }

    @Override
    public ResourceLocation getCapeResourceLocation() {
        return new ResourceLocation(IronsSpellbooks.MODID, "textures/models/armor/electromancer_cape.png");
    }
}
