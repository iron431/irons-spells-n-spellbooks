package io.redspace.ironsspellbooks.api.network;

import io.redspace.ironsspellbooks.network.EntityEventPacket;
import net.minecraft.world.entity.Entity;
import io.redspace.ironsspellbooks.setup.PacketDistributor;

public interface IClientEventEntity {
    void handleClientEvent(byte eventId);

    default <T extends Entity & IClientEventEntity> void serverTriggerEvent(byte eventId) {
        PacketDistributor.sendToPlayersTrackingEntity((T) this, new EntityEventPacket<T>((T) this, eventId));
    }
}
