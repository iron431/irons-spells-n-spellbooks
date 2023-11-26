package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.api.item.curios.RingData;
import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import io.redspace.ironsspellbooks.datafix.DataFixerHelpers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;

import java.util.List;

public class FixIsbEnhance extends DataFixerElement {
    @Override
    public List<String> preScanValuesToMatch() {
        return List.of(RingData.ISB_ENHANCE);
    }

    @Override
    public boolean runFixer(CompoundTag tag) {
        if (tag != null) {
            if (tag.contains(RingData.ISB_ENHANCE)) {
                var ringTag = tag.get(RingData.ISB_ENHANCE);
                if (ringTag instanceof IntTag legacyRingTag) {
                    tag.remove(RingData.ISB_ENHANCE);
                    tag.putString(RingData.ISB_ENHANCE, DataFixerHelpers.LEGACY_SPELL_MAPPING.getOrDefault(legacyRingTag.getAsInt(), "irons_spellbooks:none"));
                    return true;
                } else if (ringTag instanceof StringTag stringTag) {
                    String newName = DataFixerHelpers.NEW_SPELL_IDS.get(stringTag.getAsString());
                    if (newName != null) {
                        tag.putString(RingData.ISB_ENHANCE, newName);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
