package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class EntityEventPacket<T extends Entity & IClientEventEntity> implements CustomPacketPayload {
    private final int entityId;
    private final byte eventId;

    public EntityEventPacket(Entity pEntity, byte pEventId) {
        this.entityId = pEntity.getId();
        this.eventId = pEventId;
    }

    public EntityEventPacket(FriendlyByteBuf pBuffer) {
        this.entityId = pBuffer.readInt();
        this.eventId = pBuffer.readByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void toBytes(FriendlyByteBuf pBuffer) {
        pBuffer.writeInt(this.entityId);
        pBuffer.writeByte(this.eventId);
    }


    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            MinecraftInstanceHelper.ifPlayerPresent(player -> {
                if (getEntity(player.level) instanceof IClientEventEntity entity) {
                    entity.handleClientEvent(this.eventId);
                }
            });
        });

        return true;
    }

    @Nullable
    public Entity getEntity(Level pLevel) {
        return pLevel.getEntity(this.entityId);
    }

    public byte getEventId() {
        return this.eventId;
    }
}