package com.example.testmod.capabilities.magic.data;

import com.example.testmod.TestMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class PlayerMagicData {
    public static final String MANA = "mana";
    public static final String COOLDOWNS = "cooldowns";

    private int mana;
    private final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void addMana(int mana) {
        this.mana += mana;
    }

    public PlayerCooldowns getPlayerCooldowns(){
        return this.playerCooldowns;
    }

    public void saveNBTData(CompoundTag compound) {
        TestMod.LOGGER.info("PlayerMagicData: saving nbt");
        compound.putInt(MANA, mana);

        if (playerCooldowns.hasCooldownsActive()) {
            ListTag listTag = new ListTag();
            playerCooldowns.saveNBTData(listTag);
            if (!listTag.isEmpty()) {
                compound.put(COOLDOWNS, listTag);
            }
        }
    }

    public void loadNBTData(CompoundTag compound) {
        //TODO: I don't think we need to but there is a chance we need to create a new PlayerCooldowns here.

        TestMod.LOGGER.info("PlayerMagicData: loading nbt");
        mana = compound.getInt(MANA);

        ListTag listTag = (ListTag) compound.get(COOLDOWNS);

        if (!listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

    }
}
