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

public class SyncAllCameraShakesPacket implements CustomPacketPayload {
    public static final Type<SyncAllCameraShakesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_all_camera_shake"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncAllCameraShakesPacket> STREAM_CODEC = CustomPacketPayload.codec(SyncAllCameraShakesPacket::write, SyncAllCameraShakesPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }



    ArrayList<CameraShakeData> cameraShakeData;


    public SyncAllCameraShakesPacket(ArrayList<CameraShakeData> cameraShakeData) {
        this.cameraShakeData = cameraShakeData;
    }

    public SyncAllCameraShakesPacket(FriendlyByteBuf buf) {
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

    public static void handle(SyncAllCameraShakesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CameraShakeManager.cameraShakeData.clear();
            CameraShakeManager.cameraShakeData.addAll(packet.cameraShakeData);
        });
    }


}