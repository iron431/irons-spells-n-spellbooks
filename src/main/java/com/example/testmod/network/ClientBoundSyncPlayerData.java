package com.example.testmod.network;


import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.example.testmod.capabilities.magic.SyncedSpellData.SYNCED_SPELL_DATA;

public class ClientBoundSyncPlayerData {
    SyncedSpellData syncedSpellData;

    public ClientBoundSyncPlayerData(SyncedSpellData playerSyncedData) {
        this.syncedSpellData = playerSyncedData;
    }

    public ClientBoundSyncPlayerData(FriendlyByteBuf buf) {
        syncedSpellData = SYNCED_SPELL_DATA.read(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        SYNCED_SPELL_DATA.write(buf, syncedSpellData);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ClientMagicData.handlePlayerSyncedData(syncedSpellData);
        });

        return true;
    }
}