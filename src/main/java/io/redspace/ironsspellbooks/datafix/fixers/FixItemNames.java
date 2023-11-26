package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import net.minecraft.nbt.CompoundTag;

import io.redspace.ironsspellbooks.datafix.DataFixerHelpers;

import java.util.List;

public class FixItemNames extends DataFixerElement {
    @Override
    public List<String> preScanValuesToMatch() {
        return DataFixerHelpers.LEGACY_ITEM_IDS.keySet().stream().toList();
    }

    @Override
    public boolean runFixer(CompoundTag tag) {
        //itemStack.save saves a compound tag with id "id". it can probably be safely assumed that all items are saved like this
        //8 is string tag
        if (tag != null && tag.contains("id", 8)) {
            String itemName = tag.getString("id");
            String newName = DataFixerHelpers.LEGACY_ITEM_IDS.get(itemName);
            if (newName != null) {
                tag.putString("id", newName);
                return true;
            }
        }
        return false;
    }
}
