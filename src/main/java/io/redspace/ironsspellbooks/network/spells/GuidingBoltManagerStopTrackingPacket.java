package io.redspace.ironsspellbooks.network.spells;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class GuidingBoltManagerStopTrackingPacket implements CustomPacketPayload {
    private final UUID entity;

    public GuidingBoltManagerStopTrackingPacket(Entity entity) {
        this.entity = entity.getUUID();
    }

    public GuidingBoltManagerStopTrackingPacket(FriendlyByteBuf buf) {
        this.entity = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entity);

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuidingBoltManager.handleClientboundStopTracking(entity);
        });
        return true;
    }
}