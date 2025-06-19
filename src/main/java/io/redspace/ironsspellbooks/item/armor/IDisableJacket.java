package io.redspace.ironsspellbooks.item.armor;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * When implemented as an ArmorItem, will hide the player's torso, arms, and leg jacket layer when equipped in the appropriate slots
 */
public interface IDisableJacket {
    /**
     * @param slot {@link EquipmentSlot#CHEST}, {@link EquipmentSlot#LEGS}, or {@link EquipmentSlot#FEET}
     * @return Whether to disable the skin's jacket/sleeves or pant layers when this ArmorItem is equipped in the given slot.
     */
    default boolean disableForSlot(EquipmentSlot slot) {
        return true;
    }
}
