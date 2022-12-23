package com.example.testmod.capabilities.spellbook.data;

import com.example.testmod.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;

public class SpellBookData {
    private final ArrayList<AbstractSpell> transcribedSpells = new ArrayList<>();
    private AbstractSpell activeSpell = null;
    private int spellSlots = 0;

    public AbstractSpell getActiveSpell() {
        return activeSpell;
    }

    public boolean setActiveSpell(AbstractSpell spell) {

        var index = transcribedSpells.indexOf(spell);

        if (index > -1) {
            this.activeSpell = transcribedSpells.get(index);
            return true;
        }
        return false;
    }

    public int getSpellSlots() {
        return spellSlots;
    }

    public void setSpellSlots(int numSlots) {
        this.spellSlots = numSlots;
    }

    public boolean addSpell(AbstractSpell spell) {
        if (transcribedSpells.size() < spellSlots) {
            transcribedSpells.add(spell);

            if (this.transcribedSpells.size() == 1) {
                setActiveSpell(spell);
            }

            return true;
        }
        return false;
    }

    public boolean replaceSpell(AbstractSpell oldSpell, AbstractSpell newSpell) {
        if (transcribedSpells.remove(oldSpell)) {
            transcribedSpells.add(newSpell);
            return true;
        }
        return false;
    }

    public boolean removeSpell(AbstractSpell spell) {
        return transcribedSpells.remove(spell);
    }

    public void saveNBTData(CompoundTag compound) {
        compound.putInt("spellSlots", spellSlots);

        ListTag listTagSpells = new ListTag();

        transcribedSpells.forEach(spell -> {
            CompoundTag ct = new CompoundTag();
            ct.putInt("id", spell.getID());
            ct.putInt("level", spell.getLevel());
            listTagSpells.add(ct);
        });
        compound.put("spells", listTagSpells);

        if (this.activeSpell == null) {
            compound.putInt("activeSpellId", -1);
        } else {
            compound.putInt("activeSpellId", this.activeSpell.getID());
        }
    }

    public void loadNBTData(CompoundTag compound) {
        spellSlots = compound.getInt("spellSlots");
        int activeSpellId = compound.getInt("activeSpellId");

        ListTag listTagSpells = (ListTag) compound.get("spells");
        if (listTagSpells != null) {
            listTagSpells.forEach(tag -> {
                CompoundTag t = (CompoundTag) tag;
                int id = t.getInt("id");
                int level = t.getInt("level");

                AbstractSpell s = AbstractSpell.getSpell(id, level);

                if (s != null) {
                    transcribedSpells.add(s);
                    if (activeSpellId == s.getID()) {
                        setActiveSpell(s);
                    }
                }
            });
        }
    }
}
