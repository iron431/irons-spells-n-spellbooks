package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.ClientSpellTargetingData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerCooldowns;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClientMagicData {

    /**
     * Current Player's Synced Data
     */
//    private static final MagicData playerMagicData = new MagicData();
    private static final Set<UUID> activeSummons = new HashSet<>();

    /**
     * Other Player's Synced Data
     */
//    private static final HashMap<Integer, SyncedSpellData> playerSyncedDataLookup = new HashMap<>();
//    private static final SyncedSpellData emptySyncedData = new SyncedSpellData(-999);

    /**
     * Spell Selections
     */
    static SpellSelectionManager spellSelectionManager;

    public static SpellSelectionManager getSpellSelectionManager() {
        if (spellSelectionManager == null) {
            var player = MinecraftInstanceHelper.getPlayer();
            if (player != null) {
                spellSelectionManager = new SpellSelectionManager(player);
            }

        }

        return spellSelectionManager;
    }

    public static void updateSpellSelectionManager(@NotNull ServerPlayer player) {
        spellSelectionManager = new SpellSelectionManager(player);
    }

    public static void updateSpellSelectionManager() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            spellSelectionManager = new SpellSelectionManager(Minecraft.getInstance().player);
        }
    }

    /**
     * Local Targeting data
     */
    private static ClientSpellTargetingData spellTargetingData;

    public static void setTargetingData(ClientSpellTargetingData spellTargetingData) {
        ClientMagicData.spellTargetingData = spellTargetingData;
    }

    public static ClientSpellTargetingData getTargetingData() {
        if (spellTargetingData == null)
            setTargetingData(new ClientSpellTargetingData());
        return spellTargetingData;
    }

    public static void resetTargetingData() {
        spellTargetingData = null;
    }

    @Nullable
    private static MagicData getLocalMagicData() {
        Player player = MinecraftInstanceHelper.getPlayer();
        if (player == null) {
            return null;
        }
        return MagicData.getPlayerMagicData(player);
    }

    public static PlayerCooldowns getCooldowns() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? magicData.getPlayerCooldowns() : new PlayerCooldowns();
    }

    public static PlayerRecasts getRecasts() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? magicData.getPlayerRecasts() : new PlayerRecasts();
    }

    public static void cacheClientSummons() {
        MagicData magicData = getLocalMagicData();
        if (magicData == null) {
            return;
        }
        var recasts = magicData.getPlayerRecasts();
        activeSummons.clear();
        recasts.getActiveRecasts().forEach(instance -> {
            if (instance.getCastData() instanceof SummonedEntitiesCastData summonedEntitiesCastData) {
                activeSummons.addAll(summonedEntitiesCastData.getSummons());
            }
        });
    }

    public static void setRecasts(PlayerRecasts playerRecasts) {
        MagicData magicData = getLocalMagicData();
        if (magicData == null) {
            return;
        }
        magicData.setPlayerRecasts(playerRecasts);
        cacheClientSummons();
    }

    public static Set<UUID> getActiveSummons() {
        return activeSummons;
    }

    public static float getCooldownPercent(AbstractSpell spell) {
        return getCooldowns().getCooldownPercent(spell);
    }

    public static int getPlayerMana() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? (int) magicData.getMana() : 0;
    }

    public static void setMana(int playerMana) {
        MagicData magicData = getLocalMagicData();
        if (magicData != null) {
            magicData.setMana(playerMana);
        }
    }

    public static CastType getCastType() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? magicData.getCastType() : CastType.NONE;
    }

    public static String getCastingSpellId() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? magicData.getCastingSpellId() : SpellRegistry.none().getSpellId();
    }

    public static int getCastingSpellLevel() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? magicData.getCastingSpellLevel() : 0;
    }

    public static int getCastDurationRemaining() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? magicData.getCastDurationRemaining() : 0;
    }

    public static int getCastDuration() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? magicData.getCastDuration() : 0;
    }

    public static boolean isCasting() {
        MagicData magicData = getLocalMagicData();
        return magicData != null && magicData.isCasting();
    }

    public static void handleCastDuration() {
        MagicData magicData = getLocalMagicData();
        if (magicData != null) {
            magicData.handleCastDuration();
        }
    }

    public static float getCastCompletionPercent() {
        MagicData magicData = getLocalMagicData();
        return magicData != null ? magicData.getCastCompletionPercent() : 0f;
    }

    public static void setClientCastState(String spellId, int spellLevel, int castDuration, CastSource castSource, String castingEquipmentSlot) {
        MagicData magicData = getLocalMagicData();
        if (magicData != null) {
            magicData.initiateCast(SpellRegistry.getSpell(spellId), spellLevel, castDuration, castSource, castingEquipmentSlot);
        }
    }

    public static void resetClientCastState(UUID playerUUID) {
        //Ironsspellbooks.logger.debug("resetClientCastState.1: instanceUUID:{}, playerUUID:{}", Minecraft.getInstance().player.getUUID(), playerUUID);

        if (Minecraft.getInstance().player.getUUID().equals(playerUUID)) {
            //Ironsspellbooks.logger.debug("resetClientCastState.1.1");
            MagicData magicData = getLocalMagicData();
            if (magicData != null) {
                magicData.resetCastingState();
            }
            resetTargetingData();
        }

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem() && Minecraft.getInstance().player.getUUID().equals(playerUUID)) {
            //Ironsspellbooks.logger.debug("resetClientCastState.2: instanceUUID:{}, playerUUID:{}", Minecraft.getInstance().player.getUUID(), playerUUID);
            Minecraft.getInstance().player.stopUsingItem();
        }
    }

//    public static SyncedSpellData getSyncedSpellData(LivingEntity livingEntity) {
//        if (livingEntity instanceof Player) {
//            return playerSyncedDataLookup.getOrDefault(livingEntity.getId(), emptySyncedData);
//        }
//        if (livingEntity instanceof IMagicEntity abstractSpellCastingMob) {
//            return abstractSpellCastingMob.getMagicData().getSyncedData();
//        }
//        return new SyncedSpellData(null);
//
//    }

//    public static void handlePlayerSyncedData(SyncedSpellData playerSyncedData) {
//        if (Log.SPELL_SELECTION) {
//            IronsSpellbooks.LOGGER.debug("ClientMagicData.handlePlayerSyncedData {}", playerSyncedData.getSpellSelection());
//        }
//        playerSyncedDataLookup.put(playerSyncedData.getServerPlayerId(), playerSyncedData);
//    }

//    public static void handleAbstractCastingMobSyncedData(int entityId, SyncedSpellData syncedSpellData) {
//        var level = Minecraft.getInstance().level;
//
//        if (Log.SPELL_DEBUG) {
//            IronsSpellbooks.LOGGER.debug("handleAbstractCastingMobSyncedData {}, {}, {}", level, entityId, syncedSpellData);
//        }
//
//        if (level == null) {
//            return;
//        }
//
//        var entity = level.getEntity(entityId);
//        if (entity instanceof IMagicEntity abstractSpellCastingMob) {
//            abstractSpellCastingMob.setSyncedSpellData(syncedSpellData);
//        }
//    }
}