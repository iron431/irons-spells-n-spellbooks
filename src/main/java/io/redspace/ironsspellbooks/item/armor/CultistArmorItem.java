package io.redspace.ironsspellbooks.item.armor;

import net.minecraft.world.entity.EquipmentSlot;

public class CultistArmorItem extends ImbuableChestplateArmorItem {
    public CultistArmorItem(EquipmentSlot slot, Properties settings) {
        super(ExtendedArmorMaterials.CULTIST, slot, settings);
    }
}
