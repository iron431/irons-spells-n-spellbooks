package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import io.redspace.ironsspellbooks.datafix.DataFixerHelpers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
                    boolean fixed = false;
                    if (((CompoundTag) listTagSpells.get(0)).contains(SpellBookData.LEGACY_ID)) {
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
            int legacySpellId = t.getInt(SpellBookData.LEGACY_ID);
            t.putString(SpellBookData.ID, DataFixerHelpers.LEGACY_SPELL_MAPPING.getOrDefault(legacySpellId, "irons_spellbooks:none"));
            t.remove(SpellBookData.LEGACY_ID);
        });
    }

    private boolean fixSpellbookSpellIds(ListTag listTagSpells) {
        IronsSpellbooks.LOGGER.debug("fixSpellbookSpellIds: {}", listTagSpells);
        AtomicBoolean fixed = new AtomicBoolean(false);
        listTagSpells.forEach(tag -> {
            CompoundTag spellTag = (CompoundTag) tag;
            if(spellTag.contains(SpellBookData.ID)) {
                String newName = DataFixerHelpers.NEW_SPELL_IDS.get(spellTag.get(SpellBookData.ID).getAsString());
                if (newName != null) {
                    spellTag.putString(SpellBookData.ID, newName);
                    fixed.set(true);
                }
            }
        });
        return fixed.get();
    }
}
