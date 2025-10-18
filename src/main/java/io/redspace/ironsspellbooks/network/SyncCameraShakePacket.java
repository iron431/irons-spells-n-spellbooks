package io.redspace.ironsspellbooks.network;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public record SyncCameraShakePacket(CameraShakeData data, boolean remove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCameraShakePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_camera_shake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCameraShakePacket> STREAM_CODEC = CustomPacketPayload.codec(SyncCameraShakePacket::write, SyncCameraShakePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public SyncCameraShakePacket(FriendlyByteBuf buf) {
        this(CameraShakeData.deserializeFromBuffer(buf), buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        this.data.serializeToBuffer(buf);
        buf.writeBoolean(remove);
    }

    public static void handle(SyncCameraShakePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.remove) {
                CameraShakeManager.removeClientCameraShake(packet.data);
            } else {
                CameraShakeManager.addClientCameraShake(packet.data);
            }
        });
    }

}