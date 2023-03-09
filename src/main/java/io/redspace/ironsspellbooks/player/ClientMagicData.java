package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerCooldowns;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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

    /**
     * Animation Data
     */

    public static KeyframeAnimationPlayer castingAnimationPlayer;


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
        IronsSpellbooks.LOGGER.debug("ClientMagicData.resetClientCastState");
        playerMagicData.resetCastingState();

        if (castingAnimationPlayer != null) {
            castingAnimationPlayer.stop();
        }

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem()) {
            Minecraft.getInstance().player.stopUsingItem();
        }
    }

    public static SyncedSpellData getSyncedSpellData(LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            return playerSyncedDataLookup.getOrDefault(livingEntity.getId(), emptySyncedData);
        }
        if (livingEntity instanceof AbstractSpellCastingMob abstractSpellCastingMob) {
            return abstractSpellCastingMob.getPlayerMagicData().getSyncedData();
        }
        return new SyncedSpellData(null);

    }

    public static void handlePlayerSyncedData(SyncedSpellData playerSyncedData) {
        playerSyncedDataLookup.put(playerSyncedData.getServerPlayerId(), playerSyncedData);
    }
}