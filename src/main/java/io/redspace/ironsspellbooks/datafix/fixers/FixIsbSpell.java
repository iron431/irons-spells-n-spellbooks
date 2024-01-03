package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.api.spells.LegacySpellData;
import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import io.redspace.ironsspellbooks.datafix.DataFixerHelpers;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

public class FixIsbSpell extends DataFixerElement {
    @Override
    public List<String> preScanValuesToMatch() {
        return List.of(LegacySpellData.ISB_SPELL);
    }

    @Override
    public boolean runFixer(CompoundTag tag) {
        if (tag != null) {
            var spellTag = (CompoundTag) tag.get(LegacySpellData.ISB_SPELL);
            if (spellTag != null) {
                boolean fixed = false;
                if (spellTag.contains(LegacySpellData.LEGACY_SPELL_TYPE)) {
                    fixScrollData(spellTag);
                    fixed = true;
                }
                if (spellTag.contains(LegacySpellData.SPELL_ID)) {
                    String newName = DataFixerHelpers.NEW_SPELL_IDS.get(spellTag.get(LegacySpellData.SPELL_ID).getAsString());
                    if (newName != null) {
                        spellTag.putString(LegacySpellData.SPELL_ID, newName);
                        fixed = true;
                    }
                }
                return fixed;
            }
        }

        return false;
    }

    private void fixScrollData(CompoundTag tag) {
        var legacySpellId = tag.getInt(LegacySpellData.LEGACY_SPELL_TYPE);
        tag.remove(LegacySpellData.LEGACY_SPELL_TYPE);
        tag.putString(LegacySpellData.SPELL_ID, DataFixerHelpers.LEGACY_SPELL_MAPPING.getOrDefault(legacySpellId, "irons_spellbooks:none"));
    }
}
