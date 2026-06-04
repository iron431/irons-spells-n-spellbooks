package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.spells.CastingMobAimingData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncCastingMobAimingDataPacket implements CustomPacketPayload {
    private final int entityId;
    private final CastingMobAimingData aimingData;

    public static final CustomPacketPayload.Type<SyncCastingMobAimingDataPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_casting_mob_aiming_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCastingMobAimingDataPacket> STREAM_CODEC =
            CustomPacketPayload.codec(SyncCastingMobAimingDataPacket::write, SyncCastingMobAimingDataPacket::new);

    public SyncCastingMobAimingDataPacket(int entityId, CastingMobAimingData aimingData) {
        this.entityId = entityId;
        this.aimingData = aimingData;
    }

    public SyncCastingMobAimingDataPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.aimingData = new CastingMobAimingData();
        this.aimingData.readFromBuffer(buf);
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        aimingData.writeToBuffer(buf);
    }

    public static void handle(SyncCastingMobAimingDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientSpellCastHelper.handleCastingMobAimingData(packet.entityId, packet.aimingData));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
