package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class SyncRecastPacket implements CustomPacketPayload {

    private final RecastInstance recastInstance;

    public SyncRecastPacket(RecastInstance recastInstance) {
        this.recastInstance = recastInstance;
    }

    public SyncRecastPacket(FriendlyByteBuf buf) {
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
            ClientMagicData.cacheClientSummons();
        });
        return true;
    }
}