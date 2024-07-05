package io.redspace.ironsspellbooks.datafix.fixers;

import io.redspace.ironsspellbooks.capabilities.magic.SpellContainer;
import io.redspace.ironsspellbooks.datafix.DataFixerElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FixSpellbookSlots extends DataFixerElement {
    @Override
    public List<String> preScanValuesToMatch() {
        return List.of(
                "irons_spellbooks:netherite_spell_book",
                "irons_spellbooks:diamond_spell_book",
                "irons_spellbooks:gold_spell_book",
                "irons_spellbooks:iron_spell_book",
                "irons_spellbooks:copper_spell_book",
                "irons_spellbooks:rotten_spell_book",
                "irons_spellbooks:blaze_spell_book",
                "irons_spellbooks:dragonskin_spell_book",
                "irons_spellbooks:druidic_spell_book",
                "irons_spellbooks:villager_spell_book");
    }

    @Override
    public boolean runFixer(CompoundTag tag) {
//        if (tag != null && tag.contains("id", Tag.TAG_STRING)) {
//            AtomicBoolean fixed = new AtomicBoolean(false);
//            preScanValuesToMatch().forEach(v -> {
//                if (tag.getString("id").equals(v)) {
//                    var itemTag = tag.getCompound("tag");
//                    if (itemTag.contains(SpellContainer.SPELL_SLOT_CONTAINER)) {
//                        var spellBookTag = itemTag.getCompound(SpellContainer.SPELL_SLOT_CONTAINER);
//                        switch (v) {
//                            case "irons_spellbooks:netherite_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 12);
//                            case "irons_spellbooks:diamond_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 10);
//                            case "irons_spellbooks:gold_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 8);
//                            case "irons_spellbooks:iron_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 6);
//                            case "irons_spellbooks:copper_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 5);
//                            case "irons_spellbooks:rotten_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 8);
//                            case "irons_spellbooks:blaze_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 10);
//                            case "irons_spellbooks:dragonskin_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 12);
//                            case "irons_spellbooks:druidic_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 10);
//                            case "irons_spellbooks:villager_spell_book" -> spellBookTag.putInt(SpellContainer.MAX_SLOTS, 10);
//                        }
//                        fixed.set(true);
//                    }
//                }
//            });
//            return fixed.get();
//        }
        return false;
    }
}

