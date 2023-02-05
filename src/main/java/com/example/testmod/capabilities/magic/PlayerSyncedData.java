package com.example.testmod.capabilities.magic;

import com.example.testmod.network.ClientBoundSyncPlayerData;
import com.example.testmod.setup.Messages;
import net.minecraft.world.entity.player.Player;

public class PlayerSyncedData {

    //TODO: may want to switch this to ServerPlayer.UUID
    private final int serverPlayerId;
    private Player player;

    /**
     * REMINDER: Need to update ClientBoundSyncPlayerData when adding fields to this class
     **/
    private boolean hasAngelWings;
    private boolean hasEvasion;

    //Use this on the client
    public PlayerSyncedData(int serverPlayerId) {
        this.player = null;
        this.serverPlayerId = serverPlayerId;
        this.hasAngelWings = false;
        this.hasEvasion = false;
    }

    //Use this on the server
    public PlayerSyncedData(Player player) {
        this(player.getId());
        this.player = player;
    }

    public int getServerPlayerId() {
        return serverPlayerId;
    }

    private void doSync() {
        //this.player will only be null on the client side
        if (this.player != null) {
            Messages.sendToPlayersTrackingEntity(new ClientBoundSyncPlayerData(this), player);
        }
    }

    public boolean getHasAngelWings() {
        return hasAngelWings;
    }

    public void setHasAngelWings(boolean hasAngelWings) {
        this.hasAngelWings = hasAngelWings;
        doSync();
    }

    public boolean getHasEvasion() {
        return hasEvasion;
    }

    public void setHasEvasion(boolean hasEvasion) {
        this.hasEvasion = hasEvasion;
        doSync();
    }
}
