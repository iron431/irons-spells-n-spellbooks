package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncEntityDataPacket implements CustomPacketPayload {
    SyncedSpellData syncedSpellData;
    int entityId;
    public static final CustomPacketPayload.Type<SyncEntityDataPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_entity_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityDataPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncEntityDataPacket::write, SyncEntityDataPacket::new);

    public SyncEntityDataPacket(SyncedSpellData syncedSpellData, IMagicEntity entity) {
        this.syncedSpellData = syncedSpellData;
        this.entityId = ((Entity) entity).getId();
    }

    public SyncEntityDataPacket(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        syncedSpellData = SyncedSpellData.read(buf);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        SyncedSpellData.write(buf, syncedSpellData);
    }

    public static void handle(SyncEntityDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientMagicData.handleAbstractCastingMobSyncedData(packet.entityId, packet.syncedSpellData);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
