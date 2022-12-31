package com.example.testmod.capabilities.magic.data;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.network.PacketCastingState;
import com.example.testmod.capabilities.magic.network.PacketSyncMagicDataToClient;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;

import static com.example.testmod.registries.AttributeRegistry.MAX_MANA;

public class MagicManager extends SavedData {

    public static final String MAGIC_MANAGER = "magicManager";
    public static final int TICKS_PER_CYCLE = 20;
    private int counter = 0;
    private static MagicManager magicManager = null;

    @Nonnull
    public static MagicManager get(Level level) {

        if (level.isClientSide) {
            throw new RuntimeException("Don't access the ManaManager client-side!");
        }

        if (magicManager == null) {
            DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
            magicManager = storage.computeIfAbsent(MagicManager::new, MagicManager::new, MAGIC_MANAGER);
        }
        return magicManager;
    }

    public PlayerMagicData getPlayerMagicData(ServerPlayer serverPlayer) {
        var capContainer = serverPlayer.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
        if (capContainer.isPresent()) {
            return capContainer.resolve().orElse(new PlayerMagicData());
        }
        return new PlayerMagicData();
    }

    public int getPlayerCurrentMana(ServerPlayer serverPlayer) {
        return getPlayerMagicData(serverPlayer).getMana();
    }

    public void setPlayerCurrentMana(ServerPlayer serverPlayer, int newManaValue) {
        var playerMagicData = getPlayerMagicData(serverPlayer);
        playerMagicData.setMana(newManaValue);
    }

    public void regenPlayerMana(ServerPlayer serverPlayer, PlayerMagicData playerMagicData) {
        int playerMaxMana = (int) serverPlayer.getAttributeValue(MAX_MANA.get());
        int increment = Math.round(Math.max(playerMaxMana * .01f, 1));

        if (playerMagicData.getMana() != playerMaxMana) {
            if (playerMagicData.getMana() + increment < playerMaxMana) {
                playerMagicData.addMana(increment);
            } else {
                playerMagicData.setMana(playerMaxMana);
            }
        }
    }

    public void tick(Level level) {
        counter--;

        level.players().forEach(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerMagicData playerMagicData = getPlayerMagicData(serverPlayer);

                playerMagicData.getPlayerCooldowns().tick(1);

                if (playerMagicData.isCasting()) {
                    playerMagicData.handleCastDuration();
                    if (!playerMagicData.isCasting()) {
                        TestMod.LOGGER.info("MagicManager.tick: handle spell casting complete");
                        Messages.sendToPlayer(new PacketCastingState(playerMagicData.getCastingSpellId(), 0, true), serverPlayer);
                        var spell = AbstractSpell.getSpell(playerMagicData.getCastingSpellId(), playerMagicData.getCastingSpellLevel());
                        spell.finishCasting(serverPlayer.level, serverPlayer, this, playerMagicData);
                        playerMagicData.resetCastingState();
                    }
                }

                if (counter <= 0) {
                    counter = TICKS_PER_CYCLE;
                    regenPlayerMana(serverPlayer, playerMagicData);
                    Messages.sendToPlayer(new PacketSyncMagicDataToClient(playerMagicData), serverPlayer);
                }
            }
        });
    }

    public MagicManager() {
    }

    public MagicManager(CompoundTag tag) {

    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }
}
