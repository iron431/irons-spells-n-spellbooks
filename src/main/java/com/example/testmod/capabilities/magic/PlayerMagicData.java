package com.example.testmod.capabilities.magic;

import com.example.testmod.TestMod;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PlayerMagicData extends AbstractMagicData {

    public PlayerMagicData() {
    }

    public PlayerMagicData(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    public void setServerPlayer(ServerPlayer serverPlayer) {
        if (this.serverPlayer == null) {
            this.serverPlayer = serverPlayer;
        }
    }

    private ServerPlayer serverPlayer = null;
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

    /********* SYNC DATA *******************************************************/

    private SyncedSpellData playerSyncedData;

    public SyncedSpellData getSyncedData() {
        if (playerSyncedData == null) {
            playerSyncedData = new SyncedSpellData(serverPlayer);
        }

        return playerSyncedData;
    }

    /********* CASTING *******************************************************/

    private boolean isCasting = false;
    private int castingSpellId = 0;
    private int castingSpellLevel = 0;
    private int castDurationRemaining = 0;
    private CastSource castSource;
    private CastData additionalCastData;

    private ItemStack castingItemStack = ItemStack.EMPTY;


    public void resetCastingState() {
        this.isCasting = false;
        this.castingSpellId = 0;
        this.castingSpellLevel = 0;
        this.castDurationRemaining = 0;
        resetAdditionCastData();
    }

    public void initiateCast(int castingSpellId, int castingSpellLevel, int castDuration, CastSource castSource) {
        this.castSource = castSource;
        this.castingSpellId = castingSpellId;
        this.castingSpellLevel = castingSpellLevel;
        this.castDurationRemaining = castDuration;
        this.isCasting = true;
    }

    public CastData getAdditionalCastData() {
        return additionalCastData;
    }

    public void setAdditionalCastData(CastData newCastData) {
        additionalCastData = newCastData;
    }

    public void resetAdditionCastData() {
        if (additionalCastData != null) {
            additionalCastData.reset();
            additionalCastData = null;
        }
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

    public CastSource getCastSource() {
        return castSource;
    }

    public int getCastDurationRemaining() {
        return castDurationRemaining;
    }

    public void handleCastDuration() {
        castDurationRemaining--;
        //TestMod.LOGGER.debug("PlayerMagicData: isUsingItem: {} ", serverPlayer.isUsingItem());

        if (castDurationRemaining <= 0) {
//            TestMod.LOGGER.debug("PlayerMagicData: ready to cast");
//            TestMod.LOGGER.debug("PlayerMagicData: cast type: {} ", SpellType.getTypeFromValue(castingSpellId).getCastType());
//            TestMod.LOGGER.debug("PlayerMagicData: isUsingItem: {} ", serverPlayer.isUsingItem());

            //Wait for charge cast to release
            if (SpellType.getTypeFromValue(castingSpellId).getCastType() == CastType.CHARGE) {
                if (serverPlayer != null && serverPlayer.isUsingItem())
                    return;
            }

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
            var opt = capContainer.resolve();
            if (opt.isEmpty()) {
                return new PlayerMagicData(serverPlayer);
            }

            var pmd = opt.get();
            pmd.setServerPlayer(serverPlayer);
            return pmd;
        }
        return new PlayerMagicData(serverPlayer);
    }

    public void saveNBTData(CompoundTag compound) {
        //TestMod.LOGGER.debug("PlayerMagicData: saving nbt");
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
        //TestMod.LOGGER.debug("PlayerMagicData: loading nbt");
        mana = compound.getInt(MANA);
        ListTag listTag = (ListTag) compound.get(COOLDOWNS);

        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

    }
}
