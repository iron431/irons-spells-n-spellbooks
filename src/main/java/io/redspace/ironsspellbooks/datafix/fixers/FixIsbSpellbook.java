package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.api.spells.LegacySpellBookData;
import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import io.redspace.ironsspellbooks.datafix.DataFixerHelpers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FixIsbSpellbook extends DataFixerElement {
    @Override
    public List<String> preScanValuesToMatch() {
        return List.of(LegacySpellBookData.ISB_SPELLBOOK);
    }

    @Override
    public boolean runFixer(CompoundTag tag) {
        if (tag != null) {
            var spellBookTag = (CompoundTag) tag.get(LegacySpellBookData.ISB_SPELLBOOK);
            if (spellBookTag != null) {
                ListTag listTagSpells = (ListTag) spellBookTag.get(LegacySpellBookData.SPELLS);
                if (listTagSpells != null && !listTagSpells.isEmpty()) {
                    boolean fixed = false;
                    if (((CompoundTag) listTagSpells.get(0)).contains(LegacySpellBookData.LEGACY_ID)) {
                        fixSpellbookData(listTagSpells);
                        fixed = true;
                    }
                    if (fixSpellbookSpellIds(listTagSpells)) {
                        fixed = true;
                    }
                    return fixed;
                }
            }
        }
        return false;
    }

    private void fixSpellbookData(ListTag listTag) {
        listTag.forEach(tag -> {
            CompoundTag t = (CompoundTag) tag;
            int legacySpellId = t.getInt(LegacySpellBookData.LEGACY_ID);
            t.putString(LegacySpellBookData.ID, DataFixerHelpers.LEGACY_SPELL_MAPPING.getOrDefault(legacySpellId, "irons_spellbooks:none"));
            t.remove(LegacySpellBookData.LEGACY_ID);
        });
    }

    private boolean fixSpellbookSpellIds(ListTag listTagSpells) {
        //IronsSpellbooks.LOGGER.debug("fixSpellbookSpellIds: {}", listTagSpells);
        AtomicBoolean fixed = new AtomicBoolean(false);
        listTagSpells.forEach(tag -> {
            CompoundTag spellTag = (CompoundTag) tag;
            if(spellTag.contains(LegacySpellBookData.ID)) {
                String newName = DataFixerHelpers.NEW_SPELL_IDS.get(spellTag.get(LegacySpellBookData.ID).getAsString());
                if (newName != null) {
                    spellTag.putString(LegacySpellBookData.ID, newName);
                    fixed.set(true);
                }
            }
        });
        return fixed.get();
    }
}
