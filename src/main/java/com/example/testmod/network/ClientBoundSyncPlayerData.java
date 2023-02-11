package com.example.testmod.network;


import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSyncPlayerData {
    SyncedSpellData playerSyncedData;

    public ClientBoundSyncPlayerData(SyncedSpellData playerSyncedData) {
        this.playerSyncedData = playerSyncedData;
    }

    public ClientBoundSyncPlayerData(FriendlyByteBuf buf) {
        playerSyncedData = new SyncedSpellData(buf.readInt());
        playerSyncedData.setHasEvasion(buf.readBoolean());
        playerSyncedData.setHasAngelWings(buf.readBoolean());
        playerSyncedData.setHasHeartstop(buf.readBoolean());
        playerSyncedData.setHeartstopAccumulatedDamage(buf.readFloat());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerSyncedData.getServerPlayerId());
        buf.writeBoolean(playerSyncedData.getHasEvasion());
        buf.writeBoolean(playerSyncedData.getHasAngelWings());
        buf.writeBoolean(playerSyncedData.getHasHeartstop());
        buf.writeFloat(playerSyncedData.getHeartstopAccumulatedDamage());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ClientMagicData.handlePlayerSyncedData(playerSyncedData);
        });

        return true;
    }
}