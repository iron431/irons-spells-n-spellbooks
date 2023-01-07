package com.example.testmod.capabilities.magic;

import com.example.testmod.TestMod;
import com.example.testmod.entity.AbstractConeProjectile;
import com.example.testmod.player.ClientMagicData;
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

    public ClientMagicData.SpinAttackType spinAttackType;

    private boolean isCasting = false;
    private int castingSpellId = 0;
    private int castingSpellLevel = 0;
    private int castDurationRemaining = 0;
    private boolean fromScroll = false;

    public AbstractConeProjectile cone;
    private int castDuration = 0;
    private ItemStack castingItemStack = ItemStack.EMPTY;

    public void resetCastingState() {
        this.isCasting = false;
        this.castingSpellId = 0;
        this.castingSpellLevel = 0;
        this.castDurationRemaining = 0;
        this.discardCone();
    }

    public void initiateCast(int castingSpellId, int castingSpellLevel, int castDuration) {
        this.isCasting = true;
        this.castingSpellId = castingSpellId;
        this.castingSpellLevel = castingSpellLevel;
        this.castDuration = castDuration;
        this.castDurationRemaining = castDuration;
    }
    public boolean discardCone(){
        if (this.cone != null) {
            this.cone.discard();
            this.cone = null;
            TestMod.LOGGER.debug("PlayerMagicData: discarding cone");
            return true;
        }
        return false;
    }

    public void initiateCast(int castingSpellId, int castingSpellLevel, int castDuration, boolean fromScroll) {
        this.fromScroll = fromScroll;
        initiateCast(castingSpellId, castingSpellLevel, castDuration);
    }

    public boolean isCasting() {
        return isCasting;
    }

    public int getCastingSpellId() {
        return castingSpellId;
    }

    public int getCastingSpellLevel() {
        return castingSpellLevel;
    }

    public int getCastDurationRemaining() {
        return castDurationRemaining;
    }

    public void handleCastDuration() {
        castDurationRemaining--;

        if (castDurationRemaining <= 0) {
            isCasting = false;
            castDurationRemaining = 0;
        }
    }

    public void setPlayerCastingItem(ItemStack itemStack) {
        this.castingItemStack = itemStack;
    }

    public ItemStack getPlayerCastingItem() {
        return this.castingItemStack;
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
        TestMod.LOGGER.debug("PlayerMagicData: saving nbt");
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
        TestMod.LOGGER.debug("PlayerMagicData: loading nbt");
        mana = compound.getInt(MANA);
        ListTag listTag = (ListTag) compound.get(COOLDOWNS);

        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

    }
}
