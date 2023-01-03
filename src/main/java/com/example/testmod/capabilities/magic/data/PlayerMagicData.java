package com.example.testmod.capabilities.magic.data;

import com.example.testmod.TestMod;
import com.example.testmod.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PlayerMagicData {
    public static final String MANA = "mana";
    public static final String COOLDOWNS = "cooldowns";

    /********* MANA *******************************************************/

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

    /********* CASTING *******************************************************/

    private boolean isCasting = false;
    private int castingSpellId = 0;
    private int castingSpellLevel = 0;
    private int castDurationRemaining = 0;
    private int castDuration = 0;
    private ItemStack castingItemStack = ItemStack.EMPTY;

    public void resetCastingState() {
        isCasting = false;
        castingSpellId = 0;
        castingSpellLevel = 0;
        castDurationRemaining = 0;
        castDuration = 0;
    }

    public void initiateCast(int castingSpellId, int castingSpellLevel, int castDuration) {
        isCasting = true;
        this.castingSpellId = castingSpellId;
        this.castingSpellLevel = castingSpellLevel;
        this.castDuration = castDuration;
        this.castDurationRemaining = castDuration;
    }

    public boolean isCasting() {
        return isCasting;
    }

    public void setCasting(boolean casting) {
        isCasting = casting;
    }

    public void setCastingSpellId(int spellId) {
        castingSpellId = spellId;
    }

    public int getCastingSpellId() {
        return castingSpellId;
    }

    public void setCastingSpellLevel(int spellLevel) {
        castingSpellLevel = spellLevel;
    }

    public int getCastingSpellLevel() {
        return castingSpellLevel;
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

    public void setPlayerCastingItem(ItemStack itemStack) {
        this.castingItemStack = itemStack;
    }
    public ItemStack getPlayerCastingItem() {
        return this.castingItemStack;
    }

    public void handleCastDuration() {
        castDurationRemaining--;

        if (castDurationRemaining <= 0) {
            isCasting = false;
            castDurationRemaining = 0;
        }
    }

    /********* COOLDOWNS *******************************************************/

    private final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    public PlayerCooldowns getPlayerCooldowns() {
        return this.playerCooldowns;
    }

    /********* SYSTEM *******************************************************/

    public static PlayerMagicData getPlayerMagicData(ServerPlayer serverPlayer) {
        var capContainer = serverPlayer.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
        if (capContainer.isPresent()) {
            return capContainer.resolve().orElse(new PlayerMagicData());
        }
        return new PlayerMagicData();
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
        TestMod.LOGGER.info("PlayerMagicData: loading nbt");
        mana = compound.getInt(MANA);
        ListTag listTag = (ListTag) compound.get(COOLDOWNS);

        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

    }
}
