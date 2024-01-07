package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundEquipmentChanged {

    public ClientboundEquipmentChanged() {
    }

    public ClientboundEquipmentChanged(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(true);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(ClientMagicData::updateSpellSelectionManager);
        return true;
    }
}