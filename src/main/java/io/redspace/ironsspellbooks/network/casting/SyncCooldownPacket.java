package io.redspace.ironsspellbooks.network.casting;

import io.redspace.ironsspellbooks.api.backwards_compat.CustomPacketPayload;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCooldownPacket implements CustomPacketPayload {
    private final String spellId;
    private final int duration;

    public SyncCooldownPacket(String spellId, int duration) {
        this.spellId = spellId;
        this.duration = duration;
    }

    public SyncCooldownPacket(FriendlyByteBuf buf) {
        spellId = buf.readUtf();
        duration = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(spellId);
        buf.writeInt(duration);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientMagicData.getCooldowns().addCooldown(spellId, duration);
        });
        return true;
    }
}
