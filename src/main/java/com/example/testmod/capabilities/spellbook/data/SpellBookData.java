package com.example.testmod.capabilities.spellbook.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.EnchantedBookItem;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SpellBookData {
    private final ArrayList<Integer> transcribedSpells = new ArrayList<>();
    private int activeSpellId = -1;
    private int spellSlots = 0;

    public int getActiveSpellId() {
        return activeSpellId;
    }

    public boolean setActiveSpellId(int spellId) {
        if (transcribedSpells.contains(spellId)) {
            this.activeSpellId = spellId;
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

    public boolean addSpell(int spellId) {
        if (transcribedSpells.size() < spellSlots) {
            transcribedSpells.add(spellId);
            return true;
        }
        return false;
    }

    public boolean replaceSpell(int oldSpellId, int newSpellId) {
        if (transcribedSpells.remove((Object) oldSpellId)) {
            transcribedSpells.add(newSpellId);
            return true;
        }
        return false;
    }

    public boolean removeSpell(int spellId) {
        if (transcribedSpells.remove((Object) spellId)) {
            return true;
        }
        return false;
    }

    public void saveNBTData(CompoundTag compound) {
        compound.putInt("spellSlots", spellSlots);
        compound.putInt("activeSpellId", activeSpellId);
        compound.putString("transcribedSpells", transcribedSpells.stream().map(Object::toString).collect(Collectors.joining(",")));
        //compound.getList()
    }

    public void loadNBTData(CompoundTag compound) {
        spellSlots = compound.getInt("spellSlots");
        activeSpellId = compound.getInt("activeSpellId");

        var tmpSpellList = compound.getString("transcribedSpells");

        if (!tmpSpellList.isEmpty()) {
            for (String s : tmpSpellList.split(",")) {
                transcribedSpells.add(Integer.parseInt(s));
            }
        }
    }
}
