package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import io.redspace.ironsspellbooks.datafix.DataFixerHelpers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;

public class FixIsbSpellbook extends DataFixerElement {
    @Override
    public List<String> preScanValuesToMatch() {
        return List.of(SpellBookData.ISB_SPELLBOOK);
    }

    @Override
    public boolean runFixer(CompoundTag tag) {
        if (tag != null) {
            var spellBookTag = (CompoundTag) tag.get(SpellBookData.ISB_SPELLBOOK);
            if (spellBookTag != null) {
                ListTag listTagSpells = (ListTag) spellBookTag.get(SpellBookData.SPELLS);
                if (listTagSpells != null && !listTagSpells.isEmpty()) {
                    if (((CompoundTag) listTagSpells.get(0)).contains(SpellBookData.LEGACY_ID)) {
                        fixSpellbookData(listTagSpells);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void fixSpellbookData(ListTag listTag) {
        listTag.forEach(tag -> {
            CompoundTag t = (CompoundTag) tag;
            int legacySpellId = t.getInt(SpellBookData.LEGACY_ID);
            t.putString(SpellBookData.ID, DataFixerHelpers.LEGACY_SPELL_MAPPING.getOrDefault(legacySpellId, "irons_spellbooks:none"));
            t.remove(SpellBookData.LEGACY_ID);
        });
    }
}
