package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

/**
 * Deprecated wrapper-class for 1.20.1 API compat. Use {@link EntityEventPacket} instead
 */
@Deprecated(forRemoval = true)
public class ClientboundEntityEvent<T extends Entity & IClientEventEntity> extends EntityEventPacket<T> {
    public ClientboundEntityEvent(Entity pEntity, byte pEventId) {
        super(pEntity, pEventId);
    }

    public ClientboundEntityEvent(FriendlyByteBuf pBuffer) {
        super(pBuffer);
    }
}
