package io.redspace.ironsspellbooks.network;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncCameraShakePacket(CameraShakeData data, boolean remove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCameraShakePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "sync_camera_shake"));
//    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCameraShakePacket> STREAM_CODEC = CustomPacketPayload.codec(SyncCameraShakePacket::write, SyncCameraShakePacket::new);

//    @Override
//    public Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }

    public SyncCameraShakePacket(FriendlyByteBuf buf) {
        this(CameraShakeData.deserializeFromBuffer(buf), buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        this.data.serializeToBuffer(buf);
        buf.writeBoolean(remove);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (remove) {
                CameraShakeManager.removeClientCameraShake(data);
            } else {
                CameraShakeManager.addClientCameraShake(data);
            }
        });
        return true;
    }
}