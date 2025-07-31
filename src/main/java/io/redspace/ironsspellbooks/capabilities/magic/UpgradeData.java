package io.redspace.ironsspellbooks.capabilities.magic;

import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.item.armor.UpgradeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class UpgradeData {
    public static final String Upgrades = "ISBUpgrades";
    public static final String Upgrade_Key = "id";
    public static final String Slot_Key = "slot";
    public static final String Upgrade_Count = "upgrades";
    public static final UpgradeData NONE = new UpgradeData(Map.of(), EquipmentSlot.MAINHAND.getName());

    private final Map<UpgradeType, Integer> upgrades;
    private String/*EquipmentSlot*/ upgradedSlot;

    protected UpgradeData(Map<UpgradeType, Integer> upgrades, /*EquipmentSlot*/ String slot) {
        this.upgrades = upgrades;
        this.upgradedSlot = slot;
    }

    public static UpgradeData getUpgradeData(ItemStack itemStack) {
        if (!UpgradeData.hasUpgradeData(itemStack))
            return NONE;
        ListTag upgrades = itemStack.getOrCreateTag().getList(Upgrades, 10);
        //String attributeName = Registry.ATTRIBUTE.getKey(attribute).toString();
        Map<UpgradeType, Integer> map = new HashMap<>();
        String upgradedSlot = null;
        for (Tag tag : upgrades) {
            if (tag instanceof CompoundTag compoundTag) {
                if (upgradedSlot == null) {
                    upgradedSlot = compoundTag.getString(Slot_Key);//EquipmentSlot.byName(compoundTag.getString(Slot_Key));
                }
                var upgradeKey = ResourceLocation.parse(compoundTag.getString(Upgrade_Key));
                UpgradeType.getUpgrade(upgradeKey).ifPresent((upgrade) -> map.put(upgrade, compoundTag.getInt(Upgrade_Count)));
            }
            //Optional<Attribute> optional = Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(attributeName));
            //optional.ifPresent((atr) -> attributes.put(atr, compoundTag.getInt(Upgrade_Count)));

        }
        return new UpgradeData(map, upgradedSlot);
    }

    public static boolean hasUpgradeData(ItemStack itemStack) {
        return itemStack.getTag() != null && itemStack.getTag().contains(Upgrades);
    }

    public static void setUpgradeData(ItemStack itemStack, UpgradeData upgradeData) {
        if (upgradeData == NONE) {
            if (UpgradeData.hasUpgradeData(itemStack)) {
                itemStack.removeTagKey(Upgrades);
            }
            return;
        }
        ListTag upgrades = new ListTag();

        for (ImmutableMap.Entry<UpgradeType, Integer> upgradeInstance : upgradeData.upgrades.entrySet()) {
            CompoundTag upgradeTag = new CompoundTag();
            upgradeTag.putString(Upgrade_Key, upgradeInstance.getKey().getId().toString());
            upgradeTag.putString(Slot_Key, upgradeData.upgradedSlot/*.getName()*/);
            upgradeTag.putInt(Upgrade_Count, upgradeInstance.getValue());
            upgrades.add(upgradeTag);
        }

        itemStack.addTagElement(Upgrades, upgrades);
    }

    public static void removeUpgradeData(ItemStack itemStack) {
        setUpgradeData(itemStack, NONE);
    }

    public UpgradeData addUpgrade(ItemStack stack, UpgradeType upgradeType, String slot) {
        if (this == NONE) {
            Map<UpgradeType, Integer> map = new HashMap<>();
            map.put(upgradeType, 1);
            var upgrade = new UpgradeData(map, slot);
            UpgradeData.setUpgradeData(stack, upgrade);
            return upgrade;
        } else {
            if (this.upgrades.containsKey(upgradeType)) {
                this.upgrades.put(upgradeType, this.upgrades.get(upgradeType) + 1);
            } else {
                this.upgrades.put(upgradeType, 1);
            }
            UpgradeData.setUpgradeData(stack, this);
            return this;
        }
    }

    public int getCount() {
        int count = 0;
        for (ImmutableMap.Entry<UpgradeType, Integer> upgradeInstance : this.upgrades.entrySet()) {
            count += upgradeInstance.getValue();
        }
        return count;
    }

    public String/*EquipmentSlot*/ getUpgradedSlot() {
        return this.upgradedSlot;
    }

    public Map<UpgradeType, Integer> getUpgrades() {
        return this.upgrades;
    }
}
