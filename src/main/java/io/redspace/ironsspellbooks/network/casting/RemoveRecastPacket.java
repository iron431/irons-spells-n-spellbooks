package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RemoveRecastPacket implements CustomPacketPayload {

    private final String spellId;

    public RemoveRecastPacket(String spellId) {
        this.spellId = spellId;
    }

    public RemoveRecastPacket(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.getRecasts().removeRecast(spellId);
            ClientMagicData.cacheClientSummons();
        });
        return true;
    }
}