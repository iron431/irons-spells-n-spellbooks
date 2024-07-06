package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

public class EntityEventPacket<T extends Entity & IClientEventEntity> implements CustomPacketPayload {
    private final int entityId;
    private final byte eventId;

    public static final CustomPacketPayload.Type<EntityEventPacket<?>> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "entity_event"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityEventPacket<?>> STREAM_CODEC = CustomPacketPayload.codec(EntityEventPacket::write, EntityEventPacket::new);

    public EntityEventPacket(Entity pEntity, byte pEventId) {
        this.entityId = pEntity.getId();
        this.eventId = pEventId;
    }

    public EntityEventPacket(FriendlyByteBuf pBuffer) {
        this.entityId = pBuffer.readInt();
        this.eventId = pBuffer.readByte();
    }

    public void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeInt(this.entityId);
        pBuffer.writeByte(this.eventId);
    }

    public static void handle(EntityEventPacket<?> packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            MinecraftInstanceHelper.ifPlayerPresent(player -> {
                if (packet.getEntity(player.level) instanceof IClientEventEntity entity) {
                    entity.handleClientEvent(packet.getEventId());
                }
            });
        });
    }

    @Nullable
    public Entity getEntity(Level pLevel) {
        return pLevel.getEntity(this.entityId);
    }

    public byte getEventId() {
        return this.eventId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}