package com.example.testmod.capabilities.magic.data;

import com.example.testmod.TestMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class PlayerMagicData {
    public static final String MANA = "mana";
    public static final String COOLDOWNS = "cooldowns";

    //MANA
    private int mana;

    //COOLDOWNS
    private final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    //CASTING
    private boolean isCasting = false;
    private int castDurationRemaining = 0;
    private int castDuration = 0;

    //CASTING
    public boolean isCasting() {
        return isCasting;
    }

    public void setCasting(boolean casting) {
        isCasting = casting;
    }

    public int getCastDurationRemaining() {
        return castDurationRemaining;
    }

    public void setCastDurationRemaining(int castDurationRemaining) {
        this.castDurationRemaining = castDurationRemaining;
    }

    public int getCastDuration() {
        return castDuration;
    }

    public void setCastDuration(int castDuration) {
        this.castDuration = castDuration;
    }

    public void decrementCastDuration() {
        castDurationRemaining--;
    }

    public void handleCastDuration() {
        castDurationRemaining--;

        if (castDurationRemaining <= 0) {
            isCasting = false;
            castDurationRemaining = 0;
        }
    }


    //MANA
    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void addMana(int mana) {
        this.mana += mana;
    }


    //COOLDOWNS
    public PlayerCooldowns getPlayerCooldowns() {
        return this.playerCooldowns;
    }

    //SYSTEM
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
        TestMod.LOGGER.info("PlayerMagicData: loading nbt");
        mana = compound.getInt(MANA);
        ListTag listTag = (ListTag) compound.get(COOLDOWNS);

        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

    }
}
