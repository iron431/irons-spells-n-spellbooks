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

public class SyncCameraShakePacket implements CustomPacketPayload {
    ArrayList<CameraShakeData> cameraShakeData;
    public static final CustomPacketPayload.Type<SyncCameraShakePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_camera_shake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCameraShakePacket> STREAM_CODEC = CustomPacketPayload.codec(SyncCameraShakePacket::write, SyncCameraShakePacket::new);

    public SyncCameraShakePacket(ArrayList<CameraShakeData> cameraShakeData) {
        this.cameraShakeData = cameraShakeData;
    }

    public SyncCameraShakePacket(FriendlyByteBuf buf) {
        cameraShakeData = new ArrayList<>();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            cameraShakeData.add(CameraShakeData.deserializeFromBuffer(buf));
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(cameraShakeData.size());
        for (CameraShakeData data : cameraShakeData)
            data.serializeToBuffer(buf);
    }

    public static void handle(SyncCameraShakePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CameraShakeManager.clientCameraShakeData = packet.cameraShakeData;
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}