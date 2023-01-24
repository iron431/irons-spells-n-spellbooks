package com.example.testmod.capabilities.magic;

import com.example.testmod.TestMod;
import com.example.testmod.network.ClientBoundSyncPlayerData;
import com.example.testmod.setup.Messages;
import net.minecraft.world.entity.player.Player;

public class PlayerSyncedData {

    //TODO: may want to switch this to ServerPlayer.UUID
    private final int serverPlayerId;
    private Player player;

    private boolean hasAngelWings;

    //Use this on the client
    public PlayerSyncedData(int serverPlayerId) {
        this.player = null;
        this.serverPlayerId = serverPlayerId;
        this.hasAngelWings = false;
    }

    //Use this on the server
    public PlayerSyncedData(Player player) {
        this(player.getId());
        this.player = player;
    }

    public int getServerPlayerId() {
        return serverPlayerId;
    }

    public boolean getHasAngelWings() {
        return hasAngelWings;
    }

    public void setHasAngelWings(boolean hasAngelWings) {
        this.hasAngelWings = hasAngelWings;
        TestMod.LOGGER.debug("setHasAngelWings: isPlayerNull: {}", player == null);
        Messages.sendToPlayersTrackingEntity(new ClientBoundSyncPlayerData(this), player);
    }
}
