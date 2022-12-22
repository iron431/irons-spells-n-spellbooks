package com.example.testmod.capabilities.mana.data;

import net.minecraft.nbt.CompoundTag;

public class PlayerMana {

    private int mana;

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void addMana(int mana) {
        this.mana += mana;
    }

    public void copyFrom(PlayerMana source) {
        mana = source.mana;
    }


    public void saveNBTData(CompoundTag compound) {
        compound.putInt("mana", mana);
    }

    public void loadNBTData(CompoundTag compound) {
        mana = compound.getInt("mana");
    }
}
