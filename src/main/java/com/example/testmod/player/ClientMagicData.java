package com.example.testmod.player;

import com.example.testmod.capabilities.magic.PlayerCooldowns;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.client.Minecraft;

import java.util.HashMap;

public class ClientMagicData {

    /**
     * Current Player's Synced Data
     */
    private static final PlayerMagicData playerMagicData = new PlayerMagicData();

    /**
     * Other Player's Synced Data
     */
    private static final HashMap<Integer, SyncedSpellData> playerSyncedDataLookup = new HashMap<>();
    private static final SyncedSpellData emptySyncedData = new SyncedSpellData(-999);


    public static PlayerCooldowns getCooldowns() {
        return playerMagicData.getPlayerCooldowns();
    }

    public static float getCooldownPercent(SpellType spellType) {
        return playerMagicData.getPlayerCooldowns().getCooldownPercent(spellType);
    }

    public static int getPlayerMana() {
        return playerMagicData.getMana();
    }

    public static void setMana(int playerMana) {
        ClientMagicData.playerMagicData.setMana(playerMana);
    }

    public static CastType getCastType() {
        return ClientMagicData.playerMagicData.getCastType();
    }

    public static int getCastingSpellId() {
        return playerMagicData.getCastingSpellId();
    }

    public static int getCastDurationRemaining() {
        return playerMagicData.getCastDurationRemaining();
    }

    public static int getCastDuration() {
        return playerMagicData.getCastDuration();
    }

    public static boolean isCasting() {
        return playerMagicData.isCasting();
    }

    public static void handleCastDuration() {
        playerMagicData.handleCastDuration();
    }

    public static float getCastCompletionPercent() {
        return playerMagicData.getCastCompletionPercent();
    }

    public static void setClientCastState(int spellId, int spellLevel, int castDuration, CastSource castSource) {
        playerMagicData.initiateCast(spellId, spellLevel, castDuration, castSource);
    }

    public static void resetClientCastState() {
        playerMagicData.resetCastingState();

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem()) {
            Minecraft.getInstance().player.stopUsingItem();
        }
    }

    public static SyncedSpellData getPlayerSyncedData(int serverPlayerId) {
        return playerSyncedDataLookup.getOrDefault(serverPlayerId, emptySyncedData);
    }

    public static void handlePlayerSyncedData(SyncedSpellData playerSyncedData) {
        playerSyncedDataLookup.put(playerSyncedData.getServerPlayerId(), playerSyncedData);
    }
}