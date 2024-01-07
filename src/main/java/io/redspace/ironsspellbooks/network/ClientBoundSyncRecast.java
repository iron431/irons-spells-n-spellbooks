package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundSyncRecast {

    private final RecastInstance recastInstance;

    public ClientBoundSyncRecast(RecastInstance recastInstance) {
        this.recastInstance = recastInstance;
    }

    public ClientBoundSyncRecast(FriendlyByteBuf buf) {
        recastInstance = new RecastInstance();
        recastInstance.readFromBuffer(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        if (recastInstance != null) {
            recastInstance.writeToBuffer(buf);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.getRecasts().forceAddRecast(recastInstance);
        });
        return true;
    }
}