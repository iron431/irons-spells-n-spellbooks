package io.redspace.ironsspellbooks.item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PumpkinArmorItem extends ExtendedArmorItem {
    public PumpkinArmorItem(EquipmentSlot slot, Properties settings) {
        super(ExtendedArmorMaterials.PUMPKIN, slot, settings);
    }

    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(this);
    }
}
