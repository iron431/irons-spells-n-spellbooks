//package io.redspace.ironsspellbooks.datafix.fixers;
//
//import io.redspace.ironsspellbooks.api.item.UpgradeData;
//import io.redspace.ironsspellbooks.datafix.DataFixerElement;
//import io.redspace.ironsspellbooks.datafix.DataFixerHelpers;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.nbt.Tag;
//
//import java.util.List;
//
//public class FixUpgradeType extends DataFixerElement {
//    @Override
//    public List<String> preScanValuesToMatch() {
//        return List.of(UpgradeData.Upgrades);
//    }
//
//    @Override
//    public boolean runFixer(CompoundTag tag) {
//        if (tag != null && tag.contains(UpgradeData.Upgrades)) {
//            //IronsSpellbooks.LOGGER.debug("fixUpgradeType: found tag with upgrades {}",tag);
//            ListTag upgrades = tag.getList(UpgradeData.Upgrades, 10);
//            for (Tag t : upgrades) {
//                CompoundTag upgrade = (CompoundTag) t;
//                String upgradeKey = upgrade.getString(UpgradeData.UPGRADE_TYPE);
//                //IronsSpellbooks.LOGGER.debug("fixUpgradeType: {} | needsFixing: {}", upgradeKey, LEGACY_UPGRADE_TYPE_IDS.get(upgradeKey) != null);
//                String newKey = DataFixerHelpers.LEGACY_UPGRADE_TYPE_IDS.get(upgradeKey);
//                if (newKey != null) {
//                    upgrade.putString(UpgradeData.UPGRADE_TYPE, newKey);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//}
