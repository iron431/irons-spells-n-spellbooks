package com.example.testmod.capabilities.spellbook.data;

import com.example.testmod.TestMod;
import com.example.testmod.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;

public class SpellBookData {
    private final ArrayList<AbstractSpell> transcribedSpells = new ArrayList<>();
    private AbstractSpell activeSpell = null;
    private int spellSlots = 0;
    private boolean dirty = true;
    private CompoundTag tag = new CompoundTag();

    public AbstractSpell getActiveSpell() {
        return activeSpell;
    }

    public boolean setActiveSpell(AbstractSpell spell) {

        var index = transcribedSpells.indexOf(spell);

        if (index > -1) {
            this.activeSpell = transcribedSpells.get(index);
            setDirty(true);
            return true;
        }
        return false;
    }

    public boolean isDirty() {
        return dirty;
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getSpellSlots() {
        return spellSlots;
    }

    public void setSpellSlots(int numSlots) {
        setDirty(true);
        this.spellSlots = numSlots;
    }

    public boolean addSpell(AbstractSpell spell) {
        if (transcribedSpells.size() < spellSlots) {
            transcribedSpells.add(spell);

            if (this.transcribedSpells.size() == 1) {
                setActiveSpell(spell);
            }
            setDirty(true);
            return true;
        }
        return false;
    }

    public boolean replaceSpell(AbstractSpell oldSpell, AbstractSpell newSpell) {
        if (transcribedSpells.remove(oldSpell)) {
            transcribedSpells.add(newSpell);
            setDirty(true);
            return true;
        }
        return false;
    }

    public boolean removeSpell(AbstractSpell spell) {
        if (transcribedSpells.remove(spell)) {
            setDirty(true);
            return true;
        }
        return false;
    }

    public CompoundTag saveNBTData() {
        if (!dirty) {
            return this.tag;
        }

        ListTag listTagSpells = new ListTag();
        CompoundTag compound = new CompoundTag();
        compound.putInt("spellSlots", spellSlots);

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
        this.tag = compound;
        setDirty(false);
        return (this.tag);
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
                transcribedSpells.add(s);
                if (activeSpellId == s.getID()) {
                    setActiveSpell(s);
                }
            });
        }
    }
}
