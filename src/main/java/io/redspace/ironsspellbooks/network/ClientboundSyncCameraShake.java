package io.redspace.ironsspellbooks.network;


import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientboundSyncCameraShake {
    List<CameraShakeData> cameraShakeData;

    public ClientboundSyncCameraShake(List<CameraShakeData> cameraShakeData) {
        this.cameraShakeData = cameraShakeData;
    }

    public ClientboundSyncCameraShake(FriendlyByteBuf buf) {
        cameraShakeData = new ArrayList<>();
        int i = buf.readInt();
        for (int j = 0; j < i; j++) {
            cameraShakeData.add(CameraShakeData.deserializeFromBuffer(buf));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(cameraShakeData.size());
        for (CameraShakeData data : cameraShakeData)
            data.serializeToBuffer(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            CameraShakeManager.clientCameraShakeData.clear();
            CameraShakeManager.clientCameraShakeData.addAll(cameraShakeData);
        });

        return true;
    }
}