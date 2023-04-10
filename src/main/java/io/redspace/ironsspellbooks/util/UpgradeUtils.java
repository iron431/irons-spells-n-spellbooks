package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;

import java.util.Collection;
import java.util.HashMap;
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

    public static EquipmentSlot getAssignedEquipmentSlot(ItemStack itemStack) {
        for (EquipmentSlot slot : EquipmentSlot.values())
            if (!itemStack.getAttributeModifiers(slot).isEmpty())
                return slot;
        return EquipmentSlot.MAINHAND;
    }

    public static double getValueByOperation(Collection<AttributeModifier> modifiers, AttributeModifier.Operation operation) {
        double total = 0;
        for (AttributeModifier a : modifiers)
            if (a.getOperation() == operation)
                total += a.getAmount();
        return total;
    }

    public static UUID UUIDForSlot(EquipmentSlot slot) {
        return UPGRADE_UUIDS_BY_SLOT.get(slot);
    }

    //public static final String Key = "irons_spellbooks";
    public static final String Upgrades = "ISBUpgrades";
    public static final String Upgrade_Key = "id";
    public static final String Slot_Key = "slot";
    public static final String Upgrade_Count = "upgrades";

    public static boolean isUpgraded(ItemStack stack) {
        //9 is the magic number here ig
        return stack.hasTag() && stack.getTag().contains(Upgrades, 9);
    }

    public static void appendUpgrade(ItemStack stack, UpgradeType upgradeType, EquipmentSlot slot) {
        //We are going to use NBT because attaching capabilities to every potentially upgradable item is dumb plus forge bug = destroy upgrades
        //Good reference: ItemStack 934, 1013, 1038
        // 10 is the magic number
        //we want to store slot, attribute, and levels applied.
        //storing the slot seems good for performance as we don't want to constantly have to search for it
        //store the attribute... no shit
        //amount per level is fixed, so we can calculate our modifier using only the levels applied
        ListTag upgrades = stack.getOrCreateTag().getList(Upgrades, 10);
        //Check if we already have this attribute upgraded
        //String attributeName = Registry.ATTRIBUTE.getKey(attribute).toString();
        for (Tag tag : upgrades) {
            CompoundTag compoundTag = (CompoundTag) tag;
            String upgradeName = compoundTag.getString(Upgrade_Key);
            if (upgradeName.equalsIgnoreCase(upgradeType.key)) {
                compoundTag.putInt(Upgrade_Count, compoundTag.getInt(Upgrade_Count) + 1);
                stack.addTagElement(Upgrades, upgrades);
                return;
            }
        }
        //Insert New Upgrade
        CompoundTag upgrade = new CompoundTag();
        upgrade.putString(Upgrade_Key, upgradeType.key);
        upgrade.putString(Slot_Key, slot.getName());
        upgrade.putInt(Upgrade_Count, 1);
        upgrades.add(upgrade);
        stack.addTagElement(Upgrades, upgrades);
    }

    public static int getUpgradeCount(ItemStack stack) {
        if (!isUpgraded(stack))
            return 0;
        ListTag upgrades = stack.getOrCreateTag().getList(Upgrades, 10);
        int count = 0;
        for (Tag tag : upgrades) {
            CompoundTag compoundTag = (CompoundTag) tag;
            count += compoundTag.getInt(Upgrade_Count);
        }
        return count;

    }

    public static EquipmentSlot getUpgradedSlot(ItemStack stack) {
        //this assumes the item is already been checked to have been upgraded... idk what will ensue if it is not (prob an index out of bounds error)
        ListTag upgrades = stack.getOrCreateTag().getList(Upgrades, 10);
        return EquipmentSlot.byName(((CompoundTag) upgrades.get(0)).getString(Slot_Key));
    }

    public static double collectAndRemovePreexistingAttribute(ItemAttributeModifierEvent event, Attribute key, AttributeModifier.Operation operationToMatch) {
        //Tactical incision to remove the preexisting attribute but preserver its value (cuz otherwise the tooltip is ugly as hell) (yes this was a lot of work to clean up the tooltip)
        if (event.getOriginalModifiers().containsKey(key)) {
            for (AttributeModifier modifier : event.getOriginalModifiers().get(key))
                if (modifier.getOperation().equals(operationToMatch)) {
                    event.removeModifier(key, modifier);
                    return modifier.getAmount();
                }
        }
        return 0;
    }

//    public static float getModifierAmount(Attribute attribute, int upgradesApplied) {
//        //TODO: switch to enum for way more flexibility
//        float amountPerUpgrade = attribute == AttributeRegistry.COOLDOWN_REDUCTION.get() ? .05f : .025f;
//        return upgradesApplied * amountPerUpgrade;
//    }

    public static Map<UpgradeType, Integer> deserializeUpgrade(ItemStack stack) {
        ListTag upgrades = stack.getOrCreateTag().getList(Upgrades, 10);
        //String attributeName = Registry.ATTRIBUTE.getKey(attribute).toString();
        Map<UpgradeType, Integer> attributes = new HashMap<>();
        for (Tag tag : upgrades) {
            CompoundTag compoundTag = (CompoundTag) tag;
            String upgradeKey = compoundTag.getString(Upgrade_Key);
            UpgradeType.getUpgrade(upgradeKey).ifPresent((upgrade) -> attributes.put(upgrade, compoundTag.getInt(Upgrade_Count)));
            //Optional<Attribute> optional = Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(attributeName));
            //optional.ifPresent((atr) -> attributes.put(atr, compoundTag.getInt(Upgrade_Count)));

        }
        return attributes;
    }
}
