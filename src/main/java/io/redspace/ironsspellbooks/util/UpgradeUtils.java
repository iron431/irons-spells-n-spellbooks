package io.redspace.ironsspellbooks.util;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

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

    public static String getRelevantEquipmentSlot(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ICurioItem curioItem) {
            var tags = CuriosApi.getCuriosHelper().getCurioTags((Item) curioItem);
            var slot = tags.stream().findFirst();
            if (slot.isPresent()) {
                return slot.get();
            }
        } else if (itemStack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getEquipmentSlot().getName();
        }
        return EquipmentSlot.MAINHAND.getName();
    }

    public static UUID UUIDForSlot(EquipmentSlot slot) {
        return UPGRADE_UUIDS_BY_SLOT.get(slot);
    }

    /**
     * Handler generified for forge's and curio's attribute event
     *
     * @param modifiers      item's original attribute map
     * @param upgradeData    upgrade data we're applying
     * @param addCallback    function to add new modifiers to the item
     * @param removeCallback function to remove old modifier from the item
     */
    public static void handleAttributeEvent(List<ItemAttributeModifiers.Entry> modifiers, UpgradeData upgradeData, BiConsumer<Holder<Attribute>, AttributeModifier> addCallback, BiConsumer<Holder<Attribute>, AttributeModifier> removeCallback, String slotId) {
        var upgrades = upgradeData.getUpgrades();
        for (Map.Entry<UpgradeType, Integer> entry : upgrades.entrySet()) {
            UpgradeType upgradeType = entry.getKey();
            int count = entry.getValue();
            double baseAmount = UpgradeUtils.collectAndRemovePreexistingAttribute(modifiers, upgradeType.getAttribute(), upgradeType.getOperation(), removeCallback);
            addCallback.accept(upgradeType.getAttribute(), new AttributeModifier(IronsSpellbooks.id(String.format("%s_upgrade_%s", slotId, upgradeType.getId().getPath())), baseAmount + upgradeType.getAmountPerUpgrade() * count, entry.getKey().getOperation()));
        }
    }

    public static double collectAndRemovePreexistingAttribute(List<ItemAttributeModifiers.Entry> modifiers, Holder<Attribute> key, AttributeModifier.Operation operationToMatch, BiConsumer<Holder<Attribute>, AttributeModifier> removeCallback) {
        //Tactical incision to remove the preexisting attribute but preserve its value
        for (ItemAttributeModifiers.Entry entry : modifiers) {
            if (entry.attribute().equals(key)) {
                if (entry.modifier().operation().equals(operationToMatch)) {
                    removeCallback.accept(key, entry.modifier());
                    return entry.modifier().amount();
                }
            }
        }
        return 0;
    }
}
