package io.redspace.ironsspellbooks.network;


import io.redspace.ironsspellbooks.IronsSpellbooks;
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
    ArrayList<CameraShakeData> cameraShakeData;

    public ClientboundSyncCameraShake(ArrayList<CameraShakeData> cameraShakeData) {
        this.cameraShakeData = cameraShakeData;
    }

    public ClientboundSyncCameraShake(FriendlyByteBuf buf) {
        cameraShakeData = new ArrayList<>();
        int i = buf.readInt();
        //IronsSpellbooks.LOGGER.debug("ClientboundSyncCameraShake construct from buf: {}", i);
        for (int j = 0; j < i; j++) {
            cameraShakeData.add(CameraShakeData.deserializeFromBuffer(buf));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(cameraShakeData.size());
        //IronsSpellbooks.LOGGER.debug("ClientboundSyncCameraShake.toBytes: {}", cameraShakeData.size());

        for (CameraShakeData data : cameraShakeData)
            data.serializeToBuffer(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            //IronsSpellbooks.LOGGER.debug("ClientboundsyncCameraShakeData {}", cameraShakeData.size());
            CameraShakeManager.clientCameraShakeData = cameraShakeData;
        });

        return true;
    }
}