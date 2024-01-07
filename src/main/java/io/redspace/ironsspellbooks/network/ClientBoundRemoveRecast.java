package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundRemoveRecast {

    private final String spellId;

    public ClientBoundRemoveRecast(String spellId) {
        this.spellId = spellId;
    }

    public ClientBoundRemoveRecast(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.getRecasts().removeRecast(spellId);
        });
        return true;
    }
}