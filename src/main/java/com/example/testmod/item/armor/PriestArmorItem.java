package com.example.testmod.item.armor;

import net.minecraft.world.entity.EquipmentSlot;

public class PriestArmorItem extends ExtendedArmorItem{
    public PriestArmorItem(EquipmentSlot slot, Properties settings) {
        super(ExtendedArmorMaterials.PRIEST, slot, settings);
    }
}
