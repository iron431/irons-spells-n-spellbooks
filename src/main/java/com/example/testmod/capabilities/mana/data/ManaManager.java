package com.example.testmod.capabilities.mana.data;

import com.example.testmod.capabilities.mana.network.PacketSyncManaToClient;
import com.example.testmod.setup.Messages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;

import static com.example.testmod.registries.AttributeRegistry.MAX_MANA;

public class ManaManager extends SavedData {

    private int counter = 0;

    @Nonnull
    public static ManaManager get(Level level) {

        if (level.isClientSide) {
            throw new RuntimeException("Don't access the ManaManager client-side!");
        }

        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(ManaManager::new, ManaManager::new, "manamanager");
    }

    public PlayerMana getFromPlayerCapability(ServerPlayer serverPlayer) {
        var capContainer = serverPlayer.getCapability(PlayerManaProvider.PLAYER_MANA);
        if (capContainer.isPresent()) {
            return capContainer.resolve().orElse(new PlayerMana());
        }
        return new PlayerMana();
    }

    public int getPlayerCurrentMana(ServerPlayer serverPlayer) {
        return getFromPlayerCapability(serverPlayer).getMana();
    }

    public void setPlayerCurrentMana(ServerPlayer serverPlayer, int newManaValue) {
        getFromPlayerCapability(serverPlayer).setMana(newManaValue);
    }

    public int regenPlayerMana(ServerPlayer serverPlayer) {
        int playerMaxMana = (int) serverPlayer.getAttributeValue(MAX_MANA.get());
        int increment = Math.round(Math.max(playerMaxMana * .01f, 1));

        PlayerMana manaState = getFromPlayerCapability(serverPlayer);

        if (manaState.getMana() == playerMaxMana) {
            return playerMaxMana;
        }

        if (manaState.getMana() + increment < playerMaxMana) {
            manaState.addMana(increment);
        } else {
            manaState.setMana(playerMaxMana);
        }

        //Don't really need this any longer since we are using the player cap for saving the state
        //setDirty();

        return manaState.getMana();
    }

    public void tick(Level level) {
        counter--;
        if (counter <= 0) {
            counter = 20;
            // Synchronize the mana to the players in this world
            // todo expansion: keep the previous data that was sent to the player and only send if changed
            level.players().forEach(player -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    Messages.sendToPlayer(new PacketSyncManaToClient(regenPlayerMana(serverPlayer)), serverPlayer);
                }
            });
        }
    }

    public ManaManager() {
    }

    public ManaManager(CompoundTag tag) {

    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }
}
