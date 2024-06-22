package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


import java.util.function.Supplier;

public class ServerboundQuickCast {

    private int slot;

    public ServerboundQuickCast(int slot) {
        this.slot = slot;
    }

    public ServerboundQuickCast(FriendlyByteBuf buf) {
        slot = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            Utils.serverSideInitiateQuickCast(serverPlayer, slot);
        });
        return true;
    }
}
