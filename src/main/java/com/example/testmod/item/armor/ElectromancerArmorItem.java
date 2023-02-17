package com.example.testmod.item.armor;

import com.example.testmod.TestMod;
import com.example.testmod.item.ArmorCapeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class ElectromancerArmorItem extends ExtendedArmorItem implements ArmorCapeProvider {
    public ElectromancerArmorItem(EquipmentSlot slot, Properties settings) {
        super(ExtendedArmorMaterials.ELECTROMANCER, slot, settings);
    }

    @Override
    public ResourceLocation getCapeResourceLocation() {
        return new ResourceLocation(TestMod.MODID, "textures/models/armor/electromancer_cape.png");
    }
}
