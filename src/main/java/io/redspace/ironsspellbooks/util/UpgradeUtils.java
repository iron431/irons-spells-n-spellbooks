package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.capabilities.magic.UpgradeData;
import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class UpgradeUtils {

    //We need consistent UUIDs. Different Attributes on a player piece can have the same uuid, but duplicate UUID and attributes will break (UUID per attribute you can say)
    public static final Map<EquipmentSlot, UUID> UPGRADE_UUIDS_BY_SLOT = Map.of(
            EquipmentSlot.HEAD, UUID.fromString("f6c19678-1c70-4d41-ad19-cd84d8610242"),
            EquipmentSlot.CHEST, UUID.fromString("8d02c916-b0eb-4d17-8414-329b4bd38ae7"),
            EquipmentSlot.LEGS, UUID.fromString("3739c748-98d4-4a2d-9c25-3b4dec74823d"),
            EquipmentSlot.FEET, UUID.fromString("41cede88-7881-42dd-aac3-d6ab4b56b1f2"),
            EquipmentSlot.MAINHAND, UUID.fromString("c3865ad7-1f35-46d4-8b4b-a6b934a1a896"),
            EquipmentSlot.OFFHAND, UUID.fromString("c508430e-7497-42a9-9a9c-1a324dccca54")
    );

    public static EquipmentSlot getRelevantEquipmentSlot(ItemStack itemStack) {
        for (EquipmentSlot slot : EquipmentSlot.values())
            if (!itemStack.getAttributeModifiers(slot).isEmpty())
                return slot;
        return EquipmentSlot.MAINHAND;
    }

    public static UUID UUIDForSlot(EquipmentSlot slot) {
        return UPGRADE_UUIDS_BY_SLOT.get(slot);
    }

    public static void handleAttributeEvent(ItemAttributeModifierEvent event, UpgradeData upgradeData){
        var upgrades = upgradeData.getUpgrades();
        for (Map.Entry<UpgradeType, Integer> entry : upgrades.entrySet()) {
            UpgradeType upgradeType = entry.getKey();
            int count = entry.getValue();
            double baseAmount = UpgradeUtils.collectAndRemovePreexistingAttribute(event, upgradeType.attribute, upgradeType.operation);
            event.addModifier(upgradeType.attribute, new AttributeModifier(UpgradeUtils.UUIDForSlot(event.getSlotType()), "upgrade", baseAmount + upgradeType.amountPerUpgrade * count, entry.getKey().operation));
        }
    }

    public static double collectAndRemovePreexistingAttribute(ItemAttributeModifierEvent event, Attribute key, AttributeModifier.Operation operationToMatch) {
        //Tactical incision to remove the preexisting attribute but preserve its value
        if (event.getOriginalModifiers().containsKey(key)) {
            for (AttributeModifier modifier : event.getOriginalModifiers().get(key))
                if (modifier.getOperation().equals(operationToMatch)) {
                    event.removeModifier(key, modifier);
                    return modifier.getAmount();
                }
        }
        return 0;
    }
}
