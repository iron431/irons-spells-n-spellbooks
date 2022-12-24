package com.example.testmod.capabilities.mana.data;

import com.example.testmod.TestMod;
import net.minecraft.nbt.CompoundTag;

public class PlayerMana {

    private int mana;

    public int getMana() {
        return mana;
    }

    public PlayerMana getInstance() {
        return this;
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
        TestMod.LOGGER.info("PlayerMana: saving nbt");
        compound.putInt("mana", mana);
    }

    public void loadNBTData(CompoundTag compound) {
        TestMod.LOGGER.info("PlayerMana: loading nbt");
        mana = compound.getInt("mana");
    }
}
